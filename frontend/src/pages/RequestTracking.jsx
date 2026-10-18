import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { bloodRequestAPI } from '../services/api';
import {
  ArrowLeft, MapPin, CheckCircle, Clock, RefreshCw,
} from '../components/icons';

const bloodLabel = (code) => (code || '').replace('_POSITIVE', '+').replace('_NEGATIVE', '-');

function RequestTracking() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [request, setRequest] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [cancelling, setCancelling] = useState(false);

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

  const handleCancel = async () => {
    if (!window.confirm('Cancel this blood request?')) return;
    setCancelling(true);
    try {
      await bloodRequestAPI.update(id, { status: 'CANCELLED' });
      loadRequest();
    } catch (err) {
      console.error(err);
    } finally {
      setCancelling(false);
    }
  };

  if (loading) return <div className="page"><div className="container"><p className="no-data">Loading...</p></div></div>;
  if (!request)
    return (
      <div className="page">
        <div className="container"><p className="no-data">{error || 'Request not found'}</p></div>
      </div>
    );

  const progressStages = [
    { stage: 'OPEN', label: 'Created' },
    { stage: 'SEARCHING', label: 'Searching', sub: `Radius established near ${request.location}` },
    { stage: 'DONORS_NOTIFIED', label: 'Donors Notified', sub: 'Waiting for initial responses' },
    { stage: 'DONOR_RESPONDED', label: 'Donor Responded' },
    { stage: 'FULFILLED', label: 'Connected' },
  ];

  const getCurrentStageIndex = () => {
    const stageIndex = progressStages.findIndex((s) => s.stage === request.status);
    return Math.max(0, stageIndex);
  };

  const currentStageIndex = getCurrentStageIndex();
  const isCancellable = !['FULFILLED', 'CANCELLED', 'EXPIRED'].includes(request.status);

  return (
    <div className="page request-tracking-page">
      <div className="top-bar">
        <button className="top-bar-back" onClick={() => navigate('/')}>
          <ArrowLeft size={18} /> Back
        </button>
        <h1>BloodConnect</h1>
        <div style={{ width: 38 }} />
      </div>

      <div className="container">
        <span className="urgent-banner"><span className="pulse-dot" />{request.urgency} Request</span>
        <h2 className="tracking-title">Searching for {bloodLabel(request.bloodGroup)}</h2>
        <p className="tracking-subtitle"><MapPin size={14} /> {request.location}</p>

        <div className="stats-section" style={{ marginTop: 18 }}>
          <div className="stat-card">
            <span className="stat-value">{request.donorCountNotified}</span>
            <span className="stat-label">Donors Notified</span>
          </div>
          <div className="stat-card">
            <span className="stat-value">{request.donorCountResponded}</span>
            <span className="stat-label">Responded</span>
          </div>
          <div className="stat-card">
            <span className="stat-value">{request.unitsFulfilled}/{request.unitsRequired}</span>
            <span className="stat-label">Units Filled</span>
          </div>
        </div>

        <div className="form-section">
          <h3>Request Status</h3>
          <div className="progress-tracker-v">
            {progressStages.map((stage, index) => (
              <div
                key={stage.stage}
                className={`progress-step-v ${index <= currentStageIndex ? 'active' : ''} ${
                  stage.stage === request.status ? 'current' : ''
                }`}
              >
                <div className="step-circle-v">
                  {index <= currentStageIndex ? <CheckCircle size={16} /> : <span style={{ width: 6, height: 6, borderRadius: '50%', background: 'currentColor', display: 'block' }} />}
                </div>
                <div className="step-text-v">
                  <div className="step-label-v">{stage.label}</div>
                  {stage.sub && index <= currentStageIndex && <div className="step-sub-v">{stage.sub}</div>}
                </div>
              </div>
            ))}
          </div>
        </div>

        <div className="search-area-card">
          <h3>Active Search Area</h3>
          <div className="radar">
            <div className="radar-ring r1" />
            <div className="radar-ring r2" />
            <div className="radar-ring r3" />
            <div className="radar-center" />
          </div>
          <p className="tracking-subtitle" style={{ marginTop: 12, justifyContent: 'center' }}>
            <Clock size={14} /> Est. response time: ~8 mins
          </p>
        </div>

        <div className="form-section">
          <h3>Request Details</h3>
          <div className="detail-item">
            <span>Request ID</span>
            <strong>#REQ-{String(request.id).padStart(4, '0')}</strong>
          </div>
          <div className="detail-item">
            <span>Blood Group</span>
            <strong>{bloodLabel(request.bloodGroup)}</strong>
          </div>
          <div className="detail-item">
            <span>Units Required</span>
            <strong>{request.unitsRequired} units</strong>
          </div>
          <div className="detail-item">
            <span>Contact</span>
            <strong>{request.contactNumber}</strong>
          </div>
          <div className="detail-item">
            <span>Required by</span>
            <strong>{new Date(request.requiredDatetime).toLocaleString()}</strong>
          </div>
        </div>

        <div className="status-section">
          <p className="status-message">
            {request.status === 'OPEN' && 'Searching for nearby donors...'}
            {request.status === 'SEARCHING' && 'Matching with compatible donors...'}
            {request.status === 'DONORS_NOTIFIED' &&
              'Donors have been notified. Waiting for responses...'}
            {request.status === 'DONOR_RESPONDED' &&
              'A donor has accepted your request!'}
            {request.status === 'FULFILLED' && 'Your blood request is fulfilled!'}
            {request.status === 'CANCELLED' && 'Request cancelled.'}
            {request.status === 'EXPIRED' && 'Request expired - no donors available.'}
          </p>
        </div>

        <button className="btn btn-secondary btn-block" onClick={loadRequest} style={{ marginBottom: isCancellable ? 14 : 0 }}>
          <RefreshCw size={16} /> Refresh Status
        </button>

        {isCancellable && (
          <div style={{ textAlign: 'center' }}>
            <button className="btn-danger-text btn-sm" onClick={handleCancel} disabled={cancelling}>
              {cancelling ? 'Cancelling...' : 'Cancel Emergency Request'}
            </button>
          </div>
        )}
      </div>
    </div>
  );
}

export default RequestTracking;
