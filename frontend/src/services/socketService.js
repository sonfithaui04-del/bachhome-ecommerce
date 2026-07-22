import SockJS from 'sockjs-client';
import Stomp from 'stompjs';

// URL của service-socket (qua API Gateway port 8080)
const SOCKET_URL = 'http://localhost:8080/ws';

let stompClient = null;
let notificationCallback = null;

export const connectSocket = (onMessageReceived, userId = null) => {
    // Update callback
    notificationCallback = onMessageReceived;

    if (stompClient) {
        console.log('✅ User Socket already initialized, reusing instance.');
        return stompClient;
    }

    const socket = new SockJS(SOCKET_URL);
    stompClient = Stomp.over(socket);
    stompClient.debug = () => {};

    stompClient.connect({}, () => {
        console.log('✅ Connected to WebSocket');

        if (userId) {
            console.log(`🔌 Subscribing to /topic/user/${userId}`);
            stompClient.subscribe(`/topic/user/${userId}`, (msg) => {
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
        }
    }, (error) => {
        console.error('❌ WebSocket error:', error);
        stompClient = null;
        setTimeout(() => connectSocket(notificationCallback, userId), 5000);
    });

    return stompClient;
};

export const disconnectSocket = () => {
    if (stompClient) {
        stompClient.disconnect();
        stompClient = null;
        console.log('🛑 User Socket Disconnected');
    }
};
