import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { notificationAPI } from '../services/api';

function Notifications() {
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    loadNotifications();
  }, []);

  const loadNotifications = async () => {
    try {
      const response = await notificationAPI.getAll();
      setNotifications(response.data);
    } catch (error) {
      console.error('Failed to load notifications:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleMarkAsRead = async (id) => {
    try {
      await notificationAPI.markAsRead(id);
      loadNotifications();
    } catch (error) {
      console.error('Failed to mark notification as read:', error);
    }
  };

  const handleMarkAllAsRead = async () => {
    try {
      await notificationAPI.markAllAsRead();
      loadNotifications();
    } catch (error) {
      console.error('Failed to mark all as read:', error);
    }
  };

  if (loading) return <div className="page"><p>Loading...</p></div>;

  return (
    <div className="page notifications-page">
      <div className="top-bar">
        <button onClick={() => navigate('/')}>← Back</button>
        <h1>Notifications</h1>
        {notifications.some((n) => !n.read) && (
          <button className="btn btn-sm" onClick={handleMarkAllAsRead}>
            Mark All as Read
          </button>
        )}
      </div>

      <div className="container">
        {notifications.length > 0 ? (
          <div className="notifications-list">
            {notifications.map((notification) => (
              <div
                key={notification.id}
                className={`notification-item ${!notification.read ? 'unread' : ''}`}
              >
                <div className="notification-content">
                  <div className="notification-icon">
                    {notification.type === 'BLOOD_REQUEST' && '🩸'}
                    {notification.type === 'DONOR_RESPONSE' && '✅'}
                    {notification.type === 'VERIFICATION' && '✔️'}
                    {notification.type === 'SYSTEM' && '🔔'}
                  </div>
                  <div className="notification-text">
                    <h4>{notification.title}</h4>
                    <p>{notification.message}</p>
                    <small>{new Date(notification.createdAt).toLocaleString()}</small>
                  </div>
                </div>
                {!notification.read && (
                  <button
                    className="btn-small"
                    onClick={() => handleMarkAsRead(notification.id)}
                  >
                    ✓ Mark Read
                  </button>
                )}
              </div>
            ))}
          </div>
        ) : (
          <div className="no-data">
            <p>📭 No notifications yet</p>
          </div>
        )}
      </div>
    </div>
  );
}

export default Notifications;
