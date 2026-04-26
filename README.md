# Servicio de Usuarios y Jugadores

## 🧱 Estructura del proyecto (scaffolding)

Proyecto **Java 21** con **Maven** y **Spring Boot**.

### Árbol principal
```text
.
├── 📁 .github/                 # Configuración de GitHub (workflows, etc.)
├── 📁 .mvn/                    # Archivos auxiliares de Maven
├── 📄 pom.xml                  # Dependencias y configuración de build
├── 📁 src/
│   ├── 📁 main/
│   │   ├── 📁 java/
│   │   │   └── 📁 edu/
│   │   │       └── 📁 dosw/
│   │   │           └── 📁 users/
│   │   │               └── App.java  # Clase principal / punto de entrada
│   │   └── 📁 resources/       # application.yml/properties, estáticos, etc.
│   └── 📁 test/
│       └── 📁 java/            # Pruebas automatizadas
└── 📄 README.md
```

### Stack / Tecnologías (según `pom.xml`)
- **Spring Boot** (Web)
- **Spring Data JPA**
- **Spring Validation**
- **Spring Security**
- **PostgreSQL** (runtime)
- **H2** (para pruebas)
- **JWT** (jjwt)
- **OpenAPI/Swagger UI** (springdoc)
- **MapStruct** + **Lombok**
- **Testing**: spring-boot-starter-test + spring-security-test
- **Calidad**: JaCoCo + Sonar Maven Plugin

### Convenciones
- Código fuente: `src/main/java`
- Configuración/recursos: `src/main/resources`
- Pruebas: `src/test/java`
- Paquete base: `edu.dosw.users`
