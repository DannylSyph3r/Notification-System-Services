const firebaseConfig = __FIREBASE_CONFIG_JSON_PLACEHOLDER__;

firebase.initializeApp(firebaseConfig);
const messaging = firebase.messaging();
const permissionButton = document.getElementById('request-permission');
const permissionStatus = document.getElementById('permission-status');
const tokenSection = document.getElementById('token-section');
const tokenText = document.getElementById('device-token');
const copyButton = document.getElementById('copy-token');
const notificationList = document.getElementById('notification-list');

permissionButton.addEventListener('click', async () => {
    console.log('Requesting notification permission...');
    try {
        const permission = await Notification.requestPermission();
        if (permission === 'granted') {
            console.log('Notification permission granted.');
            permissionStatus.textContent = 'Status: Granted';
            permissionStatus.style.color = 'green';
            await getToken();
        } else {
            console.warn('Notification permission denied.');
            permissionStatus.textContent = 'Status: Denied';
            permissionStatus.style.color = 'red';
        }
    } catch (error) {
        console.error('Error requesting permission:', error);
        permissionStatus.textContent = 'Status: Error';
        permissionStatus.style.color = 'red';
    }
});

async function getToken() {
    try {
        const vapidKey = "__FIREBASE_VAPID_KEY_PLACEHOLDER__";

        const currentToken = await messaging.getToken({ vapidKey: vapidKey });

        if (currentToken) {
            console.log('FCM Token:', currentToken);
            tokenText.value = currentToken;
            tokenSection.style.display = 'block';
        } else {
            console.warn('No registration token available. Request permission to generate one.');
        }
    } catch (err) {
        console.error('An error occurred while retrieving token:', err);
    }
}

messaging.onMessage((payload) => {
    console.log('Message received in foreground:', payload);

    const placeholder = document.querySelector('.placeholder');
    if (placeholder) {
        placeholder.remove();
    }

    const { notification } = payload;
    const listItem = document.createElement('li');
    listItem.innerHTML = `
        <strong>${notification.title}</strong>
        <p>${notification.body}</p>
        <small>${new Date().toLocaleTimeString()}</small>
    `;
    notificationList.prepend(listItem);

    new Notification(notification.title, {
        body: notification.body,
        icon: notification.image
    });
});

copyButton.addEventListener('click', () => {
    if (!navigator.clipboard) {
        tokenText.select();
        document.execCommand('copy');
        alert('Token copied to clipboard (fallback).');
        return;
    }

    navigator.clipboard.writeText(tokenText.value).then(() => {
        alert('Token copied to clipboard!');
    }).catch(err => {
        console.error('Failed to copy token:', err);
        alert('Failed to copy token.');
    });
});