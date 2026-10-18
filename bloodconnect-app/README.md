# BloodConnect - Emergency Blood Donation Platform

A full-stack application connecting patients needing blood with nearby verified donors.

## Architecture Overview

```
Frontend (React)
    ↓↑
REST API (Spring Boot)
    ↓↑
Database (PostgreSQL)
```

## Quick Start (Docker)

### Prerequisites
- Docker & Docker Compose
- Git

### 1. Start the Application

```bash
cd .docker
docker-compose up --build
```

This will:
- Start PostgreSQL database
- Run database migrations (Flyway)
- Start Spring Boot backend (http://localhost:8080)
- Database: http://localhost:5432

### 2. Start Frontend (in another terminal)

```bash
cd frontend
npm install
npm run dev
```

Frontend will be available at: http://localhost:3000

### 3. Access the Application

1. Open http://localhost:3000
2. Register a new account:
   - Choose role: **Patient**, **Donor**, or **Hospital**
   - Fill in your details
3. Login with your credentials

## Manual Setup (Without Docker)

### Prerequisites
- Java 17+
- Maven 3.9+
- PostgreSQL 14+
- Node.js 18+
- npm

### 1. Setup PostgreSQL Database

```bash
# Create database and user
psql -U postgres
CREATE DATABASE bloodconnect;
CREATE USER bloodconnect_user WITH PASSWORD 'bloodconnect_pass';
GRANT ALL PRIVILEGES ON DATABASE bloodconnect TO bloodconnect_user;
```

### 2. Start Backend

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

Backend will run on: http://localhost:8080/api

### 3. Start Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend will run on: http://localhost:3000

## API Endpoints

### Authentication
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login
- `POST /api/auth/refresh` - Refresh token

### Blood Requests
- `POST /api/blood-requests` - Create blood request
- `GET /api/blood-requests` - Get user's requests
- `GET /api/blood-requests/all` - Get all open requests
- `GET /api/blood-requests/{id}` - Get request details
- `PATCH /api/blood-requests/{id}` - Update request

### Donors
- `POST /api/donors/profile` - Create donor profile
- `GET /api/donors/profile` - Get donor profile
- `PATCH /api/donors/availability` - Update availability
- `GET /api/donors/requests` - Get donor requests
- `GET /api/donors/requests/pending` - Get pending requests
- `POST /api/donors/requests/{id}/accept` - Accept request
- `POST /api/donors/requests/{id}/decline` - Decline request

### Notifications
- `GET /api/notifications` - Get all notifications
- `GET /api/notifications/unread` - Get unread notifications
- `PATCH /api/notifications/{id}/read` - Mark as read
- `PATCH /api/notifications/read-all` - Mark all as read

## Core Flow

### 1. Blood Request Creation
```
Patient creates blood request
    ↓
Backend validates request
    ↓
Matching service finds compatible donors
    ↓
Donors are notified automatically
    ↓
Donor can accept/decline
    ↓
Patient sees responses in real-time
    ↓
Request status updates
```

### 2. Donor Registration
```
Donor registers
    ↓
Donor profile created (PENDING verification)
    ↓
Admin verifies donor
    ↓
Donor becomes eligible for matching
    ↓
Donor receives blood requests
```

## Blood Group Compatibility

The system supports complete blood group compatibility:

| Blood Type | Can Receive From |
|-----------|------------------|
| O-        | O-              |
| O+        | O+, O-          |
| A-        | A-, O-          |
| A+        | A+, A-, O+, O-  |
| B-        | B-, O-          |
| B+        | B+, B-, O+, O-  |
| AB-       | AB-, A-, B-, O- |
| AB+       | All             |

## Database Schema

### Core Tables
- `users` - User accounts (Patient, Donor, Hospital, Admin)
- `donor_profiles` - Donor information
- `hospitals` - Hospital information
- `blood_requests` - Emergency blood requests
- `donor_requests` - Donor-Request matching records
- `notifications` - User notifications
- `audit_logs` - Action audit trail

## Environment Variables

### Backend (.env)
```
JWT_SECRET=your-secret-key-min-256-bits
DB_URL=jdbc:postgresql://localhost:5432/bloodconnect
DB_USERNAME=bloodconnect_user
DB_PASSWORD=bloodconnect_pass
```

### Frontend (.env.production, used by Docker build / .env.local, used by `npm run dev`)
```
VITE_API_URL=/api
```
This is a relative path on purpose: nginx (Docker) and the Vite dev server proxy both forward `/api/*` to the backend, so the same value works in both local dev and containerized deployment without hardcoding a host.

## Key Features

✅ User Authentication (JWT-based)
✅ Role-Based Access Control (Patient, Donor, Hospital, Admin)
✅ Blood Group Matching
✅ Nearby Donor Search (Haversine formula)
✅ Real-time Notifications
✅ Donor Verification Workflow
✅ Request Status Tracking
✅ Audit Logging
✅ Password Hashing (BCrypt)
✅ Input Validation
✅ Global Error Handling
✅ CORS Support
✅ Database Migrations (Flyway)

## Testing the Application

### 1. Register Test Users

**Patient Account:**
- Email: patient@test.com
- Password: password123
- Role: PATIENT

**Donor Account:**
- Email: donor@test.com
- Password: password123
- Role: DONOR

### 2. Test Blood Request Flow

1. Login as Patient
2. Create an emergency blood request:
   - Blood Group: O+
   - Units: 2
   - Urgency: HIGH
   - Location: Chennai
3. View request tracking
4. (In another browser/session) Login as Donor
5. Create donor profile
6. Verify donor in database (set verificationStatus = 'VERIFIED')
7. Set availability to AVAILABLE
8. View blood requests
9. Accept/Decline request
10. Patient sees response

### 3. View Notifications

- Blood request created → Donors notified
- Donor accepts/declines → Patient notified
- Request fulfilled → Both notified

## Database Admin Access

```bash
# Connect to PostgreSQL
psql -U bloodconnect_user -d bloodconnect -h localhost

# View users
SELECT id, email, name, role, account_status FROM users;

# View blood requests
SELECT id, blood_group, units_required, status, created_at FROM blood_requests;

# View donor profiles
SELECT id, user_id, blood_group, verification_status FROM donor_profiles;

# Manually verify a donor (admin task)
UPDATE donor_profiles SET verification_status = 'VERIFIED' WHERE user_id = 2;
```

## Deployment

### Backend Deployment

1. **Build Docker Image:**
```bash
cd backend
mvn clean package
docker build -t bloodconnect-api:1.0.0 .
docker push your-registry/bloodconnect-api:1.0.0
```

2. **Deploy to Kubernetes/Cloud:**
- Use provided docker-compose.yml as reference
- Set environment variables for production
- Use production database (managed service)
- Enable HTTPS
- Set strong JWT secret
- Configure rate limiting

### Frontend Deployment

1. **Build:**
```bash
cd frontend
npm run build
```

2. **Deploy to CDN/Server:**
- Upload `dist` folder to web server
- Configure API URL for production
- Set up proper CORS headers

## Troubleshooting

### Backend won't start
- Check PostgreSQL is running
- Verify database credentials
- Check port 8080 is available
- Review logs for Flyway migration errors

### Frontend won't connect to API
- Check backend is running on 8080
- Verify VITE_API_URL is correct
- Check CORS configuration
- Check browser console for errors

### Donor not receiving notifications
- Verify donor profile is VERIFIED
- Check availability is AVAILABLE
- Verify blood group matches
- Check location coordinates are set

### Blood request not finding donors
- Verify donors exist with compatible blood groups
- Check donor verification status
- Check donor availability status
- Increase search radius

## Security Notes

- All passwords hashed with BCrypt
- JWT tokens expire after 1 hour
- Refresh tokens valid for 7 days
- Never expose passwords or private keys
- All API endpoints require authentication
- Role-based access control enforced
- Input validation on all endpoints
- SQL injection protected (JPA parameterized queries)
- CORS configured for frontend domain only

## Support

For issues or questions, check:
1. Application logs
2. Database logs
3. Browser console errors
4. Network tab in browser DevTools
5. Docker logs: `docker-compose logs -f`

## License

MIT License - Feel free to use this project freely.
