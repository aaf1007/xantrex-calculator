# Xantrex Solar Calculator — Project Handover

**Project:** Xantrex Solar Calculator (MPPT charge controller sizing tool)
**Course:** CMPT 276 — Group 18, Simon Fraser University
**Handover date:** 
**Prepared by:** Erfan, Huda, Anton

This document is intended to give the Xantrex team everything needed to maintain or extend the application after the course ends.

---

## 1. Setup and deployment (local)

### Prerequisites

- **Java 25** (any vendor; tested with Eclipse Temurin)
- **Maven Wrapper** is bundled (`./mvnw`); no global Maven required
- **PostgreSQL** — either the existing hosted database (see section 2) or a local Postgres 14+ instance
- **Git** (only needed if cloning rather than using a zip)

### First-time setup

1. Clone (or unzip) the project:
2. Create a `.env` file in the project root (a template is at `.env.example`):
  ```env
   DB_URL=jdbc:postgresql://<host>:5432/<database>
   DB_USER=<username>
   DB_PASSWORD=<password>
  ```
   The application reads these via the existing `application.properties`. **Do not commit `.env`** — it is gitignored.
3. For weather lookups, the app calls Open-Meteo's free public API; no API key is required.

### Common commands


| Action                   | Command                                                     |
| ------------------------ | ----------------------------------------------------------- |
| Run the app              | `./mvnw spring-boot:run`                                    |
| Build a runnable JAR     | `./mvnw clean package -DskipTests`                          |
| Run all tests            | `./mvnw test`                                               |
| Run a single test class  | `./mvnw test -Dtest=UserServiceTest`                        |
| Run a single test method | `./mvnw test -Dtest=UserServiceTest#registerHashesPassword` |


