import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { donorAPI } from '../services/api';
import { useAuthStore } from '../stores/authStore';
import { ArrowLeft, Heart, Shield, CheckCircle, CreditCard } from '../components/icons';

function DonorRegistration() {
  const { user } = useAuthStore();
  const [formData, setFormData] = useState({
    bloodGroup: 'O_POSITIVE',
    dateOfBirth: '',
    weightKg: '',
    location: '',
    latitude: '',
    longitude: '',
    lastDonationDate: '',
    allowLocationSharing: false,
    pushNotificationsEnabled: true,
    availableForEmergencies: false,
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [locating, setLocating] = useState(false);
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
    if (formData.latitude === '' || formData.longitude === '') {
      setError('Use your current location before saving so we can match nearby requests accurately.');
      return;
    }
    setLoading(true);

    try {
      await donorAPI.createProfile({
        ...formData,
        weightKg: parseFloat(formData.weightKg),
        latitude: parseFloat(formData.latitude),
        longitude: parseFloat(formData.longitude),
      });
      setSuccess(true);
      setTimeout(() => navigate('/'), 2000);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create donor profile');
    } finally {
      setLoading(false);
    }
  };

  const useCurrentLocation = () => {
    if (!navigator.geolocation) {
      setError('Your browser does not support location. Please use a device or browser that does.');
      return;
    }
    setLocating(true);
    navigator.geolocation.getCurrentPosition(
      ({ coords }) => {
        setFormData((prev) => ({ ...prev, latitude: coords.latitude, longitude: coords.longitude }));
        setError('');
        setLocating(false);
      },
      () => {
        setError('We could not access your location. Allow location access and try again.');
        setLocating(false);
      },
      { enableHighAccuracy: true, timeout: 10000, maximumAge: 60000 }
    );
  };

  if (success) {
    return (
      <div className="page success-page">
        <div className="container">
          <div className="success-message">
            <div className="success-icon"><CheckCircle size={30} /></div>
            <h2>Profile Created Successfully!</h2>
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
        <button className="top-bar-back" onClick={() => navigate('/')}>
          <ArrowLeft size={18} /> Back
        </button>
        <h1>Donor Registration</h1>
        <div style={{ width: 38 }} />
      </div>

      <div className="container">
        <div className="donor-id-card">
          <div className="donor-id-top">
            <div>
              <div className="donor-id-eyebrow">Donor Profile</div>
              <div className="donor-id-name">{user?.name || 'New Donor'}</div>
            </div>
            <span className="donor-id-pending">Pending</span>
          </div>
          <div className="donor-id-bottom">
            <span className="donor-id-number">ID · XXXX-XXXX-XXXX</span>
            <CreditCard size={22} style={{ opacity: 0.55 }} />
          </div>
        </div>

        <div className="donor-intro">
          <h2><Heart size={18} /> Become a Life Saver</h2>
          <p>Complete your profile to join our verified network of heroes. Your blood donation can save up to 3 lives.</p>
        </div>

        <form onSubmit={handleSubmit}>
          {error && <div className="error-message">{error}</div>}

          <div className="form-section">
            <h3>Personal Details</h3>

            <div className="form-row">
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
            </div>

            <div className="form-row">
              <div className="form-group">
                <label>Weight (kg) *</label>
                <input
                  type="number"
                  name="weightKg"
                  value={formData.weightKg}
                  onChange={handleChange}
                  min="40"
                  placeholder="e.g., 65"
                  required
                />
              </div>

              <div className="form-group">
                <label>Last Donation</label>
                <input
                  type="date"
                  name="lastDonationDate"
                  value={formData.lastDonationDate}
                  onChange={handleChange}
                />
              </div>
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

            <button type="button" className="btn btn-secondary" onClick={useCurrentLocation} disabled={locating}>
              {locating ? 'Finding location…' : formData.latitude === '' ? 'Use current location for matching' : 'Location captured — update'}
            </button>
          </div>

          <div className="info-box">
            <Shield size={17} />
            <span>To ensure safety, you must complete a brief medical history questionnaire and present a valid ID at your first donation center visit.</span>
          </div>

          <div className="form-section">
            <h3>Availability &amp; Privacy</h3>

            <div className="form-group checkbox">
              <input
                type="checkbox"
                name="availableForEmergencies"
                checked={formData.availableForEmergencies}
                onChange={handleChange}
              />
              <label>Available for emergencies — once your profile is verified, include me in compatible local matches</label>
            </div>

            <div className="form-group checkbox">
              <input
                type="checkbox"
                name="allowLocationSharing"
                checked={formData.allowLocationSharing}
                onChange={handleChange}
              />
              <label>Allow my approximate location to be used to find nearby requests</label>
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

          <button type="submit" className="btn btn-primary btn-block btn-lg" disabled={loading}>
            {loading ? 'Saving...' : 'Save Profile'}
          </button>
        </form>
      </div>
    </div>
  );
}

export default DonorRegistration;
