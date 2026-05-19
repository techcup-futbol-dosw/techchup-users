# TECHCUP FÚTBOL

> [!IMPORTANT]
> Este repositorio contiene el **BackEnd** para el servicio de **[Usuarios y Jugadores]**

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

<!--
  EDITAR: Completa con los datos reales de tu equipo.
-->

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

<!--
  EDITAR: Completa con las funcionalidades reales según el enunciado.
-->

| Funcionalidad | Descripción |
|---------------|-------------|
| **Actualizar usuario** | El usuario y el administrador podrán actualizar la información básica del usuario: nombre completo, relación con la Escuela (estudiante, profesor, administrativo, graduado o familiar), programa académico, semestre (si es estudiante). El correo y la contraseña no se podrán modificar. |
| **Crear perfil deportivo** | Cada jugador podrá crear un perfil deportivo indicando: posición de juego predefinida (portero, defensa, volante, delantero), número dorsal predefinido, foto y si se encuentra disponible o no para ser convocado por algún equipo. |
| **Actualizar perfil deportivo** | El jugador podrá actualizar todos los datos de su perfil deportivo siempre y cuando no esté asignado a un equipo. |
| **Eliminar perfil deportivo** | El sistema no permitirá eliminar un perfil deportivo. |
| **Búsqueda de jugadores** | Los capitanes podrán buscar jugadores por: posición, edad, género, nombre, identificación y/o semestre. |
| **Invitaciones** | Los jugadores podrán recibir invitaciones de equipos y aceptar o rechazar invitaciones. |
| **Auditoría** | Registrar las acciones de actualización e inactivación de usuarios. Y de gestión del perfil. |

---

### Requerimientos

> Los requerimientos funcionales y no funcionales de este servicio se encuentran documentados en [`src/main/resources/docs/requirements/requirement.md`](src/main/resources/docs/requirements/requirement.md)

---

## Stack tecnológico

<!--
  EDITAR: Elimina los badges y filas que NO uses en este servicio.
-->

