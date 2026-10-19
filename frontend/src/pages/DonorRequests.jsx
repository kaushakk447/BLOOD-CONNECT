import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { donorAPI } from '../services/api';
import { ArrowLeft, MapPin, CheckCircle, XCircle, ClipboardList } from '../components/icons';

const bloodLabel = (code) => (code || '').replace('_POSITIVE', '+').replace('_NEGATIVE', '-');

function DonorRequests() {
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [responding, setResponding] = useState(null);
  const [refreshing, setRefreshing] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    loadRequests();
  }, []);

  const loadRequests = async () => {
    try {
      const response = await donorAPI.getPendingRequests();
      setRequests(response.data);
      setError('');
    } catch (err) {
      setError('Failed to load blood requests');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleAccept = async (requestId) => {
    setResponding(requestId);
    try {
      await donorAPI.acceptRequest(requestId);
      loadRequests();
    } catch (err) {
      console.error(err);
    } finally {
      setResponding(null);
    }
  };

  const handleDecline = async (requestId) => {
    setResponding(requestId);
    try {
      await donorAPI.declineRequest(requestId);
      loadRequests();
    } catch (err) {
      console.error(err);
    } finally {
      setResponding(null);
    }
  };

  const handleRefresh = async () => {
    setRefreshing(true);
    try {
      await loadRequests();
    } finally {
      setRefreshing(false);
    }
  };

  if (loading) return <div className="page"><div className="container"><p className="no-data">Loading...</p></div></div>;

  return (
    <div className="page donor-requests-page">
      <div className="top-bar">
        <button className="top-bar-back" onClick={() => navigate('/')}>
          <ArrowLeft size={18} /> Back
        </button>
        <h1>Requests For You</h1>
        <button className="btn btn-secondary" onClick={handleRefresh} disabled={refreshing}>
          {refreshing ? 'Refreshing...' : 'Refresh'}
        </button>
      </div>

      <div className="container">
        {error && <div className="error-message">{error}</div>}

        {requests.length > 0 ? (
          <div className="requests-list">
            {requests.map((request) => (
              <div key={request.id} className="request-card-detailed">
                <div className="request-header">
                  <div style={{ display: 'flex', gap: 8 }}>
                    <span className="blood-type">{bloodLabel(request.bloodRequest.bloodGroup)}</span>
                    <span className={`urgency ${request.bloodRequest.urgency}`}>
                      {request.bloodRequest.urgency}
                    </span>
                  </div>
                  {request.distanceKm && (
                    <span className="distance"><MapPin size={13} /> {request.distanceKm.toFixed(1)} km away</span>
                  )}
                </div>

                <div className="request-info">
                  <p><strong>Location:</strong> {request.bloodRequest.location}</p>
                  <p><strong>Units Needed:</strong> {request.bloodRequest.unitsRequired}</p>
                  <p><strong>Contact:</strong> {request.bloodRequest.contactNumber}</p>
                  <p>
                    <strong>Required by:</strong>{' '}
                    {new Date(request.bloodRequest.requiredDatetime).toLocaleString()}
                  </p>
                  {request.bloodRequest.description && (
                    <p><strong>Notes:</strong> {request.bloodRequest.description}</p>
                  )}
                </div>

                <div className="request-actions">
                  <button
                    className="btn btn-primary"
                    onClick={() => handleAccept(request.id)}
                    disabled={responding === request.id}
                  >
                    <CheckCircle size={15} /> {responding === request.id ? '...' : 'Accept'}
                  </button>
                  <button
                    className="btn btn-secondary"
                    onClick={() => handleDecline(request.id)}
                    disabled={responding === request.id}
                  >
                    <XCircle size={15} /> {responding === request.id ? '...' : 'Decline'}
                  </button>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="no-data">
            <ClipboardList size={30} style={{ opacity: 0.35, marginBottom: 10 }} />
            <p>No blood requests matching your profile yet.</p>
            <p>Make sure your donor profile is verified and availability is enabled.</p>
            <button className="btn btn-secondary" onClick={() => navigate('/donor-registration')}>
              Update Donor Profile
            </button>
          </div>
        )}
      </div>
    </div>
  );
}

export default DonorRequests;
