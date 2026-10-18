import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '../stores/authStore';
import { bloodRequestAPI, notificationAPI } from '../services/api';

function Home() {
  const { user, logout } = useAuthStore();
  const [activeRequests, setActiveRequests] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const navigate = useNavigate();

  useEffect(() => {
    loadRequests();
    loadNotifications();
  }, []);

  const loadRequests = async () => {
    try {
      const response = await bloodRequestAPI.getAll();
      setActiveRequests(response.data);
    } catch (error) {
      console.error('Failed to load requests:', error);
    }
  };

  const loadNotifications = async () => {
    try {
      const response = await notificationAPI.getUnread();
      setUnreadCount(response.data.length);
    } catch (error) {
      console.error('Failed to load notifications:', error);
    }
  };

  return (
    <div className="page home-page">
      <div className="top-bar">
        <h1>BloodConnect</h1>
        <div className="top-bar-actions">
          <button className="icon-btn" onClick={() => navigate('/notifications')}>
            🔔 {unreadCount > 0 && <span className="badge">{unreadCount}</span>}
          </button>
          <button className="icon-btn" onClick={() => navigate('/donor-requests')}>
            📋
          </button>
          {user?.role === 'ADMIN' && (
            <button className="icon-btn" onClick={() => navigate('/admin/donors')} title="Verify Donors">
              🛡️
            </button>
          )}
          <button className="icon-btn" onClick={logout}>
            🚪
          </button>
        </div>
      </div>

      <div className="container">
        <div className="hero">
          <h2>Welcome, {user?.name || 'User'}!</h2>
          <p>Help save lives by donating blood or creating an emergency request</p>
        </div>

        <div className="cta-section">
          <button
            className="btn btn-primary btn-lg"
            onClick={() => navigate('/create-request')}
          >
            🩹 Create Blood Request
          </button>
          <button
            className="btn btn-secondary btn-lg"
            onClick={() => navigate('/donor-registration')}
          >
            ❤️ Become a Donor
          </button>
        </div>

        <div className="section">
          <h3>Active Blood Requests</h3>
          <div className="requests-list">
            {activeRequests.length > 0 ? (
              activeRequests.map((request) => (
                <div
                  key={request.id}
                  className="request-card"
                  onClick={() => navigate(`/request/${request.id}`)}
                >
                  <div className="request-header">
                    <span className={`blood-type ${request.bloodGroup}`}>
                      {request.bloodGroup}
                    </span>
                    <span className={`urgency ${request.urgency}`}>
                      {request.urgency}
                    </span>
                  </div>
                  <p className="request-location">📍 {request.location}</p>
                  <p className="request-units">
                    Units needed: {request.unitsRequired}
                  </p>
                  <p className="request-status">Status: {request.status}</p>
                  <p className="request-donors">
                    Donors notified: {request.donorCountNotified}
                  </p>
                </div>
              ))
            ) : (
              <p className="no-data">No active blood requests</p>
            )}
          </div>
        </div>
      </div>

      <div className="bottom-nav">
        <button className="nav-item active" onClick={() => navigate('/')}>
          🏠 Home
        </button>
        <button className="nav-item" onClick={() => navigate('/create-request')}>
          📝 Requests
        </button>
        <button className="nav-item" onClick={() => navigate('/donor-requests')}>
          🗺️ Map
        </button>
        <button className="nav-item" onClick={() => navigate('/notifications')}>
          👤 Profile
        </button>
      </div>
    </div>
  );
}

export default Home;