The app starts on **[http://localhost:8080](http://localhost:8080)**.

### First admin account

The login screen does not expose a registration form. Admin accounts are created through `/dashboard/admins/add`, which itself requires an existing admin login. To bootstrap the first admin in a fresh database:

- Either restore from the existing hosted database (which already has admin rows), **or**
- Temporarily uncomment the `RegisterController` POST handler (lines 29-42 of `RegisterController.java`) and the registration form in `index.html` (currently commented out), register one `@xantrex.com` user, then re-comment.

Only `@xantrex.com` email addresses can register or log in (enforced in `UserService.register` and `UserService.loadUserByUsername`).

---

## 2. Deployment details

The PostgreSQL connection details are:

- **Host:** `<db-host — fill in>`
- **Database name:** `<db-name — fill in>`
- **User:** `<db-user — fill in>`
- **Password:** delivered separately — see section 3

### Deploying a new build

1. Build the JAR: `./mvnw clean package -DskipTests`
2. Deploy via:
  - **Hosting provider dashboard** — pushing to `main` typically triggers an auto-build if the web service is wired to GitHub, or
  - **Docker** — a `Dockerfile` is included; run `docker build -t xantrex-calculator . && docker run -p 8080:8080 --env-file .env xantrex-calculator`

Hibernate is set to `ddl-auto=update`, so the schema migrates automatically on startup. Spring Session creates `spring_session` and `spring_session_attributes` tables on first run.

---

## 3. Credentials and access

### Inventory of secrets that must be transferred


| #   | Secret                                                             | Where it lives now                       | Suggested transfer                                                                            |
| --- | ------------------------------------------------------------------ | ---------------------------------------- | --------------------------------------------------------------------------------------------- |
| 1   | Hosted PostgreSQL password (`DB_PASSWORD`)                         | Local `.env` on each developer's machine | 1Password / Bitwarden shared item, or in-person handoff                                       |
| 2   | Hosting provider account login (if Xantrex takes ownership)        |                                          | Account ownership transfer via the provider's dashboard                                       |
| 3   | GitHub repository access                                           | `aaf1007` GitHub account                 | Transfer ownership to a Xantrex GitHub org, or invite Xantrex maintainer                      |
| 4   | Open-Meteo API                                                     | None — public unauthenticated API        | No action needed                                                                              |
| 5   | Application admin login (one of the existing `@xantrex.com` users) | PostgreSQL `users` table                 | Share one credential securely; Xantrex can rotate via the change-password flow on first login |


### Access checklist for the Xantrex team

- GitHub repo ownership transferred or maintainer added
- Hosting provider account ownership transferred (or new Xantrex account provisioned and DB migrated)
- At least one `@xantrex.com` admin login confirmed working at `/login`
- `.env` populated on the new owner's local machine and `./mvnw spring-boot:run` reaches the dashboard

---

## 4. Documentation and maintenance notes

### Tech stack

- **Backend:** Spring Boot 4.0.3 on Java 25
- **View:** Thymeleaf server-side templates, Bootstrap 5 (CDN)
- **Persistence:** Spring Data JPA over PostgreSQL; Spring Session (JDBC store)
- **Security:** Spring Security with form login, BCrypt password hashing
- **No REST API, no SPA frontend.** Forms POST directly to controllers, controllers return template names or redirects.

### Package layout

Root package: `com.group18.xantrex_calculator`


| Package      | Responsibility                                                                                                                                     |
| ------------ | -------------------------------------------------------------------------------------------------------------------------------------------------- |
| `controller` | MVC handlers — `RegisterController`, `CalculatorController`, `DashboardController`, `SolarPanelController`, `AdminController`, `WeatherController` |
| `service`    | Business logic — `UserService`, `CalculatorService`, `SolarPanelsService`, `WeatherService`                                                        |
| `repository` | JPA repositories — `UserRepository`, `MpptControllerRepository`, `SolarPanelsRepository`                                                           |
| `entity`     | JPA entities — `User`, `Role` (enum), `MpptController`, `SolarPanels`                                                                              |
| `model`      | DTOs — `CalculatorResult`                                                                                                                          |
| `security`   | `SecurityConfig` (form login, route rules)                                                                                                         |
| `config`     | `PasswordConfig` (BCrypt bean), `DataSeeder`                                                                                                       |
| `exception`  | `UserAlreadyExistsException`, `InvalidDomainException`                                                                                             |


### Calculator sizing logic

Inputs: panel `pmax`, `voc`, `isc`; array `series` × `parallel`; battery bank voltage; ambient minimum temperature (looked up from Open-Meteo or entered manually).

```
totalPower          = pmax × series × parallel
correctedVoc        = voc × series × tempFactor
chargeVoltage       = 14.7 (12V) | 29.4 (24V) | 44.1 (36V) | 58.8 (48V)
maxChargeCurrent    = totalPower / chargeVoltage
shortCircuitCurrent = isc × parallel
```

`tempFactor` is selected based on the location's historical minimum temperature (NEC-style correction): `1.3` below -10 °C, `1.25` below 0 °C, `1.2` otherwise. See `CalculatorController.calculateTemperatureFactor` and `CalculatorService`.

A controller is "compatible" if its `batteryBank` matches and its `maxVoc`, `maxCurrent`, and `maxIsc` all meet or exceed the calculated values. The "best match" returned first is the compatible controller with the **lowest `maxVoc`** (least oversized). See `CalculatorService.findAllCompatibleControllers`.

### Data sources

- **MPPT controller catalog and solar panel catalog:** stored in the `mppt_controllers` and `SolarPanels` tables. Admins manage them through the dashboard. There is no automated import; the catalog must be maintained manually.
- **Weather:** Open-Meteo geocoding API + historical archive API. Free, no key, rate-limited. The app falls back gracefully when the API is unavailable.

### Tests

84 tests across the controller, service, repository, and entity layers. Run with `./mvnw test`. `UserRepositoryTest` and `XantrexCalculatorApplicationTests` connect to the live hosted Postgres instance — they will fail if the database is unreachable or the credentials in `.env` are wrong.

### Known issues / incomplete features


| Issue                                                                                                                                                                                                                                                                               | Location                                                                  |
| ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------- |
| `imageUrl` and `productUrl` on Add Controller / Add Solar Panel accept arbitrary strings. A `javascript:` URL stored as `productUrl` would execute when a user clicks "Get More Info".                                                                                              | `DashboardController.addController`, `SolarPanelController.addController` |
| CSRF protection is **disabled** in `SecurityConfig`. All mutating POSTs are vulnerable to cross-site request forgery if an admin visits a malicious page.                                                                                                                           | `security/SecurityConfig.java`                                            |
| `batteryBank` on `MpptController` is stored as a free-form string with no allowlist. The dropdown shows {12V, 24V, 36V, 48V, 60V} but a direct POST can store anything.                                                                                                             | `DashboardController.addController`                                       |
| `CalculatorService.findAllCompatibleControllers` uses `String.contains` to match battery bank, so `"12V".contains("2")` matches every controller whose bank string contains the digit `2`. Combined with the unvalidated `battV` parameter, this can produce wrong recommendations. | `CalculatorService.java:55`                                               |
| `battV` is not allowlisted server-side — any integer is accepted by `CalculatorController` and silently routed through the `else` branch (`58.8` divisor) in `CalculatorService.maxChargeCurrent`.                                                                                  | `CalculatorController.calculate`, `CalculatorService.maxChargeCurrent`    |
| Hidden `manualTempCelsius` field is unbounded server-side (the `min`/`max` on the visible `manualTemp` input is client-only).                                                                                                                                                       | `CalculatorController.calculate`                                          |
| Add-Admin form has `novalidate`, no email-format check beyond `@xantrex.com` suffix, and no minimum password length.                                                                                                                                                                | `dashboard.html`, `AdminController.addAdmin`, `UserService.register`      |
| Change-password endpoint has no minimum length on the new password.                                                                                                                                                                                                                 | `AdminController.changePassword`                                          |


### Recommended next steps

1. **Re-enable CSRF** and add the standard Thymeleaf `_csrf` hidden inputs to every form. This unblocks all the security work below.
2. **Allowlist `batteryBank` and `battV`** server-side, and replace the `String.contains` match with an exact-equality check.
3. **Validate `imageUrl` / `productUrl`** as `http(s)://...` only, server-side.
4. **Add a minimum password length** for both Add-Admin and Change-Password, and a stricter email regex on Add-Admin.
5. **Bound `manualTempCelsius`** on the server (sane range, e.g. -100 °C to 150 °C).
6. **Make the delete endpoints idempotent** by following the `findById(...).map(...)` pattern from `AdminController.deleteAdmin`.
7. **URL-encode `city`/`country`** in `WeatherService` (use `URLEncoder.encode(..., StandardCharsets.UTF_8)`).
8. **Lock down Hibernate `ddl-auto`** for production — `update` is convenient for development but lets accidental entity changes alter the schema. Consider `validate` in production with explicit Flyway migrations.
9. **Decide on a MPPT catalog source of truth.** Right now the database is the only copy; a CSV checked into the repo (or a seed migration) would make environment provisioning reproducible.

---

## 5. Future contact information

For follow-up questions after the course ends, the Xantrex team can reach:

| Name | Email |
| Anton Florendo | [aaf13@sfu.ca](mailto:aaf13@sfu.ca) |
** Fill **

---

