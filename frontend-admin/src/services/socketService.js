import SockJS from 'sockjs-client';
import Stomp from 'stompjs';

// Đi qua proxy /ws của Vite để không phụ thuộc cổng gateway (cổng này đổi tuỳ máy).
// Đặt VITE_SOCKET_URL nếu cần trỏ thẳng tới gateway.
const SOCKET_URL = import.meta.env.VITE_SOCKET_URL || '/ws';

let stompClient = null;
let connected = false;
let notificationCallback = null;

// orderId -> callback xử lý tin nhắn chat của đơn đó
const chatHandlers = new Map();
// orderId -> subscription STOMP (để huỷ khi rời khỏi hội thoại)
const chatSubs = new Map();

/** Kênh thông báo chung của admin (đơn mới, thanh toán...). */
const subscribeAdminTopic = () => {
    console.log('🔌 Subscribing to /topic/admin/notifications');
    stompClient.subscribe('/topic/admin/notifications', (msg) => {
        try {
            const notification = JSON.parse(msg.body);
            console.log('📩 New Admin Notification:', notification);
            if (notificationCallback) {
                notificationCallback(notification);
            }
        } catch (e) {
            console.error('Error parsing notification:', e);
        }
    });
};

/** Kênh chat hỗ trợ của một đơn hàng, khớp destination bên service-socket. */
const subscribeChatTopic = (orderId) => {
    if (!stompClient || !connected || chatSubs.has(orderId)) return;
    console.log(`🔌 Subscribing to /topic/orders/${orderId}/chat`);
    const sub = stompClient.subscribe(`/topic/orders/${orderId}/chat`, (msg) => {
        try {
            const payload = JSON.parse(msg.body);
            const handler = chatHandlers.get(orderId);
            if (handler) {
                handler(payload);
            }
        } catch (e) {
            console.error('Error parsing chat message:', e);
        }
    });
    chatSubs.set(orderId, sub);
};

export const connectSocket = (onMessageReceived) => {
    // Chỉ ghi đè khi có callback mới, tránh việc mở trang chat làm mất
    // handler thông báo mà Layout đã đăng ký.
    if (onMessageReceived) notificationCallback = onMessageReceived;

    if (stompClient) {
        console.log('✅ Admin Socket already initialized, reusing instance.');
        return stompClient;
    }

    const socket = new SockJS(SOCKET_URL);
    stompClient = Stomp.over(socket);
    stompClient.debug = () => {};

    stompClient.connect({}, () => {
        console.log('✅ Admin Connected to WebSocket');
        connected = true;

        subscribeAdminTopic();

        // Đăng ký lại các hội thoại đang mở (quan trọng khi vừa kết nối lại)
        chatHandlers.forEach((_, orderId) => subscribeChatTopic(orderId));
    }, (error) => {
        console.error('❌ WebSocket error:', error);
        stompClient = null;
        connected = false;
        chatSubs.clear();
        setTimeout(() => connectSocket(notificationCallback), 5000);
    });

    return stompClient;
};

/** Lắng nghe tin nhắn của một đơn hàng. Tự kết nối nếu socket chưa mở. */
export const subscribeOrderChat = (orderId, onChatMessage) => {
    if (!orderId) return;
    const key = String(orderId);
    chatHandlers.set(key, onChatMessage);

    if (!stompClient) {
        connectSocket(notificationCallback);
    } else {
        subscribeChatTopic(key);
    }
};

export const unsubscribeOrderChat = (orderId) => {
    if (!orderId) return;
    const key = String(orderId);
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
};

export const disconnectSocket = () => {
    if (stompClient) {
        stompClient.disconnect();
        stompClient = null;
        connected = false;
        chatSubs.clear();
        chatHandlers.clear();
        console.log('🛑 Admin Socket Disconnected');
    }
};
