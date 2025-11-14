importScripts('https://www.gstatic.com/firebasejs/9.0.0/firebase-app-compat.js');
importScripts('https://www.gstatic.com/firebasejs/9.0.0/firebase-messaging-compat.js');

const firebaseConfig = {
    apiKey: "AIzaSyDgnA8UropIZLxfjjhr6kbXz9IIGWRt23w",
    authDomain: "notification-system-f8967.firebaseapp.com",
    projectId: "notification-system-f8967",
    storageBucket: "notification-system-f8967.firebasestorage.app",
    messagingSenderId: "657011969631",
    appId: "1:657011969631:web:d1f1039c4668cc72b5d5ea"
};

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