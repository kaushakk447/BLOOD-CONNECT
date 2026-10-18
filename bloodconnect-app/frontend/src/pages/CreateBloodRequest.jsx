import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { bloodRequestAPI } from '../services/api';

function CreateBloodRequest() {
  const [formData, setFormData] = useState({
    bloodGroup: 'O_POSITIVE',
    unitsRequired: 2,
    urgency: 'HIGH',
    location: '',
    latitude: 13.0827,
    longitude: 80.2707, // Chennai default
    contactNumber: '',
    requiredDatetime: '',
    description: '',
    hospitalId: null,
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: name === 'unitsRequired' ? parseInt(value) : value,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const response = await bloodRequestAPI.create(formData);
      alert('Blood request created successfully!');
      navigate(`/request/${response.data.id}`);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create blood request');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page create-request-page">
      <div className="top-bar">
        <button onClick={() => navigate('/')}>← Back</button>
        <h1>Emergency Blood Request</h1>
      </div>

      <div className="container">
        <form onSubmit={handleSubmit}>
          {error && <div className="error-message">{error}</div>}

          <div className="form-section">
            <h3>Patient Details</h3>

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
              <label>Units Required *</label>
              <input
                type="number"
                name="unitsRequired"
                value={formData.unitsRequired}
                onChange={handleChange}
                min="1"
                max="100"
                required
              />
            </div>
          </div>

          <div className="form-section">
            <h3>Hospital Information</h3>

            <div className="form-group">
              <label>Hospital/Location Name *</label>
              <input
                type="text"
                name="location"
                value={formData.location}
                onChange={handleChange}
                placeholder="Hospital name or location"
                required
              />
            </div>
          </div>

          <div className="form-section">
            <h3>Contact & Timeframe</h3>

            <div className="form-group">
              <label>Contact Number *</label>
              <input
                type="tel"
                name="contactNumber"
                value={formData.contactNumber}
                onChange={handleChange}
                placeholder="Emergency contact number"
                required
              />
            </div>

            <div className="form-group">
              <label>Required Date & Time *</label>
              <input
                type="datetime-local"
                name="requiredDatetime"
                value={formData.requiredDatetime}
                onChange={handleChange}
                required
              />
            </div>

            <div className="form-group">
              <label>Urgency Level</label>
              <select name="urgency" value={formData.urgency} onChange={handleChange}>
                <option value="LOW">Low</option>
                <option value="MEDIUM">Medium</option>
                <option value="HIGH">High</option>
                <option value="CRITICAL">Critical</option>
              </select>
            </div>

            <div className="form-group">
              <label>Additional Notes</label>
              <textarea
                name="description"
                value={formData.description}
                onChange={handleChange}
                placeholder="Any additional information"
                rows="3"
              />
            </div>
          </div>

          <button type="submit" className="btn btn-primary btn-block" disabled={loading}>
            {loading ? 'Creating Request...' : 'Create Blood Request'}
          </button>
        </form>
      </div>
    </div>
  );
}

export default CreateBloodRequest;
