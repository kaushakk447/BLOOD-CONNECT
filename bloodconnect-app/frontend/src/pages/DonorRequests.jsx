import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { donorAPI } from '../services/api';

function DonorRequests() {
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [responding, setResponding] = useState(null);
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
      alert('✅ You accepted the blood request!');
      loadRequests();
    } catch (err) {
      alert('❌ Failed to accept request');
      console.error(err);
    } finally {
      setResponding(null);
    }
  };

  const handleDecline = async (requestId) => {
    setResponding(requestId);
    try {
      await donorAPI.declineRequest(requestId);
      alert('Declined the request');
      loadRequests();
    } catch (err) {
      alert('Failed to decline request');
      console.error(err);
    } finally {
      setResponding(null);
    }
  };

  if (loading) return <div className="page"><p>Loading...</p></div>;

  return (
    <div className="page donor-requests-page">
      <div className="top-bar">
        <button onClick={() => navigate('/')}>← Back</button>
        <h1>Blood Requests for You</h1>
      </div>

      <div className="container">
        {error && <div className="error-message">{error}</div>}

        {requests.length > 0 ? (
          <div className="requests-list">
            {requests.map((request) => (
              <div key={request.id} className="request-card-detailed">
                <div className="request-header">
                  <div>
                    <span className={`blood-type ${request.bloodRequest.bloodGroup}`}>
                      {request.bloodRequest.bloodGroup}
                    </span>
                    <span className={`urgency ${request.bloodRequest.urgency}`}>
                      {request.bloodRequest.urgency}
                    </span>
                  </div>
                  <div className="distance">
                    {request.distanceKm && (
                      <span>📍 {request.distanceKm.toFixed(1)} km away</span>
                    )}
                  </div>
                </div>

                <div className="request-info">
                  <p>
                    <strong>Location:</strong> {request.bloodRequest.location}
                  </p>
                  <p>
                    <strong>Units Needed:</strong> {request.bloodRequest.unitsRequired}
                  </p>
                  <p>
                    <strong>Contact:</strong> {request.bloodRequest.contactNumber}
                  </p>
                  <p>
                    <strong>Required by:</strong>{' '}
                    {new Date(request.bloodRequest.requiredDatetime).toLocaleString()}
                  </p>
                  {request.bloodRequest.description && (
                    <p>
                      <strong>Notes:</strong> {request.bloodRequest.description}
                    </p>
                  )}
                </div>

                <div className="request-actions">
                  <button
                    className="btn btn-primary"
                    onClick={() => handleAccept(request.id)}
                    disabled={responding === request.id}
                  >
                    {responding === request.id ? '...' : '✅ Accept'}
                  </button>
                  <button
                    className="btn btn-secondary"
                    onClick={() => handleDecline(request.id)}
                    disabled={responding === request.id}
                  >
                    {responding === request.id ? '...' : '❌ Decline'}
                  </button>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="no-data">
            <p>📭 No blood requests matching your profile yet.</p>
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
