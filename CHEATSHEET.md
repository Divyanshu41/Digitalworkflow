# Digital Approval Workflow - Quick Cheatsheet

## 1) Run Project Fast
- Prerequisites: Java 17, Maven, MySQL
- DB name: `digital_approval_db`
- Run:
```bash
mvn spring-boot:run
```
- Open:
  - `http://localhost:8080/`

---

## 2) Main URLs
- `/` -> redirects to `/index.html`
- `/index.html` -> login + register
- `/dashboard.html` -> role-based dashboard
- `/test` -> health check

---

## 3) Roles & Access
- `USER`
  - Create request: `POST /requests`
  - View requests: `GET /requests` (frontend self-filter by `userId`)
- `APPROVER`
  - View pending: `GET /requests/pending`
  - Approve: `PUT /requests/{id}/approve`
  - Reject: `PUT /requests/{id}/reject`
- `ADMIN`
  - View all: `GET /requests`
  - Unmatched endpoints default admin-only

---

## 4) Key APIs
## Auth
- Register: `POST /auth/register`
- Current user: `GET /auth/me`

## Requests
- Create: `POST /requests`
- List: `GET /requests`
- Pending: `GET /requests/pending`
- Approve: `PUT /requests/{id}/approve`
- Reject: `PUT /requests/{id}/reject`

---

## 5) Request Lifecycle
`PENDING` -> (`APPROVE` => `APPROVED`) OR (`REJECT` => `REJECTED`)

---

## 6) Most Important Files
## Backend
- App start: `src/main/java/com/example/digitalapproval/DigitalApprovalWorkflowSystemApplication.java`
- Security: `src/main/java/com/example/digitalapproval/config/SecurityConfig.java`
- Auth controller: `src/main/java/com/example/digitalapproval/controller/AuthController.java`
- Request controller: `src/main/java/com/example/digitalapproval/controller/RequestController.java`
- Business logic: `src/main/java/com/example/digitalapproval/service/RequestService.java`
- User auth load: `src/main/java/com/example/digitalapproval/security/CustomUserDetailsService.java`
- Entities: `src/main/java/com/example/digitalapproval/entity/`
- Repositories: `src/main/java/com/example/digitalapproval/repository/`
- DTOs: `src/main/java/com/example/digitalapproval/dto/`

## Frontend (primary)
- Login/Register UI: `src/main/resources/static/index.html`
- Dashboard UI: `src/main/resources/static/dashboard.html`
- Main JS logic: `src/main/resources/static/script.js`
- Main CSS: `src/main/resources/static/style.css`

---

## 7) Change Map (What to edit where)
- Add new role:
  - `SecurityConfig.java`, `AuthController.java`, `script.js`
- Add new field in request (e.g. priority):
  - `Request.java` -> `RequestCreateDto.java` -> `RequestResponseDto.java` -> `RequestService.java` -> `dashboard.html` + `script.js`
- Change approval rules:
  - `RequestService.java` + `SecurityConfig.java`
- Change login/auth mechanism:
  - `AuthController.java`, `SecurityConfig.java`, `script.js`
- UI redesign:
  - `index.html`, `dashboard.html`, `style.css`, `script.js`

---

## 8) Common Issues Quick Fix
- 401 Unauthorized:
  - Credentials/session clear karke re-login karo
  - Role permissions in `SecurityConfig.java` verify karo
- DB connect issue:
  - `application.properties` me URL/username/password check karo
  - MySQL running hai ya nahi check karo
- Role mismatch on dashboard:
  - `/auth/me` response check karo
  - `script.js` role branching verify karo

---

## 9) Security Notes (Important)
- Current DB password code me plain text hai (`application.properties`) -> env vars use karo.
- HTTP Basic in production only over HTTPS.
- `ddl-auto=update` dev ke liye theek; prod me migrations use karo.

---

## 10) Reference Docs
- Full detailed guide: `DETAIL_README.md`
