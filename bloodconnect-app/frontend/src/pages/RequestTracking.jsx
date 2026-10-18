import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { bloodRequestAPI } from '../services/api';

function RequestTracking() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [request, setRequest] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    loadRequest();
    const interval = setInterval(loadRequest, 5000); // Poll every 5 seconds
    return () => clearInterval(interval);
  }, [id]);

  const loadRequest = async () => {
    try {
      const response = await bloodRequestAPI.getById(id);
      setRequest(response.data);
      setError('');
    } catch (err) {
      setError('Failed to load request');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <div className="page"><p>Loading...</p></div>;
  if (!request)
    return (
      <div className="page">
        <p>{error || 'Request not found'}</p>
      </div>
    );

  const progressStages = [
    { stage: 'OPEN', label: 'Created', emoji: '📝' },
    { stage: 'SEARCHING', label: 'Searching', emoji: '🔍' },
    { stage: 'DONORS_NOTIFIED', label: 'Donors Notified', emoji: '🔔' },
    { stage: 'DONOR_RESPONDED', label: 'Donor Responded', emoji: '✅' },
    { stage: 'FULFILLED', label: 'Fulfilled', emoji: '🎉' },
  ];

  const getCurrentStageIndex = () => {
    const stageIndex = progressStages.findIndex((s) => s.stage === request.status);
    return Math.max(0, stageIndex);
  };

  const currentStageIndex = getCurrentStageIndex();

  return (
    <div className="page request-tracking-page">
      <div className="top-bar">
        <button onClick={() => navigate('/')}>← Back</button>
        <h1>Track Blood Request</h1>
      </div>

      <div className="container">
        <div className="request-card-large">
          <div className="request-header">
            <span className={`blood-type large ${request.bloodGroup}`}>
              {request.bloodGroup}
            </span>
            <span className={`urgency large ${request.urgency}`}>
              {request.urgency}
            </span>
          </div>

          <div className="request-details">
            <div className="detail-item">
              <span>Units Needed:</span>
              <strong>{request.unitsRequired} units</strong>
            </div>
            <div className="detail-item">
              <span>Location:</span>
              <strong>{request.location}</strong>
            </div>
            <div className="detail-item">
              <span>Contact:</span>
              <strong>{request.contactNumber}</strong>
            </div>
            <div className="detail-item">
              <span>Required by:</span>
              <strong>{new Date(request.requiredDatetime).toLocaleString()}</strong>
            </div>
          </div>
        </div>

        <div className="progress-section">
          <h3>Progress</h3>
          <div className="progress-tracker">
            {progressStages.map((stage, index) => (
              <div
                key={stage.stage}
                className={`progress-step ${
                  index <= currentStageIndex ? 'active' : ''
                } ${stage.stage === request.status ? 'current' : ''}`}
              >
                <div className="step-circle">{stage.emoji}</div>
                <p className="step-label">{stage.label}</p>
              </div>
            ))}
          </div>
        </div>

        <div className="stats-section">
          <div className="stat-card">
            <span className="stat-value">{request.donorCountNotified}</span>
            <span className="stat-label">Donors Notified</span>
          </div>
          <div className="stat-card">
            <span className="stat-value">{request.donorCountResponded}</span>
            <span className="stat-label">Donors Responded</span>
          </div>
          <div className="stat-card">
            <span className="stat-value">{request.unitsFulfilled}/{request.unitsRequired}</span>
            <span className="stat-label">Units Fulfilled</span>
          </div>
        </div>

        <div className="status-section">
          <h3>Request Status</h3>
          <p className="status-message">
            {request.status === 'OPEN' && '⏳ Searching for nearby donors...'}
            {request.status === 'SEARCHING' && '🔍 Matching with compatible donors...'}
            {request.status === 'DONORS_NOTIFIED' &&
              '🔔 Donors have been notified. Waiting for responses...'}
            {request.status === 'DONOR_RESPONDED' &&
              '✅ A donor has accepted your request!'}
            {request.status === 'FULFILLED' && '🎉 Your blood request is fulfilled!'}
            {request.status === 'CANCELLED' && '❌ Request cancelled'}
            {request.status === 'EXPIRED' && '⏰ Request expired - no donors available'}
          </p>
        </div>

        <button className="btn btn-secondary btn-block" onClick={loadRequest}>
          🔄 Refresh Status
        </button>
      </div>
    </div>
  );
}

export default RequestTracking;
