import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { adminAPI } from '../services/api';

function AdminDonors() {
  const [donors, setDonors] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [acting, setActing] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    loadPendingDonors();
  }, []);

  const loadPendingDonors = async () => {
    try {
      const response = await adminAPI.getPendingDonors();
      setDonors(response.data);
      setError('');
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load pending donors');
    } finally {
      setLoading(false);
    }
  };

  const handleVerify = async (id, verified) => {
    setActing(id);
    try {
      await adminAPI.verifyDonor(id, verified);
      loadPendingDonors();
    } catch (err) {
      alert('Action failed: ' + (err.response?.data?.message || 'Unknown error'));
    } finally {
      setActing(null);
    }
  };

  if (loading) return <div className="page"><p>Loading...</p></div>;

  return (
    <div className="page admin-donors-page">
      <div className="top-bar">
        <button onClick={() => navigate('/')}>← Back</button>
        <h1>Verify Donors</h1>
      </div>

      <div className="container">
        {error && <div className="error-message">{error}</div>}

        {donors.length > 0 ? (
          <div className="requests-list">
            {donors.map((donor) => (
              <div key={donor.id} className="request-card-detailed">
                <div className="request-header">
                  <span className={`blood-type ${donor.bloodGroup}`}>{donor.bloodGroup}</span>
                  <span className="urgency LOW">{donor.verificationStatus}</span>
                </div>
                <div className="request-info">
                  <p><strong>Name:</strong> {donor.user?.name}</p>
                  <p><strong>Email:</strong> {donor.user?.email}</p>
                  <p><strong>Phone:</strong> {donor.user?.phone}</p>
                  <p><strong>Location:</strong> {donor.location}</p>
                  <p><strong>Weight:</strong> {donor.weightKg} kg</p>
                  <p><strong>DOB:</strong> {donor.dateOfBirth}</p>
                </div>
                <div className="request-actions">
                  <button
                    className="btn btn-primary"
                    onClick={() => handleVerify(donor.id, true)}
                    disabled={acting === donor.id}
                  >
                    {acting === donor.id ? '...' : '✅ Verify'}
                  </button>
                  <button
                    className="btn btn-secondary"
                    onClick={() => handleVerify(donor.id, false)}
                    disabled={acting === donor.id}
                  >
                    {acting === donor.id ? '...' : '❌ Reject'}
                  </button>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="no-data">
            <p>✅ No donors pending verification</p>
          </div>
        )}
      </div>
    </div>
  );
}

export default AdminDonors;