### Backend

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)
![Hibernate](https://img.shields.io/badge/Hibernate-59666C?style=for-the-badge&logo=hibernate&logoColor=white)
![MapStruct](https://img.shields.io/badge/MapStruct-009688?style=for-the-badge)
![Lombok](https://img.shields.io/badge/Lombok-BC4521?style=for-the-badge)

### Base de datos

<!-- EDITAR: Deja solo los que uses -->
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![MongoDB](https://img.shields.io/badge/MongoDB-4EA94B?style=for-the-badge&logo=mongodb&logoColor=white)
![H2](https://img.shields.io/badge/H2-4479A1?style=for-the-badge)

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

<!--
  EDITAR: Reemplaza [servicio] por el nombre real del paquete.
  Elimina las carpetas que no existan. Agrega las que tengas.
-->

```text
📦 techchup-users/
├── 📁 .github/                                 # Configuración de GitHub (workflows, templates)
│   └── 📁 workflows/                           # Pipelines/acciones CI
├── 📁 .mvn/                                    # Archivos del Maven Wrapper
│   └── 📁 wrapper/                             # Configuración interna del wrapper
├── 📄 .gitignore                               # Exclusiones de control de versiones
├── 📄 lombok.config                            # Reglas globales de Lombok
├── 📄 pom.xml                                  # Dependencias y build Maven
├── 📄 README.md                                # Documentación principal
├── 📄 test_output.txt                          # Registro auxiliar de pruebas (si aplica)
├── 📁 src/
│   ├── 📁 main/                                # Código productivo
│   │   ├── 📁 java/
│   │   │   └── 📁 edu/
│   │   │       └── 📁 dosw/
│   │   │           └── 📁 users/
│   │   │               ├── 📄 App.java        # Punto de entrada de Spring Boot
│   │   │               ├── 📁 client/         # Clientes de integraciones externas
│   │   │               ├── 📁 config/         # Configuración global de aplicación
│   │   │               ├── 📁 controller/     # Endpoints REST
│   │   │               │   ├── 📄 UserController.java
│   │   │               │   ├── 📄 InvitationController.java
│   │   │               │   └── 📄 SportProfileController.java
│   │   │               ├── 📁 dto/            # DTOs de entrada/salida
│   │   │               ├── 📁 entity/         # Entidades JPA
│   │   │               ├── 📁 enums/          # Enumeraciones de dominio
│   │   │               ├── 📁 exception/      # Manejo de excepciones
│   │   │               ├── 📁 mapper/         # Mapeos DTO <-> modelo
│   │   │               ├── 📁 model/          # Modelos de negocio
│   │   │               ├── 📁 repository/     # Repositorios Spring Data JPA
│   │   │               ├── 📁 security/       # Seguridad (JWT, filtros, autorización)
│   │   │               │   ├── 📄 SecurityConfig.java
│   │   │               │   ├── 📄 JwtAuthenticationFilter.java
│   │   │               │   ├── 📄 AccessDeniedHandlerImpl.java
│   │   │               │   ├── 📄 AuthenticationEntryPointImpl.java
│   │   │               │   ├── 📄 JwtService.java
│   │   │               │   └── 📁 policy/     # Policies de acceso por recurso
│   │   │               │       ├── 📄 UserAccessPolicy.java
│   │   │               │       ├── 📄 SportProfileAccessPolicy.java
│   │   │               │       ├── 📄 InvitationAccessPolicy.java
│   │   │               │       └── 📄 ResourceAccessPolicy.java
│   │   │               └── 📁 service/        # Lógica de negocio
│   │   ├── 📁 resources/                       # Configuración y recursos runtime
│   │   │   ├── 📄 application.properties
│   │   │   ├── 📄 application-prod.properties
│   │   │   ├── 📄 application-local.properties.example
│   │   │   └── 📁 docs/
│   │   └── 📁 sql/                             # Scripts SQL de aplicación
│   └── 📁 test/                                # Código de pruebas
│       ├── 📁 java/
│       │   └── 📁 edu/
│       │       └── 📁 dosw/
│       │           └── 📁 users/
│       │               ├── 📄 AppTest.java
│       │               ├── 📁 controller/     # Tests de controladores
│       │               ├── 📁 integration/    # Tests de integración
│       │               ├── 📁 mapper/         # Tests de mappers
│       │               ├── 📁 model/          # Tests de modelos
│       │               ├── 📁 repository/     # Tests de repositorios
│       │               ├── 📁 security/       # Tests de seguridad
│       │               │   └── 📁 policy/     # Tests de policies
│       │               └── 📁 service/        # Tests de servicios
│       ├── 📁 resources/                       # Configuración para pruebas
│       │   └── 📄 application.properties
│       └── 📁 sql/                             # SQL de soporte para tests
└── 📁 target/                                  # Artefactos generados por Maven
    ├── 📄 jacoco.exec                          # Datos de cobertura JaCoCo
    ├── 📁 classes/                             # Clases compiladas de producción
    ├── 📁 generated-sources/                   # Fuentes generadas
    ├── 📁 generated-test-sources/              # Fuentes de test generadas
    ├── 📁 maven-status/                        # Estado interno del build
    ├── 📁 site/
    │   └── 📁 jacoco/                          # Reporte HTML de cobertura
    ├── 📁 surefire-reports/                    # Reportes de ejecución de tests
    └── 📁 test-classes/                        # Clases compiladas de pruebas
```

---

## Configuración local

### 1. Clonar el repositorio

```bash
git clone https://github.com/techcup-futbol-dosw/nombre-del-servicio.git
cd nombre-del-servicio
```

### 2. Compilar el proyecto

```bash
mvn clean install
```

> [!NOTE]
> Este comando descarga dependencias y compila el proyecto completo.

### 3. Configurar variables de entorno

```bash
cp src/main/resources/application-local.properties.example \
   src/main/resources/application-local.properties
```

<!--
  EDITAR: Ajusta las variables según las que use tu servicio.
-->

```properties
server.port=8080

spring.datasource.url=jdbc:postgresql://localhost:5432/nombre_db
spring.datasource.username=usuario
spring.datasource.password=contrasena

jwt.secret=clave_secreta
jwt.expiration=3600000
```

> [!WARNING]
> Nunca subas credenciales reales al repositorio. El archivo `application-local.properties` está en `.gitignore`.

### 4. Ejecutar en desarrollo

```bash
mvn spring-boot:run
```

> [!TIP]
> El servicio se ejecutará en `http://localhost:8080`. Swagger en `http://localhost:8080/swagger-ui.html`.

### 5. Ejecutar en modo empaquetado

```bash
mvn clean package
java -jar target/nombre-del-servicio.jar
```

### Ejecución con Docker

```bash
docker build -t nombre-del-servicio .

docker run -d \
  --name nombre-del-servicio \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host:5432/nombre_db \
  -e SPRING_DATASOURCE_USERNAME=usuario \
  -e SPRING_DATASOURCE_PASSWORD=contrasena \
  nombre-del-servicio
```

---

## Modelación y diagramas

<!--
  EDITAR: Actualiza las descripciones y sube las imágenes cuando estén listas.
-->

### Diagrama de contenedores

![ContainerDiagram](src/main/resources/docs/uml/architecturalDesigns/ContainerDiagram.png)

> [Describir cómo este servicio interactúa con los demás componentes.]

### Diagrama de clases

![ClassDiagram](src/main/resources/docs/uml/classDiagram.png)

> [Describir las principales clases y sus relaciones.]

### Diagrama Entidad-Relación

![DatabaseDiagram](src/main/resources/docs/uml/dataBaseDiagram.png)

> [Describir el modelo de datos y las relaciones entre tablas.]

---

## API y Endpoints

<!--
  EDITAR: Reemplaza con los endpoints reales. Agrega una tabla por recurso.
-->

```
http://localhost:8080/swagger-ui.html
```

![Swagger UI](src/main/resources/docs/images/swaggerUi.png)

### [Recurso 1]

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/users` | Listar todos los usuarios |
| GET | `/api/users/{id}` | Obtener usuario por ID |
| GET | `/api/users/identification/{identification}` | Obtener usuario por identificación |
| POST | `/api/users` | Crear nuevo usuario |
| PUT | `/api/users/{id}` | Reemplazar usuario completo (admin) |
| PUT | `/api/users/me` | Actualizar el perfil del usuario actual (`X-User-Id`) |
| PATCH | `/api/users/{id}/deactivate` | Desactivar usuario (estado INACTIVE) |
| PATCH | `/api/users/{id}/inactivate` | Inactivar usuario validando participación en torneo |
| GET | `/api/invitations/{id}` | Obtener invitación por ID |
| GET | `/api/invitations/player/{playerId}` | Listar invitaciones recibidas por jugador |
| POST | `/api/invitations/player/{playerId}/team/{teamId}` | Enviar invitación a jugador desde equipo |
| PATCH | `/api/invitations/{id}/accept` | Aceptar invitación |
| PATCH | `/api/invitations/{id}/reject` | Rechazar invitación |
| PATCH | `/api/invitations/{id}/cancel` | Cancelar invitación pendiente |
| GET | `/api/sport-profiles/{id}` | Obtener perfil deportivo por ID |
| GET | `/api/sport-profiles/user/{userId}` | Obtener perfil deportivo por usuario |
| POST | `/api/sport-profiles/user/{userId}` | Crear perfil deportivo para usuario (multipart: `profile`, `photo` opcional) |
| PUT | `/api/sport-profiles/{id}` | Actualizar perfil deportivo (multipart: `profile`, `photo` opcional) |
| PATCH | `/api/sport-profiles/{id}/availability?available={true\|false}` | Actualizar disponibilidad del perfil deportivo |

---

## Pruebas y calidad

### Cobertura (JaCoCo)

<!--
  EDITAR: Actualiza los números cuando tengas el reporte generado.
-->

![JaCoCo Report](src/main/resources/docs/images/jacocoReport.png)

```bash
mvn test
mvn clean test jacoco:report
# Reporte: target/site/jacoco/index.html
```

### Calidad (SonarQube)

<!--
  EDITAR: Actualiza cuando tengas el análisis generado.
-->

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

<!--
  EDITAR: Actualiza las URLs cuando el servicio esté desplegado.
-->

| Campo | Valor |
|-------|-------|
| Plataforma | Azure Web Apps |
| URL del servicio | [techcupuserwebservice-bca6dmfbgqd9bkaq.canadacentral-01.azurewebsites.net](techcupuserwebservice-bca6dmfbgqd9bkaq.canadacentral-01.azurewebsites.net) |
| Swagger desplegado | [https://nombre-del-servicio.azurewebsites.net/swagger-ui.html](https://nombre-del-servicio.azurewebsites.net/swagger-ui.html) |
| Última versión | ![Deploy](https://github.com/techcup-futbol-dosw/nombre-del-servicio/actions/workflows/cd.yml/badge.svg) |
