import SockJS from 'sockjs-client';
import Stomp from 'stompjs';

const SOCKET_URL = 'http://localhost:8080/ws';

let stompClient = null;
let notificationCallback = null;

export const connectSocket = (onMessageReceived) => {
    // Update the callback to the latest one provided by the component
    notificationCallback = onMessageReceived;

    // If already connected or connecting, just return the instance
    if (stompClient) {
        console.log('✅ Admin Socket already initialized, reusing instance.');
        return stompClient;
    }

    const socket = new SockJS(SOCKET_URL);
    stompClient = Stomp.over(socket);
    stompClient.debug = () => {}; // Disable debug logs

    stompClient.connect({}, () => {
        console.log('✅ Admin Connected to WebSocket');

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

    }, (error) => {
        console.error('❌ WebSocket error:', error);
        stompClient = null; // Reset on error so we can retry
        setTimeout(() => connectSocket(notificationCallback), 5000);
    });

    return stompClient;
};

export const disconnectSocket = () => {
    if (stompClient) {
        stompClient.disconnect();
        stompClient = null;
        console.log('🛑 Admin Socket Disconnected');
    }
};
