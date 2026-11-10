# FindLine: Digital Lost & Found Network

A police-assisted lost-and-found platform. Citizens report lost and found property, police stations take items
into verified custody, and owners claim items with evidence. Every step is recorded, and an officer decides
ownership. Built with a React frontend and a Spring Boot REST API.

> **Status:** academic prototype. It runs locally. It has not been deployed or security-audited.

**Demo video:** _add your unlisted YouTube link here_

| Citizen dashboard | Police claim review | Station management |
|---|---|---|
| ![Citizen dashboard](docs/screenshots/citizen-dashboard.png) | ![Claim review](docs/screenshots/claim-review.png) | ![Stations](docs/screenshots/stations.png) |

## Why police-assisted?

On a notice board or social media, anyone can claim an item and nobody checks. FindLine puts a verification step
in the middle:

1. A finder reports the item, then hands it to a police station.
2. An officer registers and verifies it. Only then does it appear in public search.
3. The owner submits a claim with evidence. An officer approves, rejects, or raises a dispute.
4. The owner collects the item with a one-time code, and a receipt is recorded.

## Features

**Citizens**
- Report lost items and found items, with a map pin. The area is looked up from the pin (OpenStreetMap).
- Duplicate-report warning for your own near-identical reports.
- Search verified items still in police custody (category, city, colour, brand, keywords).
- Automatic match suggestions (category, date, distance, colour, brand, description overlap).
- Submit ownership claims with evidence files. Evidence is private to you and the item's station.
- Handover: generate a one-time code, collect the item, download the receipt PDF.
- In-app notifications, feedback and complaints, case tracking, case PDF download.

**Police officers**
- Station-scoped lost-report queue, found-item intake and verification, photo upload.
- Claim review: approve, reject, or raise a dispute when claims compete.
- Handover with one-time code check, case diary, case-file PDFs (single and bulk).
- Transfer an item to another station with a logged reason.
- Retention tracking (days in custody, overdue filter) and disposal after the retention period.

**System administrator**
- Create police stations with a map pin and a station officer login.
- Manage users (activate, deactivate, unlock), view the audit log, manage item categories.
- Read and reply to feedback and complaints. Cross-station reports.
- Reports that are farther than 30 km from every station stay unassigned. The admin sees how many,
  and they are assigned automatically when a station is created nearby.

**Security**
- JWT authentication, role checks on every endpoint, and a station check so officers only touch their own station's records.
- BCrypt password hashes, login lockout (5 failures, 15 minutes), forgot and change password with single-use expiring tokens.
- Idle sign-out (10 minutes for staff, 30 for citizens).
- Protected evidence and lost-report files, and an audit log of sensitive actions.
- Optional: Google email verification at sign-up, SMS/WhatsApp alerts through Twilio. Both are off by default.

## Roles

| Role | Created by | Can do |
|---|---|---|
| `USER` (citizen) | Self-registration | Report, search, claim, track |
| `POLICE_OFFICER` | System admin (when creating a station) | Everything station-scoped listed above |
| `POLICE_ADMIN` | Demo seed only | Same as an officer, plus the station-wise report table. Not created from the UI |
| `SYSTEM_ADMIN` | Seeded | Stations, users, categories, audit log, feedback, reports |

## Tech stack

| Layer | Technology |
|---|---|
| Backend | Java 17+, Spring Boot 3.3, Spring Security (JWT), Spring Data JPA, Flyway, Lombok, OpenPDF |
| Database | PostgreSQL 16 (Docker) |
| Frontend | React 18, Vite, React Router, Axios, Bootstrap 5, React-Leaflet |
| Tests | JUnit 5 + Mockito (backend), a small Node test runner (frontend) |

```
project/
├── backend/            Spring Boot API (controller → service → repository, plus dto, mapper, security)
├── frontend/           React single-page app
└── docker-compose.yml  Local PostgreSQL
```

## Run it locally

**Prerequisites:** Java 17 or newer, Maven 3.9+, Node 18+, Docker Desktop.

```bash
# 1. Database
docker compose up -d

# 2. Backend (http://localhost:8080)
cd backend
mvn clean spring-boot:run

# 3. Frontend (http://localhost:5173), in a second terminal
cd frontend
npm install
npm run dev
```

Flyway creates the schema and a small demo data set on first start.

### Demo accounts (development only)

| Role | Email | Password |
|---|---|---|
| System admin | `admin@lostandfound.local` | `Admin@12345` |
| Station admin (Barke) | `station.admin@lostandfound.local` | `Police@12345` |
| Officer (Barke) | `officer@lostandfound.local` | `Police@12345` |
| Officer (Cubbon Park) | `cubbon.officer@lostandfound.local` | `Police@12345` |

These are seeded for local demos only. Never reuse them anywhere real.

### Configuration

All settings have local defaults. Override them with environment variables.

| Variable | Default | Purpose |
|---|---|---|
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | local Postgres | Database |
| `JWT_SECRET` | dev placeholder | **Change outside local use** |
| `JWT_EXPIRATION_MS` | `3600000` | Token lifetime |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173,http://localhost:3000` | Allowed browser origins |
| `UPLOAD_DIR`, `UPLOAD_BASE_URL` | `./uploads`, `/api/v1/files` | File storage |
| `ROUTING_MAX_DISTANCE_KM` | `30` | Reports farther than this stay unassigned |
| `RETENTION_DAYS` | `90` | Days in custody before an item is overdue |
| `FRONTEND_URL` | `http://localhost:5173` | Used in password-reset links |
| `MAIL_ENABLED` and `MAIL_*` | off | Email. When off, reset links are printed in the backend log |
| `GOOGLE_CLIENT_ID` (backend) and `VITE_GOOGLE_CLIENT_ID` (frontend) | unset | Google email check. Set both or neither |
| `SMS_ENABLED`, `SMS_CHANNEL`, `TWILIO_*` | off | SMS or WhatsApp alerts |

PowerShell example: `$env:GOOGLE_CLIENT_ID="your-id"; mvn clean spring-boot:run`.

### Tests

```bash
cd backend && mvn test
cd frontend && node test/testRunner.js
```

### Troubleshooting
- **Connection to localhost:5432 refused:** start Docker Desktop, then `docker compose up -d`.
- **Odd `ClassFormatError` or "Unresolved compilation problems":** the editor and Maven both wrote to `backend/target`. Close the editor, delete `target`, and run `mvn clean spring-boot:run`.
- **Red Lombok errors in VS Code but Maven builds fine:** the editor's Java tooling is out of date. Trust the Maven build.

## Main API areas

All under `/api/v1`: `auth`, `found-items` (public search), `lost-items`, `found-reports`, `claims`,
`matches`, `cases`, `notifications`, `feedback`, `police/*` (staff), `admin/*` (system admin).

## Known limitations

- The login token is kept in `localStorage`, and idle sign-out is a browser-side check.
- Lost-report and found-report photos are reachable by anyone with the link. Claim evidence is protected.
- Matching is rule-based and compares typed fields only. It does not look at photos, and its accuracy has not been measured.
- Station routing uses straight-line distance, not road distance or jurisdiction boundaries.
- Place checks (map lookup) run in the browser. The server validates formats only.
- The system admin sets each officer's first password.
- Search and reports filter in memory, which is fine for one city but not a whole state. Uploads sit on local disk.
- The test suite is small and has no end-to-end tests.

## Future work

Forced password change at first login, a district-level supervisor role, signed short-lived file links,
refresh tokens, a public case tracker, database-level paging and spatial queries, and end-to-end tests.

## License

MIT, see `LICENSE`.