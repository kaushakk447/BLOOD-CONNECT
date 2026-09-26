import { useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { useAuthStore } from './stores/authStore';
import Login from './pages/Login';
import Register from './pages/Register';
import Home from './pages/Home';
import CreateBloodRequest from './pages/CreateBloodRequest';
import RequestTracking from './pages/RequestTracking';
import DonorRegistration from './pages/DonorRegistration';
import DonorRequests from './pages/DonorRequests';
import Notifications from './pages/Notifications';
import AdminDonors from './pages/AdminDonors';
import './App.css';

function App() {
  const { user, hydrate } = useAuthStore();

  useEffect(() => {
    hydrate();
  }, [hydrate]);

  return (
    <Router>
      <Routes>
        {!user ? (
          <>
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="*" element={<Navigate to="/login" />} />
          </>
        ) : (
          <>
            <Route path="/" element={<Home />} />
            <Route path="/create-request" element={<CreateBloodRequest />} />
            <Route path="/request/:id" element={<RequestTracking />} />
            <Route path="/donor-registration" element={<DonorRegistration />} />
            <Route path="/donor-requests" element={<DonorRequests />} />
            <Route path="/notifications" element={<Notifications />} />
            <Route path="/admin/donors" element={<AdminDonors />} />
            <Route path="*" element={<Navigate to="/" />} />
          </>
        )}
      </Routes>
    </Router>
  );
}

export default App;
