import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { notificationAPI } from '../services/api';
import { ArrowLeft, Droplet, CheckCircle, Shield, Bell } from '../components/icons';

const ICONS = {
  BLOOD_REQUEST: Droplet,
  DONOR_RESPONSE: CheckCircle,
  VERIFICATION: Shield,
  SYSTEM: Bell,
};

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

  if (loading) return <div className="page"><div className="container"><p className="no-data">Loading...</p></div></div>;

  return (
    <div className="page notifications-page">
      <div className="top-bar">
        <button className="top-bar-back" onClick={() => navigate('/')}>
          <ArrowLeft size={18} /> Back
        </button>
        <h1>Notifications</h1>
        {notifications.some((n) => !n.read) ? (
          <button className="btn-small" onClick={handleMarkAllAsRead}>
            Mark all read
          </button>
        ) : (
          <div style={{ width: 38 }} />
        )}
      </div>

      <div className="container">
        {notifications.length > 0 ? (
          <div className="notifications-list">
            {notifications.map((notification) => {
              const NIcon = ICONS[notification.type] || Bell;
              return (
                <div
                  key={notification.id}
                  className={`notification-item ${!notification.read ? 'unread' : ''}`}
                >
                  <div className="notification-content">
                    <div className="notification-icon">
                      <NIcon size={16} />
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
                      Mark read
                    </button>
                  )}
                </div>
              );
            })}
          </div>
        ) : (
          <div className="no-data">
            <Bell size={30} style={{ opacity: 0.35, marginBottom: 10 }} />
            <p>No notifications yet</p>
          </div>
        )}
      </div>
    </div>
  );
}

export default Notifications;
