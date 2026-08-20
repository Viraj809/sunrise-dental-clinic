# Sunrise Dental Clinic — Patient & Appointment Management System

## Quick Start Guide

### Prerequisites
- Java 17+ (JDK 21 recommended)
- Apache Tomcat 10+ (or GlassFish/Payara)
- MySQL 8.0+
- Maven 3.8+ (or use the included Maven wrapper)

### 1. Database Setup

Run the SQL script to create the database, tables, stored procedures, functions, triggers, and seed data:

```bash
mysql -u root -p < sunrise_dental_db.sql
```

**Default database credentials** (update `DatabaseUtil.java` if yours differ):
- URL: `jdbc:mysql://localhost:3306/sunrise_dental_db?useSSL=false&serverTimezone=UTC`
- User: `root`
- Password: `VIP7788@viraj`

### 2. Build the Application

```bash
# Backend (REST API)
cd Backend
mvn clean package

# Frontend (Web UI)
cd Frontend
mvn clean package
```

This produces:
- `Backend/target/Backend.war`
- `Frontend/target/Frontend.war`

### 3. Deploy to Tomcat

1. Copy `Backend.war` to `$CATALINA_HOME/webapps/Backend.war`
2. Copy `Frontend.war` to `$CATALINA_HOME/webapps/Frontend.war`
3. Start Tomcat

### 4. Access the System

- **Frontend UI**: http://localhost:8080/Frontend/
- **Backend API**: http://localhost:8080/Backend/resources/

### 5. Demo Accounts

| Role | Email | Password |
|------|-------|----------|
| Admin | admin@sunrisedental.com | admin123 |
| Receptionist | reception@sunrisedental.com | reception123 |
| Dentist | perera@sunrisedental.com | dentist123 |

## System Architecture

```
┌──────────────────────┐      HTTP/JSON      ┌───────────────────────┐      JDBC       ┌─────────────────┐
│   Presentation Tier   │  ───────────────►   │     Business/Logic     │  ───────────►   │   Data Tier      │
│  Frontend WAR          │  ◄───────────────   │     Backend WAR         │  ◄───────────   │   MySQL 8.x      │
│  Servlets + HTML/CSS/JS│      (REST calls)   │  JAX-RS Web Services    │                  │ sunrise_dental_db│
│  Port 8080 (context /) │                      │  Port 8080 (/Backend)   │                  │ Port 3306        │
└──────────────────────┘                      └───────────────────────┘                  └─────────────────┘
```

## Features Implemented

### Core Requirements
1. **User Authentication** — Secure login with BCrypt password hashing, role-based access (Admin, Receptionist, Dentist)
2. **Register New Appointment** — Full patient lookup/creation, appointment booking with validation
3. **Display Appointment Details** — Search by appointment number, view complete info
4. **Calculate & Print Bill** — Auto-calculates treatment fee, consultation fee, senior discount (10%), tax (5%), printable receipt
5. **Help Section** — Step-by-step user guide for new staff
6. **Exit System** — Secure logout with session invalidation

### Advanced Features
- **Reports Dashboard** — Daily appointments, revenue, treatment popularity, dentist workload (Chart.js)
- **Patient History** — View all past appointments per patient
- **Dentist Schedule** — Daily schedule view with patient names
- **Notifications** — Observer pattern for Email/SMS/In-app notifications on appointment changes
- **Audit Trail** — Database triggers log all appointment changes
- **Double Booking Prevention** — Enforced both client-side and via DB trigger
- **Validation Chain** — Contact number, date, clinic hours, double booking validation

## Design Patterns Applied

| Pattern | Where Used | Purpose |
|---------|-----------|---------|
| **DAO** | All DAO classes | Isolates database access from business logic |
| **Singleton** | DatabaseUtil | Single shared connection pool |
| **Factory Method** | BillFactory | Creates appropriate bill strategy based on patient age |
| **Strategy** | BillCalculationStrategy | Standard vs Senior citizen billing |
| **Observer** | AppointmentSubject | Notifies Email/SMS/Staff observers on appointment changes |
| **Facade** | ReportFacade | Simplifies complex report queries into single interface |
| **Chain of Responsibility** | AppointmentValidator | Sequential validation pipeline |
| **MVC** | Frontend | HTML (View) + Servlets (Controller) + Model DTOs |

## Database Advanced Features

- **Stored Procedure**: `sp_generate_bill` — auto-calculates and inserts bills
- **Function**: `fn_get_treatment_cost` — returns price with senior discount
- **Triggers**: `trg_appointment_audit_*` — logs all appointment changes
- **Triggers**: `trg_prevent_double_booking_*` — blocks conflicting dentist appointments
- **View**: `vw_daily_schedule` — pre-joined appointment/patient/dentist data

## Troubleshooting

1. **Backend not connecting to MySQL**: Check `DatabaseUtil.java` credentials and ensure MySQL is running
2. **404 errors**: Ensure both WARs are deployed to the same Tomcat instance
3. **CORS issues**: Backend runs on `/Backend/*`, Frontend on `/Frontend/*` — same origin policy is satisfied when deployed on same host:port
4. **Session expired**: Tokens expire after inactivity; re-login

## File Structure

```
SunriseDental/
├── Backend/                          # REST API (JAX-RS)
│   ├── src/main/java/
│   │   ├── DBUtil/                   # Singleton connection pool
│   │   ├── Model/                    # Entity classes
│   │   ├── DAO/                      # Data Access Objects
│   │   ├── Service/                  # Business logic & patterns
│   │   ├── Validation/               # Chain of Responsibility validators
│   │   └── com/mycompany/backend/resources/  # JAX-RS endpoints
│   └── src/main/webapp/WEB-INF/      # Deployment descriptors
├── Frontend/                         # Web UI (Servlets + HTML/CSS/JS)
│   ├── src/main/java/servlets/       # MVC Controllers
│   └── src/main/webapp/              # Views, CSS, JavaScript
└── sunrise_dental_db.sql             # Database schema & seed data
```
