# Servicio de Usuarios y Jugadores

> [!IMPORTANT]
> Este repositorio contiene el *backend* para el servicio de **usuarios y Jugadores**

## Estructura del proyecto (scaffolding)

### Árbol principal
```text
📦 techchup-users
├── 📁 .github/                 # Configuración de GitHub (workflows, etc.)
├── 📁 .mvn/                    # Archivos auxiliares de Maven
├── 📄 .gitignore
├── 📄 lombok.config
├── 📄 pom.xml                  # Dependencias y configuración de build
├── 📁 src/
│   ├── 📁 main/
│   │   ├── 📁 java/
│   │   │   └── 📁 edu/
│   │   │       └── 📁 dosw/
│   │   │           └── 📁 users/
│   │   │               ├── 📄 App.java                # Clase principal / punto de entrada
│   │   │               ├── 📁 client/
│   │   │               ├── 📁 config/
│   │   │               ├── 📁 controller/
│   │   │               ├── 📁 dto/
│   │   │               ├── 📁 entity/
│   │   │               ├── 📁 enums/
│   │   │               ├── 📁 exception/
│   │   │               ├── 📁 mapper/
│   │   │               ├── 📁 model/
│   │   │               ├── 📁 repository/
│   │   │               └── 📁 service/
│   │   ├── 📁 resources/
│   │   │   ├── 📄 application.properties
│   │   │   ├── 📄 application-prod.properties
│   │   │   ├── 📄 application-local.properties.example
│   │   │   └── 📁 docs/
│   │   └── 📁 sql/
│   └── 📁 test/
│       ├── 📁 java/
│       │   └── 📁 edu/
│       │       └── 📁 dosw/
│       │           └── 📁 users/
│       │               ├── 📄 AppTest.java
│       │               ├── 📁 controller/
│       │               ├── 📁 integration/
│       │               ├── 📁 mapper/
│       │               ├── 📁 model/
│       │               ├── 📁 repository/
│       │               └── 📁 service/
│       ├── 📁 resources/
│       │   └── 📄 application.properties
│       └── 📁 sql/
└── 📄 README.md
```

### Stack / Tecnologías

| Tecnología | Descripción |
| ---------- | ----------- |
| **Java 17+** | Lenguaje principal utilizado para desarrollar la aplicación |
| **Spring Boot** | Framework para construir aplicaciones backend y APIs REST |
| **Maven** | Herramienta de gestión de dependencias y construcción del proyecto |
| **Spring Data JPA** | Facilita el acceso a base de datos y el mapeo objeto-relacional |
| **Spring Validation** | Permite validar los datos de entrada en la aplicación |
| **Spring Security** | Gestiona la autenticación y autorización del sistema |
| **PostgreSQL** | Base de datos relacional utilizada en producción |
| **H2 Database** | Base de datos en memoria utilizada para pruebas |
| **JWT (jjwt)** | Manejo de autenticación segura mediante tokens |
| **OpenAPI / Swagger UI** | Documentación interactiva de la API y prueba de endpoints |
| **MapStruct** | Herramienta para mapear objetos (DTOs y entidades) |
| **Lombok** | Reduce código repetitivo (getters, setters, constructores) |
| **Testing (Spring Boot Test + Security Test)** | Herramientas para pruebas unitarias e integración |
| **JaCoCo** | Mide la cobertura de pruebas del código |
| **Sonar Maven Plugin** | Análisis estático de código y control de calidad |

### Convenciones
- Código fuente: `src/main/java`
- Configuración/recursos: `src/main/resources`
- Pruebas: `src/test/java`
- Paquete base: `edu.dosw.users`
