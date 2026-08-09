import SockJS from 'sockjs-client';
import Stomp from 'stompjs';

// URL của service-socket (qua API Gateway). Mặc định đi qua proxy của Vite (/ws)
// để không phụ thuộc cổng gateway; đặt VITE_SOCKET_URL nếu cần trỏ thẳng.
const SOCKET_URL = import.meta.env.VITE_SOCKET_URL || '/ws';

let stompClient = null;
let connected = false;
let notificationCallback = null;
let currentUserId = null;

// orderId -> tập các callback quan tâm tới đơn đó.
// Dùng Set vì nhiều nơi cùng nghe một đơn: trang Đơn hàng nghe để đếm tin chưa
// đọc, còn ChatBox nghe để hiển thị. Nếu chỉ giữ một callback thì cái sau sẽ
// ghi đè cái trước.
const chatHandlers = new Map();
// orderId -> subscription STOMP (chỉ huỷ khi không còn ai nghe)
const chatSubs = new Map();

/** Kênh thông báo riêng của người dùng (trạng thái đơn, thanh toán...). */
const subscribeUserTopic = () => {
    if (!currentUserId) return;
    console.log(`🔌 Subscribing to /topic/user/${currentUserId}`);
    stompClient.subscribe(`/topic/user/${currentUserId}`, (msg) => {
        try {
            const notification = JSON.parse(msg.body);
            console.log('📩 New notification:', notification);
            if (notificationCallback) {
                notificationCallback(notification);
            }
        } catch (e) {
            console.error('Error parsing notification:', e);
        }
    });
};

/** Kênh chat hỗ trợ của một đơn hàng, khớp với destination bên service-socket. */
const subscribeChatTopic = (orderId) => {
    if (!stompClient || !connected || chatSubs.has(orderId)) return;
    console.log(`🔌 Subscribing to /topic/orders/${orderId}/chat`);
    const sub = stompClient.subscribe(`/topic/orders/${orderId}/chat`, (msg) => {
        try {
            const payload = JSON.parse(msg.body);
            const handlers = chatHandlers.get(orderId);
            if (handlers) {
                handlers.forEach(fn => fn(payload));
            }
        } catch (e) {
            console.error('Error parsing chat message:', e);
        }
    });
    chatSubs.set(orderId, sub);
};

export const connectSocket = (onMessageReceived, userId = null) => {
    // Chỉ ghi đè khi có giá trị mới, tránh việc mở khung chat làm mất
    // callback thông báo mà Navbar đã đăng ký trước đó.
    if (onMessageReceived) notificationCallback = onMessageReceived;
    if (userId) currentUserId = userId;

    if (stompClient) {
        console.log('✅ User Socket already initialized, reusing instance.');
        return stompClient;
    }

    const socket = new SockJS(SOCKET_URL);
    stompClient = Stomp.over(socket);
    stompClient.debug = () => {};

    stompClient.connect({}, () => {
        console.log('✅ Connected to WebSocket');
        connected = true;

        subscribeUserTopic();

        // Đăng ký lại các khung chat đang mở (quan trọng khi vừa kết nối lại)
        chatHandlers.forEach((_, orderId) => subscribeChatTopic(orderId));
    }, (error) => {
        console.error('❌ WebSocket error:', error);
        stompClient = null;
        connected = false;
        chatSubs.clear();
        setTimeout(() => connectSocket(notificationCallback, currentUserId), 5000);
    });

    return stompClient;
};

/**
 * Lắng nghe tin nhắn hỗ trợ của một đơn hàng. Tự kết nối nếu socket chưa mở.
 * Trả về hàm huỷ đăng ký của riêng callback này.
 */
export const subscribeOrderChat = (orderId, onChatMessage) => {
    if (!orderId || !onChatMessage) return () => {};
    const key = String(orderId);

    if (!chatHandlers.has(key)) {
        chatHandlers.set(key, new Set());
    }
    chatHandlers.get(key).add(onChatMessage);

    if (!stompClient) {
        connectSocket(notificationCallback, currentUserId);
    } else {
        subscribeChatTopic(key);
    }

    return () => unsubscribeOrderChat(key, onChatMessage);
};

/**
 * Bỏ một callback khỏi đơn hàng. Chỉ huỷ subscription STOMP khi không còn ai
 * nghe đơn đó nữa. Không truyền handler thì gỡ toàn bộ.
 */
export const unsubscribeOrderChat = (orderId, handler) => {
    if (!orderId) return;
    const key = String(orderId);
    const handlers = chatHandlers.get(key);
    if (!handlers) return;

    if (handler) {
        handlers.delete(handler);
    } else {
        handlers.clear();
    }

    if (handlers.size === 0) {
        chatHandlers.delete(key);
        const sub = chatSubs.get(key);
        if (sub) {
            try {
                sub.unsubscribe();
            } catch (e) {
                // socket có thể đã đóng, bỏ qua
            }
            chatSubs.delete(key);
        }
    }
};

export const disconnectSocket = () => {
    if (stompClient) {
        stompClient.disconnect();
        stompClient = null;
        connected = false;
        chatSubs.clear();
        chatHandlers.clear();
        console.log('🛑 User Socket Disconnected');
    }
};
