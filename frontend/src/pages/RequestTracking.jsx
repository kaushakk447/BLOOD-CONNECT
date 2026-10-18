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
  const [notFound, setNotFound] = useState(false);
  const [cancelling, setCancelling] = useState(false);
  const [retrying, setRetrying] = useState(false);

  useEffect(() => {
    let active = true;
    let interval;
    const refresh = async () => {
      try {
        const response = await bloodRequestAPI.getById(id);
        if (!active) return;
        setRequest(response.data);
        setError('');
        setNotFound(false);
        if (!['FULFILLED', 'CANCELLED', 'EXPIRED'].includes(response.data.status)) {
          interval = setTimeout(refresh, 10000);
        }
      } catch (err) {
        if (!active) return;
        setNotFound(err.response?.status === 404);
        setError(err.response?.status === 404 ? 'This request could not be found.' : 'We could not refresh this request right now.');
      } finally {
        if (active) setLoading(false);
      }
    };
    refresh();
    return () => {
      active = false;
      clearTimeout(interval);
    };
  }, [id]);

  const loadRequest = async () => {
    try {
      const response = await bloodRequestAPI.getById(id);
      setRequest(response.data);
      setError('');
      setNotFound(false);
    } catch (err) {
      setNotFound(err.response?.status === 404);
      setError(err.response?.status === 404 ? 'This request could not be found.' : 'We could not refresh this request right now.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleRetryMatching = async () => {
    setRetrying(true);
    try {
      await bloodRequestAPI.retryMatching(id);
      await loadRequest();
    } catch (err) {
      setError(err.response?.data?.message || 'Matching could not be retried right now.');
    } finally {
      setRetrying(false);
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
        <div className="container empty-state">
          <h2>{notFound ? 'Request not found' : 'Unable to load request'}</h2>
          <p className="no-data">{error || 'This request is unavailable.'}</p>
          {!notFound && <button className="btn btn-primary" onClick={loadRequest}>Retry</button>}
          <button className="btn btn-secondary" onClick={() => navigate('/')}>Back to Requests</button>
        </div>
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

        {error && <div className="error-message">{error}</div>}

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
              'A donor has responded. Check notifications for the response details.'}
            {request.status === 'FULFILLED' && 'Your blood request is fulfilled!'}
            {request.status === 'CANCELLED' && 'Request cancelled.'}
            {request.status === 'EXPIRED' && 'Request expired.'}
          </p>
        </div>

        <button className="btn btn-secondary btn-block" onClick={loadRequest} style={{ marginBottom: isCancellable ? 14 : 0 }}>
          <RefreshCw size={16} /> Refresh Status
        </button>

        {request.status === 'SEARCHING' && (
          <button className="btn btn-outline btn-block" onClick={handleRetryMatching} disabled={retrying} style={{ marginBottom: 14 }}>
            <RefreshCw size={16} /> {retrying ? 'Retrying match…' : 'Retry donor matching'}
          </button>
        )}

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
