# Dormitory Management System

JavaFX desktop application for dormitory admission workflow, including authentication, student application flow, and admin approval with room allocation.

## Stack
- Java 21
- JavaFX (FXML)
- PostgreSQL
- JDBC
- Maven
- JPMS (`module-info.java`)

## Implemented Scope (MVP)

### Activity 1: Foundation + Auth
- Layered project structure (`model`, `dao`, `service`, `controller`, `util`)
- PostgreSQL connectivity via `database.properties` or environment variables
- Startup schema initialization from `src/main/resources/db/init.sql`
- Login/register with role-based routing (`ADMIN`, `USER`)
- Password hashing for new registrations and legacy plaintext auto-migration on login

### Activity 2: Student Application Module
- Student dashboard with profile form (faculty, year, gender, phone, city)
- Profile persistence (`student_profiles`)
- Application submission (`applications`) with rule enforcement for active requests
- Application status tab for students

### Activity 3: Admin Review + Room Allocation
- Admin dashboard listing pending applications
- Approve/reject actions
- Room allocation on approval with occupancy and status update (`FREE`, `PARTIAL`, `FULL`)
- Seed dormitories/rooms for immediate testing

## Run (CLI)
```bash
mvn clean javafx:run
```

If Maven is not installed globally, run through your Maven Wrapper from another project or install Maven first.

Preferred (this project includes a local wrapper and JDK 21):

Windows (PowerShell/CMD):
```powershell
.\mvnw.cmd clean javafx:run
```

Git Bash:
```bash
./mvnw clean javafx:run
```

## Run Database With Docker
Start PostgreSQL and pgAdmin:
```bash
docker compose up -d
```

Stop containers:
```bash
docker compose down
```

Reset database volume (will rerun init.sql on next startup):
```bash
docker compose down -v
```

Services:
- PostgreSQL: localhost:5432
- pgAdmin: http://localhost:5050
	- Email: admin@dormitory.local
	- Password: admin

Default admin login in application:
- Email: `admin@dormitory.local`
- Password: `admin123`

## Run (IntelliJ)
1. Open as Maven project.
2. Let IntelliJ import dependencies.
3. Use the shared run configuration `Run JavaFX (Maven)` (from `.run/`) or run Maven goal `javafx:run`.

If you run `com.dormitory.management.DormitoryApp` as a plain Application config, IntelliJ may show:
`JavaFX runtime components are missing`.
Use Maven run instead so JavaFX modules/native libraries are added automatically.

## Database Configuration
Default config file:
- `src/main/resources/config/database.properties`

Environment variables override file values:
- `DORMITORY_DB_URL`
- `DORMITORY_DB_USER`
- `DORMITORY_DB_PASSWORD`

At startup, the application verifies database connectivity and initializes the core authentication schema (`users` table + default admin seed) if missing.

Now startup initializes the full MVP schema and seed data from `init.sql`.

## MVP User Flow

### Student Flow
1. Register a new USER account.
2. Login as that user.
3. In `Profile` tab, save student profile.
4. In `Application` tab, submit dormitory application.
5. In `Status` tab, track state (`PENDING`, `ACCEPTED`, `REJECTED`).

### Admin Flow
1. Login as admin (`admin@dormitory.local`).
2. Open pending applications list.
3. Select an application.
4. Select an assignable room.
5. Approve or reject.

## Remaining Work
- Activity 4: complaints, tickets/QR, notifications
- Activity 5: full test suite, hardening, and final delivery documentation
