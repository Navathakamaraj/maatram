# Maatram – Student Lifecycle Platform (MH26-05)

Full-stack production platform for the Maatram student lifecycle management system, engineered with an industry-standard decoupled architecture:

- **Frontend**: React 19 + Vite 6
- **Backend**: Java 21 + Spring Boot 3.3

---

## Project Structure

```
maatram/
├── frontend/                        # React 19 Client
│   ├── src/
│   │   ├── api/                     # REST API client & endpoint contracts
│   │   │   ├── http.js              # Fetch-based API client
│   │   │   └── index.js             # API entry point & state transitions
│   │   ├── components/              # Modular UI components (Table, Modal, Badge, Pagination)
│   │   ├── pages/                   # Application views & dashboards
│   │   ├── App.jsx                  # Main application layout & shell
│   │   ├── main.jsx                 # Client entry point
│   │   ├── router.jsx               # Lightweight client-side hash router
│   │   └── styles.css               # Design tokens & styles
│   ├── .env.example                 # Frontend environment template
│   ├── .env                         # Active frontend environment config
│   ├── index.html                   # HTML template
│   ├── package.json                 # Node dependencies & scripts
│   └── vite.config.js               # Vite build configuration
│
├── backend/                         # Spring Boot 3 REST API Server
│   ├── src/main/
│   │   ├── java/org/maatram/
│   │   │   ├── config/              # Web & CORS configuration (CorsConfig.java)
│   │   │   ├── controller/          # REST Controllers (SRP: thin HTTP layer)
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── DashboardController.java
│   │   │   │   ├── CycleController.java
│   │   │   │   ├── ApplicationController.java
│   │   │   │   ├── StudentController.java
│   │   │   │   ├── PersonController.java
│   │   │   │   ├── CollegeEventController.java
│   │   │   │   ├── AlumniController.java
│   │   │   │   ├── CompanyController.java
│   │   │   │   ├── DocumentController.java
│   │   │   │   ├── DomainEventController.java
│   │   │   │   ├── PublicController.java
│   │   │   │   └── DevController.java
│   │   │   ├── dto/                 # Data Transfer Objects & PaginatedResponse
│   │   │   ├── exception/           # ApiException & GlobalExceptionHandler
│   │   │   ├── model/               # Domain Models (Person, Application, Student, etc.)
│   │   │   ├── repository/          # Thread-safe persistence & data store (DataStore.java)
│   │   │   ├── service/             # Business logic & domain services
│   │   │   └── MaatramApplication.java
│   │   └── resources/
│   │       └── application.properties # Server port 8000 & settings
│   ├── mvnw / mvnw.cmd              # Maven wrapper scripts
│   └── pom.xml                      # Maven build configuration
│
├── .gitignore                       # Root unified ignore rules
└── README.md                        # Documentation
```

---

## Getting Started

### Prerequisites
- **Java 21 LTS**
- **Node.js 18+**
- **PostgreSQL 14+** (Local service or Docker)

### 1. Run Everything with Docker (Recommended)

To build and spin up the entire production stack (PostgreSQL database, Spring Boot backend, and React frontend) in containers:

```bash
docker compose up --build -d
```

- **Frontend**: [http://localhost:5173](http://localhost:5173)
- **Backend API**: [http://localhost:8000](http://localhost:8000)
- **PostgreSQL**: `localhost:5432`

To stop all containers:
```bash
docker compose down
```

---

### 2. Run Manually (Local Development)

#### Database Configuration (PostgreSQL)

Default credentials are pre-configured in [`backend/.env.example`](backend/.env.example) and [`backend/.env`](backend/.env):

```env
DB_HOST=localhost
DB_PORT=5432
DB_NAME=maatram
DB_USER=postgres
DB_PASSWORD=postgres
```

*(Optional: If you want to run only PostgreSQL in Docker while running the apps locally:)*
```bash
docker compose up postgres -d
```

#### Start the Backend (Spring Boot)

```bash
cd backend
./mvnw clean spring-boot:run
```
*(On Windows: `.\mvnw.cmd spring-boot:run`)*
*The Spring Boot server will start on [http://localhost:8000](http://localhost:8000).*

#### Start the Frontend (React + Vite)

```bash
cd frontend
npm install
npm run dev
```
*The frontend application will start on [http://localhost:5173](http://localhost:5173).*

---

## Authentication & Demo Credentials

- **Admin Login**: `admin@maatram.org`
- **Password**: `admin123`

---

## API Documentation & Architecture

- **CORS Support**: Pre-configured to allow local client access (`http://localhost:5173`).
- **RESTful Endpoints**: Clean mapping across domains (Cycles, Applications, Attendance, Alumni, Companies, Domain Events).
- **Error Format**: Fast-fail exceptions translated into standard `{ "detail": "..." }` responses.
