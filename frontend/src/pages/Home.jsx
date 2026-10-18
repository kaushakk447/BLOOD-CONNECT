import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '../stores/authStore';
import { bloodRequestAPI, notificationAPI } from '../services/api';
import {
  Droplet, Heart, Bell, ClipboardList, Shield, LogOut, MapPin,
  ChevronRight, Home as HomeIcon, Map, User, Search, Users, Target,
} from '../components/icons';

const bloodLabel = (code) => (code || '').replace('_POSITIVE', '+').replace('_NEGATIVE', '-');

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
        <div className="brand">
          <div className="brand-mark"><Droplet size={17} /></div>
          <h1>BloodConnect</h1>
        </div>
        <div className="top-bar-actions">
          <button className="icon-btn" onClick={() => navigate('/notifications')}>
            <Bell size={19} />
            {unreadCount > 0 && <span className="badge">{unreadCount}</span>}
          </button>
          <button className="icon-btn" onClick={() => navigate('/donor-requests')}>
            <ClipboardList size={19} />
          </button>
          {user?.role === 'ADMIN' && (
            <button className="icon-btn" onClick={() => navigate('/admin/donors')} title="Verify Donors">
              <Shield size={19} />
            </button>
          )}
          <button className="icon-btn" onClick={logout}>
            <LogOut size={19} />
          </button>
        </div>
      </div>

      <div className="container">
        <div className="hero">
          <div className="hero-icon"><Droplet size={22} /></div>
          <h2>Welcome, {user?.name || 'User'}!</h2>
          <p>Help save lives by donating blood or creating an emergency request.</p>
        </div>

        <div className="cta-section">
          <button className="cta-card primary" onClick={() => navigate('/create-request')}>
            <span className="cta-icon"><Droplet size={18} /></span>
            <span className="cta-label">Request Blood</span>
          </button>
          <button className="cta-card secondary" onClick={() => navigate('/donor-registration')}>
            <span className="cta-icon"><Heart size={18} /></span>
            <span className="cta-label">Become a Donor</span>
          </button>
        </div>

        <div className="section">
          <div className="section-title-row">
            <h3>Active Blood Requests</h3>
          </div>
          <div className="requests-list">
            {activeRequests.length > 0 ? (
              activeRequests.map((request) => (
                <div
                  key={request.id}
                  className="request-card"
                  onClick={() => navigate(`/request/${request.id}`)}
                >
                  <div className="request-card-body">
                    <div className="request-header">
                      <span className="blood-type">
                        <Droplet size={12} /> {bloodLabel(request.bloodGroup)}
                      </span>
                      <span className={`urgency ${request.urgency}`}>
                        {request.urgency}
                      </span>
                    </div>
                    <p className="request-meta-row"><MapPin size={13} /> {request.location}</p>
                    <p className="request-meta-row"><Users size={13} /> {request.unitsRequired} units needed · {request.donorCountNotified} donors notified</p>
                  </div>
                  <ChevronRight size={18} className="request-card-chevron" />
                </div>
              ))
            ) : (
              <p className="no-data">No active blood requests right now.</p>
            )}
          </div>
        </div>

        <div className="section">
          <div className="section-title-row">
            <h3>How It Works</h3>
          </div>
          <div className="how-it-works">
            <div className="hiw-step">
              <div className="hiw-badge c1"><Droplet size={17} /></div>
              <div className="hiw-text">
                <h4>1. Request</h4>
                <p>Submit an urgent request detailing blood type and location.</p>
              </div>
            </div>
            <div className="hiw-step">
              <div className="hiw-badge c2"><Search size={17} /></div>
              <div className="hiw-text">
                <h4>2. Find</h4>
                <p>Our system locates verified donors nearby, instantly.</p>
              </div>
            </div>
            <div className="hiw-step">
              <div className="hiw-badge c3"><Target size={17} /></div>
              <div className="hiw-text">
                <h4>3. Notify</h4>
                <p>Matching donors receive immediate push notifications.</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="bottom-nav">
        <button className="nav-item active" onClick={() => navigate('/')}>
          <HomeIcon size={19} /> Home
        </button>
        <button className="nav-item" onClick={() => navigate('/create-request')}>
          <ClipboardList size={19} /> Requests
        </button>
        <button className="nav-item" onClick={() => navigate('/donor-requests')}>
          <Map size={19} /> Map
        </button>
        <button className="nav-item" onClick={() => navigate('/notifications')}>
          <User size={19} /> Profile
        </button>
      </div>
    </div>
  );
}

export default Home;
