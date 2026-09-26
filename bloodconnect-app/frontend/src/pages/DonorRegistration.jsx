import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { donorAPI } from '../services/api';

function DonorRegistration() {
  const [formData, setFormData] = useState({
    bloodGroup: 'O_POSITIVE',
    dateOfBirth: '',
    weightKg: '',
    location: 'Chennai, Tamil Nadu',
    latitude: 13.0827,
    longitude: 80.2707,
    lastDonationDate: '',
    allowLocationSharing: false,
    pushNotificationsEnabled: true,
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);
  const navigate = useNavigate();

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      await donorAPI.createProfile({
        ...formData,
        weightKg: parseFloat(formData.weightKg),
        latitude: parseFloat(formData.latitude),
        longitude: parseFloat(formData.longitude),
      });
      setSuccess(true);
      alert('Donor profile created! Awaiting admin verification.');
      setTimeout(() => navigate('/'), 2000);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create donor profile');
    } finally {
      setLoading(false);
    }
  };

  if (success) {
    return (
      <div className="page success-page">
        <div className="container">
          <div className="success-message">
            <h2>✅ Profile Created Successfully!</h2>
            <p>Your donor profile is awaiting admin verification.</p>
            <p>Once verified, you'll be able to respond to blood requests.</p>
            <button className="btn btn-primary" onClick={() => navigate('/')}>
              Go to Home
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="page donor-registration-page">
      <div className="top-bar">
        <button onClick={() => navigate('/')}>← Back</button>
        <h1>Become a Life Saver</h1>
      </div>

      <div className="container">
        <div className="donor-intro">
          <h2>❤️ Become a Blood Donor</h2>
          <p>Your blood donation can save up to 3 lives. Help us build a strong network of verified blood donors.</p>
        </div>

        <form onSubmit={handleSubmit}>
          {error && <div className="error-message">{error}</div>}

          <div className="form-section">
            <h3>Personal Details</h3>

            <div className="form-group">
              <label>Blood Group *</label>
              <select
                name="bloodGroup"
                value={formData.bloodGroup}
                onChange={handleChange}
                required
              >
                <option value="O_NEGATIVE">O-</option>
                <option value="O_POSITIVE">O+</option>
                <option value="A_NEGATIVE">A-</option>
                <option value="A_POSITIVE">A+</option>
                <option value="B_NEGATIVE">B-</option>
                <option value="B_POSITIVE">B+</option>
                <option value="AB_NEGATIVE">AB-</option>
                <option value="AB_POSITIVE">AB+</option>
              </select>
            </div>

            <div className="form-group">
              <label>Date of Birth *</label>
              <input
                type="date"
                name="dateOfBirth"
                value={formData.dateOfBirth}
                onChange={handleChange}
                required
              />
            </div>

            <div className="form-group">
              <label>Weight (kg) *</label>
              <input
                type="number"
                name="weightKg"
                value={formData.weightKg}
                onChange={handleChange}
                min="40"
                placeholder="Minimum 40 kg"
                required
              />
            </div>

            <div className="form-group">
              <label>Location *</label>
              <input
                type="text"
                name="location"
                value={formData.location}
                onChange={handleChange}
                placeholder="Your location/city"
                required
              />
            </div>

            <div className="form-group">
              <label>Last Donation Date</label>
              <input
                type="date"
                name="lastDonationDate"
                value={formData.lastDonationDate}
                onChange={handleChange}
              />
            </div>
          </div>

          <div className="form-section">
            <h3>Privacy & Notifications</h3>

            <div className="form-group checkbox">
              <input
                type="checkbox"
                name="allowLocationSharing"
                checked={formData.allowLocationSharing}
                onChange={handleChange}
              />
              <label>Allow location sharing for donor matching</label>
            </div>

            <div className="form-group checkbox">
              <input
                type="checkbox"
                name="pushNotificationsEnabled"
                checked={formData.pushNotificationsEnabled}
                onChange={handleChange}
              />
              <label>Enable push notifications for blood requests</label>
            </div>
          </div>

          <div className="info-box">
            <p>
              📋 Your profile will be verified by our admin team before you can accept blood requests.
            </p>
          </div>

          <button type="submit" className="btn btn-primary btn-block" disabled={loading}>
            {loading ? 'Creating Profile...' : 'Complete Registration'}
          </button>
        </form>
      </div>
    </div>
  );
}

export default DonorRegistration;
