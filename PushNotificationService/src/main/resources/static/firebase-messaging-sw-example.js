importScripts('https://www.gstatic.com/firebasejs/9.0.0/firebase-app-compat.js');
importScripts('https://www.gstatic.com/firebasejs/9.0.0/firebase-messaging-compat.js');

const firebaseConfig = {};

firebase.initializeApp(firebaseConfig);
const messaging = firebase.messaging();

messaging.onBackgroundMessage((payload) => {
    console.log('Background message received:', payload);

    const { notification } = payload;

    const notificationTitle = notification.title;
    const notificationOptions = {
        body: notification.body,
        icon: notification.image || '/icon-192.png',
        badge: '/badge-72.png',
        data: payload.data
    };

    return self.registration.showNotification(notificationTitle, notificationOptions);
});