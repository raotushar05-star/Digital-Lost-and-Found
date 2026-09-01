# Digital Lost & Found Network

A full-stack civic service connecting citizens and police stations for lost-and-found
property: citizen reporting, police custody intake and verification, smart matching,
ownership claims with evidence, fraud-prevention disputes, and a documented handover —
built from the frozen project specification (`01`–`08` spec documents).

```
project/
├── backend/    Spring Boot 3 / Java 17+ REST API
├── frontend/   React 18 + Vite single-page app
└── docker-compose.yml   Local PostgreSQL for development
```

## Architecture

- **Backend:** Spring Boot 3.3, Spring Security (JWT, stateless), Spring Data JPA,
  PostgreSQL, Flyway migrations, Lombok. Package layout: `entity` → `repository` →
  `service` (business rules) → `controller` (REST endpoints), plus `dto`, `mapper`,
  `security`, `exception`, `config`, `util`.
- **Database:** 18 tables exactly as specified in `03_Database_Specification.docx`
  (`backend/src/main/resources/db/migration/V1__init_schema.sql`), with a small
  reference-data seed (`V2__seed_reference_data.sql`) for categories and a demo
  police station.
- **Frontend:** React Router, Axios (JWT bearer interceptor), React-Leaflet +
  OpenStreetMap tiles for the map/location module, Bootstrap 5 as the base
  framework with a custom "civic ledger" design layer on top (see
  `frontend/src/index.css`).

## Roles

The system has four roles (frozen in the schema's `users.role` check constraint):

| Role | Can do |
|---|---|
| `USER` | Report lost items, submit finder reports, browse/search, submit claims + evidence |
| `POLICE_OFFICER` | Intake & verify found items, review claims, verify ownership, raise disputes, record handovers |
| `POLICE_ADMIN` | Everything an officer can, plus station-level reports |
| `SYSTEM_ADMIN` | Manage police stations and item categories, view cross-station reports |

**Note:** the API contract only defines a public `/auth/register` endpoint, which always
creates a `USER` account. It does not define an endpoint for provisioning police or
admin accounts — those are institutional accounts, not self-service signups. This
build seeds one of each (see below) so you can log in and exercise every role
immediately; if you need real account provisioning, you'll want to add an
admin-only user-management endpoint (deliberately not invented here, since it isn't
in the frozen contract).

## Prerequisites

- Java 17+, Maven 3.9+ (or use your IDE's bundled Maven)
- Node.js 18+ and npm
- PostgreSQL 14+ (or Docker, for the provided `docker-compose.yml`)

## Running it locally

### 1. Start PostgreSQL

```bash
cd project
docker compose up -d
```

This starts Postgres on `localhost:5432` with database `lostandfound` / user
`postgres` / password `postgres`. (No Docker? Point the backend at any Postgres 14+
instance instead — see environment variables below.)

### 2. Run the backend

```bash
cd project/backend
mvn spring-boot:run
```

On first boot, Flyway creates the schema and seed data, and a `DataSeeder`
component creates these accounts if they don't already exist:

| Role | Email | Password |
|---|---|---|
| System Admin | `admin@lostandfound.local` | `Admin@12345` |
| Police Admin (demo station) | `station.admin@lostandfound.local` | `Police@12345` |
| Police Officer (demo station) | `officer@lostandfound.local` | `Police@12345` |

The API is served at `http://localhost:8080/api/v1`.

### 3. Run the frontend

```bash
cd project/frontend
npm install
npm run dev
```

Visit `http://localhost:5173`. The dev server proxies `/api` to the backend
(see `vite.config.js`), and `VITE_API_BASE_URL` can be set in a `.env` file
(see `.env.example`) if you deploy the two apart.

## Environment variables (backend)

| Variable | Default | Purpose |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/lostandfound` | JDBC connection string |
| `DB_USERNAME` / `DB_PASSWORD` | `postgres` / `postgres` | DB credentials |
| `JWT_SECRET` | dev-only placeholder | **Change in production.** HS256 signing key |
| `JWT_EXPIRATION_MS` | `3600000` (1 hour) | Access token lifetime |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173,http://localhost:3000` | Comma-separated allowed origins |
| `UPLOAD_DIR` | `./uploads` | Local disk path for uploaded photos/evidence files |
| `UPLOAD_BASE_URL` | `/api/v1/files` | Public URL prefix under which uploads are served |
| `SEED_ADMIN_EMAIL` / `SEED_ADMIN_PHONE` / `SEED_ADMIN_PASSWORD` | see above | Override the seeded SYSTEM_ADMIN credentials |

## Design notes & judgment calls

The functional spec (modules, schema, API contract, dev rules) is implemented as
frozen. A few implementation details weren't pinned down by the spec and required
a reasonable, documented choice:

- **Matching** runs automatically (a new lost item is matched against verified
  found items, and vice versa on verification) *and* is exposed as an explicit
  `POST /matches/generate` endpoint for police/admin to re-run in bulk. Scoring
  combines category (required), date proximity, geographic distance (haversine,
  computed in-app — no PostGIS dependency), color/brand equality, and description
  keyword overlap, with a configurable minimum threshold (`app.matching.minimum-score`).
- **Case status** follows the progression named in the UI spec's status system
  and the dev-rules workflow diagram (`REPORTED → RECEIVED → POLICE_VERIFIED →
  POTENTIAL_MATCH → CLAIM_SUBMITTED → UNDER_VERIFICATION → APPROVED/REJECTED →
  RETURNED → RESOLVED`), tracked per-case in `case_status_history`.
- **Competing claims:** approving one claim on a found item automatically
  closes out any other pending/under-review claims on the same item, per the
  frozen fraud-prevention rule that a found item can carry multiple claims.
- **Search geo-filtering** (`/found-items`, `/found-items/nearby`) is computed
  in-application rather than via a spatial database extension, which keeps the
  stack to exactly what `06_Tech_Stack_and_Architecture.docx` specifies.
- Role-gating on "Police."-only endpoints (per the API contract's own wording)
  excludes `SYSTEM_ADMIN`; endpoints explicitly marked "Police/Admin" or
  "POLICE_ADMIN, SYSTEM_ADMIN" include it. This distinction is preserved
  consistently in both the backend `@PreAuthorize` rules and the frontend route
  guards.

## Troubleshooting

- **`Could not transfer artifact ... No such host is known (repo.maven.apache.org)`**
  — Maven can't reach the internet. This is a network/DNS problem on your
  machine, not a project issue: confirm general internet access works, check
  whether a school/office network is blocking Maven Central (try a phone
  hotspot to isolate this), and check for a required corporate proxy
  (Maven needs its own proxy config in `~/.m2/settings.xml` even if your
  browser works fine).
- **`error: release version 21 not supported`** — your installed JDK is older
  than the compiler's target release. Run `java -version` to check. This
  project targets Java 17 (Spring Boot 3.3's minimum), so any JDK 17 or newer
  works; if you have something older, install a JDK 17+ (Eclipse Temurin is a
  good free option) and make sure `JAVA_HOME`/your IDE points at it.

## Known limitations

- No automated test suite is included (out of scope for this pass).
- No CI/CD, containerized deployment for the backend/frontend themselves (only
  the database has a compose file), or production-grade secrets management —
  add these before any real deployment.
- File uploads are stored on local disk (`UPLOAD_DIR`), suitable for development;
  swap in object storage (S3-compatible) for production.
- This code has not been compiled/run in the environment it was authored in (no
  network access to fetch Maven/npm dependencies there); it has been reviewed
  with static consistency checks (DTO field names, enum values, import
  resolution) but you should run `mvn compile` and `npm run build` locally as a
  first step.
