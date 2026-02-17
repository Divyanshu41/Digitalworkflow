# Digital Approval Workflow System

A full-stack web application for managing approval workflows with role-based access control.

## Project Overview

This system allows users to submit approval requests, approvers to review and approve/reject requests, and admins to manage users and oversee the entire process.

## Tech Stack

- **Backend**: Java 17, Spring Boot 3.2.0, Spring Security, JWT
- **Database**: MySQL 8
- **Frontend**: HTML, CSS, JavaScript
- **Build Tool**: Maven

## Features

### Authentication & Authorization
- Role-based login (User, Approver, Admin)
- JWT-based authentication
- Password encryption with BCrypt

### User Module
- User registration and login
- Submit approval requests with attachments
- View request status and history

### Approver Module
- View assigned requests
- Approve or reject requests with comments
- Email notifications with graceful fallback logging

### Admin Module
- Manage users (CRUD operations)
- Assign approvers to requests
- View all requests with pagination and filters
- Review full audit trail of key actions
- View real-time summary metrics for requests

### Additional Features
- Centralized audit logging for registrations, assignments, approvals, rejections, and role changes
- Request summary reporting (counts by status, pending approvals, orphaned requests)
- Paginated REST endpoints and UI tables for scalable data browsing
- Responsive role-based frontend hosted from the Spring Boot static resources
- Configurable SMTP integration (defaults to logging when mail is unavailable)

## Project Structure

```
backend/
├── src/main/java/com/example/digitalapproval/
│   ├── controller/          # REST controllers
│   ├── service/             # Business logic
│   ├── repository/          # Data access layer
│   ├── entity/              # JPA entities
│   ├── dto/                 # Data transfer objects
│   ├── security/            # Security configuration
│   └── DigitalApprovalApplication.java
├── src/main/resources/
│   └── application.properties
└── pom.xml

frontend/
├── index.html
├── styles.css
└── script.js

db/
└── schema.sql
```

## Database Schema

Core tables are provisioned through `db/schema.sql`:
- `users`: Stores account credentials and roles
- `requests`: Captures submitted approval requests and assigned approvers
- `approvals`: Persists historical approve/reject decisions and remarks
- `audit_logs`: Tracks who performed sensitive operations along with contextual details

## Setup Instructions

### Prerequisites
- Java 17 or higher
- MySQL 8
- Maven 3.6+

### Backend Setup

1. Clone the repository
2. Navigate to the backend directory
3. Update `application.properties` (or environment variables) with your MySQL and SMTP credentials
   - Per Spring Boot convention, any property may be overridden via environment variables (e.g. `SPRING_DATASOURCE_PASSWORD`)
4. Run the SQL script in `db/schema.sql` to create the database, or allow Hibernate to create/update tables automatically (`spring.jpa.hibernate.ddl-auto=update`)
   > The sample seed users ship with placeholder BCrypt hashes; replace them with real values before first login.
5. Run the application:
   ```bash
   mvn spring-boot:run
   ```

### Frontend Setup

The production UI is bundled under `backend/src/main/resources/static`. Once the backend is running, visit http://localhost:8080/ to access the role-aware dashboard.

For local development tweaks, update the files under `frontend/` and copy them into the backend `static/` directory or adjust the build tooling as desired.

### Running the Application

1. Start the backend server (runs on http://localhost:8080)
2. Open the frontend in a browser
3. Register a new user or login with existing credentials

## Configuration Notes
- Email alerts require valid SMTP credentials; when delivery fails the service logs a warning but the request workflow continues.
- Sensitive secrets should be supplied via environment variables or Spring Boot's external configuration support.

## API Endpoints

### Authentication
- `POST /api/auth/login` - User login
- `POST /api/auth/register` - User registration

### User Endpoints
- `POST /api/user/requests` - Submit new request
- `GET /api/user/requests` - Get user's requests

### Approver Endpoints
- `GET /api/approver/requests` - Get assigned requests
- `POST /api/approver/requests/{id}/approve` - Approve request
- `POST /api/approver/requests/{id}/reject` - Reject request

### Admin Endpoints
- `GET /api/admin/users` - Get all users
- `PUT /api/admin/users/{id}/role` - Update user role
- `DELETE /api/admin/users/{id}` - Delete user
- `GET /api/admin/requests` - Get all requests (supports `page` and `size` query parameters)
- `POST /api/admin/requests/{id}/assign` - Assign approver
- `GET /api/admin/audit` - Paginated audit log of critical events
- `GET /api/admin/reports/summary` - Aggregate counts for dashboards

## Security

- JWT tokens for authentication
- Role-based access control
- Password encryption
- CORS configuration

## Testing

Run tests with:
```bash
mvn test
```

## Deployment

1. Build the JAR file:
   ```bash
   mvn clean package
   ```
2. Run the JAR:
   ```bash
   java -jar target/digital-approval-workflow-0.0.1-SNAPSHOT.jar
   ```

## Future Enhancements

- File upload for attachments
- Advanced reporting and analytics
- Workflow customization
- Integration with external systems
- Mobile app development

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License.