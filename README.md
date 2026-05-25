# TECHCUP FÚTBOL

> [!IMPORTANT]
> Este repositorio contiene el *BackEnd* para el servicio de **Usuarios y Jugadores**

> Para informacion general del proyecto consulta el [README general de la organización](https://github.com/techcup-futbol-dosw).

---

## Tabla de contenido

- [Integrantes](#integrantes)
- [Descripción general](#descripción-general)
- [Requerimientos](#requerimientos)
- [Stack tecnológico](#stack-tecnológico)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Configuración local](#configuración-local)
- [Modelación y diagramas](#modelación-y-diagramas)
- [Funcionalidades del servicio](#funcionalidades-del-servicio)
- [API y Endpoints](#api-y-endpoints)
- [Pruebas y calidad](#pruebas-y-calidad)
- [CI/CD](#cicd)

---

## Integrantes

* **Product Owner:** [JUAN SEBASTIÁN GUAYAZÁN CLAVIJO](https://github.com/JuanGuayazanC) → [juan.guayazan-c@mail.escuelaing.edu.co](mailto:juan.guayazan-c@mail.escuelaing.edu.co)
* **Líder técnico:** [BRAYAN LOAIZA LEAL](https://github.com/brloa05) → [brayan.loaiza-l@mail.escuelaing.edu.co](mailto:brayan.loaiza-l@mail.escuelaing.edu.co)
* **Analista funcional:** [JUAN ESTEBAN CRUZ RICO](https://github.com/Cruz-Juan-r) → [juan.cruz-r@mail.escuelaing.edu.co](mailto:juan.cruz-r@mail.escuelaing.edu.co)
* **Desarrollador:** [JUAN JOSÉ LAVERDE RÍOS](https://github.com/juanlaverde777) → [juan.laverde-r@mail.escuelaing.edu.co](mailto:juan.laverde-r@mail.escuelaing.edu.co)
* **Desarrollador:** [JUAN MANUEL VILLEGAS MEDINA](https://github.com/juanmavill) → [juan.vmedina@mail.escuelaing.edu.co](mailto:juan.vmedina@mail.escuelaing.edu.co)

---

## Descripción general

> [!NOTE]
> Gestiona la información de los participantes del torneo y su perfil deportivo

### Funcionalidades del servicio

| Funcionalidad | Descripción | Roles permitidos |
|---------------|-------------|-----------------|
| **Actualizar usuario** | El usuario y el administrador podrán actualizar la información básica del usuario: nombre completo, relación con la Escuela (estudiante, profesor, administrativo, graduado o familiar), programa académico, semestre (si es estudiante). El correo y la contraseña no se podrán modificar. | Autenticado |
| **Inactivar usuario** | El usuario podrá inactivar su propia cuenta y el administrador podrá inactivar cualquier cuenta, validando previamente que no esté participando en un torneo. | Autenticado |
| **Reactivar usuario** | El administrador podrá reactivar una cuenta que haya sido previamente inactivada. | Administrador |
| **Crear perfil deportivo** | Cada jugador podrá crear un perfil deportivo indicando: posición de juego predefinida (portero, defensa, volante, delantero), número dorsal predefinido, foto y si se encuentra disponible o no para ser convocado por algún equipo. | Autenticado |
| **Actualizar perfil deportivo** | El jugador podrá actualizar todos los datos de su perfil deportivo siempre y cuando no esté asignado a un equipo. | Autenticado |
| **Eliminar perfil deportivo** | El sistema no permitirá eliminar un perfil deportivo. | — |
| **Búsqueda de jugadores** | Los capitanes podrán buscar jugadores por: posición, edad, género, nombre, identificación y/o semestre. Retorna también datos del perfil deportivo de cada jugador. | Capitán / Administrador |
| **Invitaciones** | Los capitanes envían invitaciones a jugadores; los jugadores pueden aceptar o rechazar; el capitán puede cancelar las que están pendientes. | Capitán (envía/cancela) · Autenticado (acepta/rechaza) · Administrador |
| **Auditoría** | Registrar las acciones de actualización e inactivación de usuarios y de gestión del perfil deportivo e invitaciones. | Administrador (consulta) |

---

### Requerimientos

> Los requerimientos funcionales y no funcionales de este servicio se encuentran documentados en [`src/main/resources/docs/requirements/requirement.md`](src/main/resources/docs/requirements/requirement.md)

---

## Stack tecnológico

### Backend

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)
![Hibernate](https://img.shields.io/badge/Hibernate-59666C?style=for-the-badge&logo=hibernate&logoColor=white)
![MapStruct](https://img.shields.io/badge/MapStruct-009688?style=for-the-badge)
![Lombok](https://img.shields.io/badge/Lombok-BC4521?style=for-the-badge)

### Base de datos

![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![MongoDB](https://img.shields.io/badge/MongoDB-4EA94B?style=for-the-badge&logo=mongodb&logoColor=white)

### Testing y calidad

![JUnit](https://img.shields.io/badge/JUnit-25A162?style=for-the-badge&logo=java&logoColor=white)
![Mockito](https://img.shields.io/badge/Mockito-78A641?style=for-the-badge)
![JaCoCo](https://img.shields.io/badge/JaCoCo-Coverage-BB0A30?style=for-the-badge)
![SonarQube](https://img.shields.io/badge/SonarQube-4E9BCD?style=for-the-badge&logo=sonarqube&logoColor=white)

### Herramientas y DevOps

![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Swagger](https://img.shields.io/badge/Swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=black)
![Postman](https://img.shields.io/badge/Postman-FF6C37?style=for-the-badge&logo=postman&logoColor=white)

---

## Estructura del proyecto

```text
📦 techchup-users/
├── 📁 .github/                                 # Configuración de GitHub (workflows, templates)
│   └── 📁 workflows/                           # Pipelines CI/CD
├── 📁 .mvn/                                    # Archivos del Maven Wrapper
│   └── 📁 wrapper/
├── 📄 .env.example                             # Variables de entorno para docker-compose
├── 📄 .gitignore
├── 📄 Dockerfile                               # Imagen del servicio (multi-stage)
├── 📄 docker-compose.yml                       # Orquesta servicio + Postgres + Mongo
├── 📄 lombok.config
├── 📄 pom.xml
├── 📄 README.md
├── 📁 src/
│   ├── 📁 main/
│   │   ├── 📁 java/
│   │   │   └── 📁 edu/dosw/users/
│   │   │       ├── 📄 App.java                 # Punto de entrada Spring Boot
│   │   │       ├── 📁 client/                  # Clientes a servicios externos (identity-service)
│   │   │       ├── 📁 config/                  # Configuración global
│   │   │       ├── 📁 controller/              # Endpoints REST
│   │   │       │   ├── 📄 UserController.java
│   │   │       │   ├── 📄 InvitationController.java
│   │   │       │   ├── 📄 SportProfileController.java
│   │   │       │   ├── 📄 AuditLogController.java
│   │   │       │   └── 📄 RootController.java
│   │   │       ├── 📁 dto/                     # DTOs de entrada/salida
│   │   │       ├── 📁 entity/                  # Entidades JPA / documentos Mongo
│   │   │       ├── 📁 enums/                   # Enumeraciones de dominio
│   │   │       ├── 📁 exception/               # Manejo global de excepciones
│   │   │       ├── 📁 mapper/                  # Mapeos DTO ↔ modelo
│   │   │       ├── 📁 model/                   # Modelos de negocio
│   │   │       ├── 📁 repository/              # Repositorios Spring Data
│   │   │       ├── 📁 security/                # JWT, filtros, autorización
│   │   │       │   ├── 📄 SecurityConfig.java
│   │   │       │   ├── 📄 LocalSecurityConfig.java
│   │   │       │   ├── 📄 JwtAuthenticationFilter.java
│   │   │       │   ├── 📄 AccessDeniedHandlerImpl.java
│   │   │       │   ├── 📄 AuthenticationEntryPointImpl.java
│   │   │       │   └── 📁 policy/              # Policies de acceso por recurso
│   │   │       └── 📁 service/                 # Lógica de negocio
│   │   └── 📁 resources/
│   │       ├── 📄 application.properties
│   │       ├── 📄 application-prod.properties
│   │       └── 📁 docs/
│   │           ├── 📁 uml/                     # Diagramas (clases, ER, contenedores)
│   │           ├── 📁 images/                  # Screenshots (Swagger, JaCoCo, Sonar)
│   │           ├── 📁 requirements/            # Requerimientos y alcance
│   │           └── 📁 planning/                # Jira y desglose Scrum
│   └── 📁 test/
│       ├── 📁 java/edu/dosw/users/
│       │   ├── 📁 controller/
│       │   ├── 📁 integration/
│       │   ├── 📁 mapper/
│       │   ├── 📁 model/
│       │   ├── 📁 repository/
│       │   ├── 📁 security/
│       │   │   └── 📁 policy/
│       │   └── 📁 service/
│       └── 📁 resources/
│           └── 📄 application.properties
```

---

## Configuración local

### 1. Clonar el repositorio

```bash
git clone https://github.com/techcup-futbol-dosw/techchup-users.git
cd techchup-users
```

### 2. Compilar el proyecto

```bash
mvn clean install
```

> [!NOTE]
> Este comando descarga dependencias y compila el proyecto completo.

### 3. Configurar variables de entorno

Copia el archivo de ejemplo y completa los valores:

```bash
cp .env.example .env
```

```properties
# Base de datos PostgreSQL
DB_USER=admin
DB_PASSWORD=cambia_esta_password

# MongoDB (fotos de jugadores)
MONGO_USER=admin
MONGO_PASSWORD=cambia_esta_password

# Seguridad JWT (Base64 de 32 bytes, mismo secreto que identity-service)
JWT_SECRET=reemplaza-con-un-base64-de-32-bytes

# DDL: update en primera ejecución, validate después
DDL_AUTO=update

# Servicio externo de identidad
IDENTITY_SERVICE_URL=https://tu-identity-service.azurewebsites.net
```

> [!WARNING]
> Nunca subas credenciales reales al repositorio. El archivo `.env` está en `.gitignore`.

### 4. Ejecutar en desarrollo

```bash
mvn spring-boot:run
```

> [!TIP]
> El servicio se ejecutará en `http://localhost:8080`. Swagger en `http://localhost:8080/swagger-ui.html`.

### 5. Ejecutar en modo empaquetado

```bash
mvn clean package
java -jar target/*.jar
```

### Ejecución con Docker Compose (recomendada)

Levanta el servicio junto con PostgreSQL y MongoDB:

```bash
docker compose up -d --build
```

Para detenerlo:

```bash
docker compose down
```

### Ejecución solo del contenedor del servicio

```bash
docker build -t techchup-users .

docker run -d \
  --name techchup-users \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host:5432/techchup_users \
  -e SPRING_DATASOURCE_USERNAME=admin \
  -e SPRING_DATASOURCE_PASSWORD=cambia_esta_password \
  -e SPRING_DATA_MONGODB_URI=mongodb://admin:pwd@host:27017/techchup_photos?authSource=admin \
  -e SECURITY_JWT_SECRET=base64_secret \
  -e IDENTITY_SERVICE_URL=https://identity-service \
  techchup-users
```

---

## Modelación y diagramas

### Diagrama de contenedores

![ContainerDiagram](src/main/resources/docs/uml/containerDiagram.png)

> Los actores **Jugador**, **Capitán** y **Administrador** acceden vía HTTPS al **Frontend** (React + TypeScript), que envía todas las peticiones REST/HTTPS al **Orchestrator API Gateway** (punto de entrada con enrutamiento y seguridad). El gateway enruta a este **Users and Players Service** (Spring Boot), responsable de la lógica de negocio y persistencia de perfiles deportivos e invitaciones. El servicio se integra con el **Identity Service** (fuente de verdad de usuarios: creación, actualización, inactivación y reactivación de cuentas) y con el **Team service** (consulta del equipo activo del jugador antes de inactivar o procesar invitaciones). Persiste datos relacionales en **PostgreSQL** (`sport_profiles`, `invitations`, `audit_log`) y archivos multimedia (fotos de jugadores) en **MongoDB**.

### Diagrama de clases

![ClassDiagram](src/main/resources/docs/uml/classDiagram.png)

> El diseño sigue una **arquitectura por capas** con separación estricta entre capas:
>
> - **ControllerLayer**: expone los endpoints REST (`UserController`, `SportProfileController`, `InvitationController`, `AuditLogController`, `RootController`) y delega en la capa de servicio.
> - **ServiceLayer**: interfaces (`IUserService`, `ISportProfileService`, `IInvitationService`, `IAuditService`, `ImageService`) e implementaciones que concentran la lógica de negocio. `UserServiceImpl` delega la persistencia de usuarios al `IdentityServiceClient`; `AuditServiceImpl` es transversal y lo invocan los demás servicios para registrar acciones.
> - **ClientLayer**: `IdentityServiceClient` (HTTP al Identity Service para CRUD de usuarios) y `TeamsServiceClient` (consulta de asignación de jugadores a equipos).
> - **MapperLayer**: mappers basados en MapStruct que convierten entre `Model` (dominio) y `Entity` (persistencia), aislando ambas representaciones.
> - **RepositoryLayer**: interfaces Spring Data (`SportProfileRepository`, `InvitationRepository`, `AuditLogRepository`, `PlayerPhotoRepository`) que abstraen el acceso a datos locales.
> - **ModelLayer**: modelos de dominio (`UserModel`, `SportProfileModel`, `InvitationModel`, `AuditLogModel`) y enumeraciones del negocio (`SchoolRelation`, `Gender`, `Position`, `InvitationStatus`, `AuditAction`).
> - **EntityLayer**: entidades JPA (`SportProfileEntity`, `InvitationEntity`, `AuditLogEntity`) que reflejan el esquema relacional local.
>
> Las relaciones clave del dominio: un usuario (gestionado por Identity Service) tiene 1:1 un `SportProfile` y 1:N `Invitation`; cada cambio sobre perfiles e invitaciones genera registros en `AuditLog`.

### Diagrama Entidad-Relación

![DatabaseDiagram](src/main/resources/docs/uml/dataBaseDiagram.png)

> El modelo relacional local de este servicio contiene únicamente tres tablas. Los datos de identidad del usuario (nombre, correo, contraseña, etc.) residen en el **Identity Service** y se referencian a través de un `user_id` externo:
>
> - **`sport_profiles`**: perfil deportivo de cada jugador (`user_id`, `position`, `dorsal_number`, `photo_id` referenciado en MongoDB, `available`). Relación 1:1 con el usuario del Identity Service.
> - **`invitations`**: invitaciones de un equipo a un jugador (`player_id`, `team_id`, `status`, `sent_at`, `responded_at`). Relación 0..* por usuario.
>
> La tabla **`audit_log`** registra de forma transversal las acciones realizadas sobre `sport_profiles` e `invitations` (relaciones 1 — 0..*), guardando `performed_by`, `action`, `timestamp` y `details` para trazabilidad.

---

## API y Endpoints

```
http://localhost:8080/swagger-ui.html
```

![Swagger UI](src/main/resources/docs/images/swaggerUi.png)

### Usuarios

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| GET | `/api/users` | Listar todos los usuarios | Administrador |
| GET | `/api/users/search` | Búsqueda con filtros (nombre, posición, estado, identificación, género, semestre, edad, disponibilidad) — retorna datos del perfil deportivo | Capitán / Administrador |
| GET | `/api/users/{id}` | Obtener usuario por ID | Autenticado |
| GET | `/api/users/identification/{identification}` | Obtener usuario por número de identificación | Capitán / Administrador |
| PUT | `/api/users/{id}` | Reemplazar datos de usuario (operación de administrador) | Administrador |
| PUT | `/api/users/me` | Actualizar el perfil del usuario actual (ID desde JWT o header `X-User-Id`) | Autenticado |
| PATCH | `/api/users/{id}/deactivate` | Desactivar cuenta (estado INACTIVE) | Administrador |
| PATCH | `/api/users/{id}/inactivate` | Inactivar cuenta validando participación en torneo activo | Autenticado |
| PATCH | `/api/users/{id}/reactivate` | Reactivar cuenta previamente inactivada | Administrador |

### Invitaciones

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| GET | `/api/invitations/{id}` | Obtener invitación por ID | Autenticado |
| GET | `/api/invitations/user/{userId}` | Listar invitaciones del jugador | Autenticado |
| POST | `/api/invitations/user/{userId}/team/{teamId}` | Enviar invitación a un jugador desde un equipo | Capitán |
| PATCH | `/api/invitations/{id}/accept` | Aceptar invitación | Autenticado |
| PATCH | `/api/invitations/{id}/reject` | Rechazar invitación | Autenticado |
| PATCH | `/api/invitations/{id}/cancel` | Cancelar invitación pendiente | Capitán |

### Perfiles deportivos

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| GET | `/api/sport-profiles/{id}` | Obtener perfil deportivo por ID | Autenticado |
| GET | `/api/sport-profiles/user/{userId}` | Obtener perfil deportivo por usuario | Autenticado |
| POST | `/api/sport-profiles/user/{userId}` | Crear perfil deportivo (multipart: `profile`, `photo` opcional) | Autenticado |
| PUT | `/api/sport-profiles/{id}` | Actualizar perfil deportivo (multipart: `profile`, `photo` opcional) | Autenticado |
| PATCH | `/api/sport-profiles/{id}/availability?available={true\|false}` | Actualizar disponibilidad | Autenticado |
| GET | `/api/sport-profiles/photos/{photoId}` | Descargar foto del perfil deportivo | Autenticado |

### Auditoría

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| GET | `/api/audit-logs/sport-profiles/{sportProfileId}` | Consultar bitácora de cambios de un perfil deportivo | Administrador |
| GET | `/api/audit-logs/invitations/{invitationId}` | Consultar bitácora de cambios de una invitación | Administrador |

---

## Pruebas y calidad

### Cobertura (JaCoCo)

![JaCoCo Report](src/main/resources/docs/images/jacocoReport.png)

```bash
mvn test
mvn clean test jacoco:report
# Reporte: target/site/jacoco/index.html
```

### Calidad (SonarQube)

![SonarQube](src/main/resources/docs/images/sonarQubeAnalysis.png)

```bash
mvn clean verify sonar:sonar \
  -Dsonar.projectKey=<PROJECT_KEY> \
  -Dsonar.host.url=<SONAR_HOST_URL> \
  -Dsonar.token=<SONAR_TOKEN>
```

### Pruebas de integración (Postman)

![Postman Tests](src/main/resources/docs/images/postmanTests.png)

---

## CI/CD

### Entorno de despliegue

| Campo | Valor |
|-------|-------|
| Plataforma | Azure Web Apps |
| URL del servicio | [https://techcupuserwebservice-bca6dmfbgqd9bkaq.canadacentral-01.azurewebsites.net](https://techcupuserwebservice-bca6dmfbgqd9bkaq.canadacentral-01.azurewebsites.net) |
| Swagger desplegado | [https://techcupuserwebservice-bca6dmfbgqd9bkaq.canadacentral-01.azurewebsites.net/swagger-ui/index.html](https://techcupuserwebservice-bca6dmfbgqd9bkaq.canadacentral-01.azurewebsites.net/swagger-ui/index.html) |
| Última versión | ![Deploy](https://github.com/techcup-futbol-dosw/techchup-users/actions/workflows/ci-cd.yml/badge.svg) |
