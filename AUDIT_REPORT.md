# BloodConnect audit report

## Scope and source of truth

The root `backend/` and `frontend/` directories are the active application: root
`docker-compose.yml` builds them. The divergent `bloodconnect-app/` copy is not
used by that deployment path and was not changed.

## Donor matching root cause

This was not a frontend-build issue. `MatchingService` only queries donor
profiles whose blood group is compatible **and** whose verification status is
`VERIFIED` **and** availability is `AVAILABLE`. A newly created profile was
always stored as `PENDING` and `UNAVAILABLE`; the UI control labelled
"Available for emergencies" actually changed `allowLocationSharing`, not the
availability value used by matching. Such a donor can therefore never be in the
candidate query before verification/availability are set.

The forms also silently submitted Chennai's fixed coordinates for every donor
and request. That produced false proximity matches in Chennai and no useful
matching elsewhere. They now require explicit browser location capture rather
than silently using unrelated coordinates.

## Repairs made

* The availability control now records `availableForEmergencies`; verification
  remains required and an opted-in verified donor is eligible for the query.
* The patient request form and donor profile form capture browser coordinates,
  with clear location-permission errors. Backend validation rejects out-of-range
  coordinates and invalid radii.
* Matching retries are idempotent for a donor/request pair. A request with a
  donor response cannot be put back into the matching state.
* A donor cannot accept or decline a cancelled, fulfilled, or expired request;
  stale terminal requests are hidden from the pending-donor list. Both accept
  and decline update the patient's request to `DONOR_RESPONDED` and persisted
  response counts. The tracking copy no longer claims every response is an
  acceptance.
* Active-request listing is restricted to admins. A matched donor can view the
  individual request; unrelated users cannot.
* The Nginx SPA fallback remains `try_files $uri $uri/ /index.html`, which is
  the required deployed-server configuration for direct route navigation.

## SMS delivery design

SMS is opt-in (`sms_notifications_consent`, default false), revocable through
`PATCH /api/notifications/sms-consent`, and is never sent by default. Every
notification attempt is persisted in `sms_deliveries` with an event-specific
idempotency key, delivery state (`MOCKED`, `SENT`, `SKIPPED`, or `FAILED`),
provider message ID, returned provider status, error, and attempt count.
`SENT` means Twilio accepted the request; it is not treated as `DELIVERED`.
Duplicate delivery keys are suppressed. The only real provider adapter is
Twilio and is isolated from mock mode; mock mode makes no network calls.

## Validation results

* `frontend`: `npm run build` **passed** (Vite production bundle).
* Backend test coverage covers the request → match → donor inbox → accept →
  patient status flow, consent/mock/disabled/duplicate SMS behaviour, provider
  failure handling, provider-response persistence, missing Twilio
  configuration, and invalid recipient numbers. These tests use mocks or fail
  before the provider call, so they never contact Twilio.
* Backend compilation and tests are **not executed** on this host: it exposes
  Java 8 (`1.8.0_251`) and no Maven, while `pom.xml` requires Java 17. Docker
  is installed but Docker Desktop's Linux engine is stopped, so the configured
  Java 17 build container cannot start.
* The supplied Render frontend and backend health endpoints returned HTTP 200
  during inspection, and direct navigation to `/login` returned the SPA shell.
  This does not verify that the uncommitted changes are deployed or that Twilio
  has accepted a real SMS.

## Commands to run when prerequisites are available

```powershell
# Option 1: install JDK 17 and Maven 3.9+, then
cd backend
mvn clean test
mvn clean package

# Option 2: start Docker Desktop's Linux engine, then from repository root
docker compose build backend
docker compose run --rm backend mvn test
docker compose up --build
```

## Render environment variables

Required backend settings:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
JWT_SECRET                         # high-entropy, 32+ byte secret
APP_CORS_ALLOWED_ORIGINS           # exact frontend HTTPS origin, no wildcard
SMS_ENABLED=true                   # only when ready to send
SMS_MOCK_MODE=false                # keep true for staging/tests
SMS_PROVIDER=twilio
TWILIO_ACCOUNT_SID
TWILIO_AUTH_TOKEN
TWILIO_FROM_NUMBER                 # Twilio sender number, E.164
```

For staging and all automated tests use `SMS_ENABLED=true` with
`SMS_MOCK_MODE=true`, or leave SMS disabled. Never set production Twilio
credentials in frontend environment variables.

## Deployment checklist

1. Provision PostgreSQL, set database/JWT/CORS variables, and deploy the
   backend. Flyway applies `V2__sms_delivery_and_consent.sql` and
   `V3__sms_provider_status.sql` automatically.
2. Deploy the frontend behind the included Nginx configuration and use the
   same-origin `/api` proxy, or set the HTTPS `VITE_API_URL` at build time. A
   direct Render static-site build falls back to
   `https://blood-connect-api-u1hr.onrender.com/api`; the local Docker build
   explicitly keeps the same-origin `/api` proxy.
3. Test login, create one verified and available donor, create a nearby patient
   request, accept it as the donor, then refresh the patient's request route.
4. Start with SMS mock mode, inspect `sms_deliveries`, then enable real Twilio
   only after an explicit consent and provider-account review.

## Intentional production SMS activation and smoke test

Set the following **backend Render service** environment variables privately;
do not add them to the frontend or repository:

```text
SMS_ENABLED=true
SMS_MOCK_MODE=false
SMS_PROVIDER=twilio
TWILIO_ACCOUNT_SID=<Twilio account SID>
TWILIO_AUTH_TOKEN=<rotated Twilio auth token>
TWILIO_FROM_NUMBER=<verified Twilio E.164 sender>
```

After Render deploys the backend and its health endpoint is healthy, an
authorized operator can deliberately create a request matching exactly one
verified, available donor who has opted in to SMS and whose test phone number
is authorized by the Twilio account. Confirm the `sms_deliveries` row has a
Twilio message ID and its returned provider status, then check the Twilio
Console message log and final delivery status. Do not run this procedure in
CI, deployment hooks, or against an unconsenting recipient. If credentials,
sender, consent, or E.164 number are missing, the service persists `FAILED` or
`SKIPPED`; it never switches to mock success.
