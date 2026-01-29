# PatientCare - Medical Patient Management System

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE.md) [![Java](https://img.shields.io/badge/Java-21-orange?style=flat&logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.2-brightgreen?style=flat&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Spring Data JPA](https://img.shields.io/badge/Spring%20Data%20JPA-3.2.2-blue?style=flat&logo=spring&logoColor=white)](https://spring.io/projects/spring-data-jpa)
[![H2 Database](https://img.shields.io/badge/H2%20Database-2.2.224-red?style=flat&logo=h2&logoColor=white)](https://www.h2database.com/)
[![Liquibase](https://img.shields.io/badge/Liquibase-4.24.0-blue?style=flat&logo=liquibase&logoColor=white)](https://www.liquibase.com/)
[![Docker](https://img.shields.io/badge/Docker-24.0+-blue?style=flat&logo=docker&logoColor=white)](https://www.docker.com/)
[![Maven](https://img.shields.io/badge/Maven-3.9+-blue?style=flat&logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![Lombok](https://img.shields.io/badge/Lombok-1.18.30-red?style=flat&logo=lombok&logoColor=white)](https://projectlombok.org/)
[![Swagger](https://img.shields.io/badge/Swagger-2.3.0-green?style=flat&logo=swagger&logoColor=white)](https://swagger.io/)


## 📋 Project Overview
PatientCare is a backend medical service prototype for managing patient records in a private clinic. It provides a RESTful API for patient CRUD operations, search, filtering, and statistics. Developed as part of AIT Trimester 2 coursework.

## 🚀 Technologies Stack
- **Java 21** with **Spring Boot 3.2.2**
- **Spring Data JPA** for database operations
- **H2 Database** (in-memory/file-based)
- **Liquibase** for database migrations
- **SpringDoc OpenAPI** for API documentation
- **Docker** for containerization
- **Maven** for build automation
- **Lombok** for boilerplate code reduction
- **Spring Validation** for data integrity

## 📁 Project Structure
```
PatientCare/
├── src/
│   ├── main/java/de/ait/patientcare/
│   │   ├── controller/     # REST controllers
│   │   ├── entity/         # JPA entities and enums
│   │   ├── repository/     # Data access layer
│   │   ├── service/        # Business logic layer
│   │   └── handler/        # Exception handlers
│   ├── resources/
│   │   ├── db/changelog/   # Liquibase migrations
│   │   ├── application.properties
│   │   └── application-test.properties
│   └── test/
│       ├── java/de/ait/patientcare/
│       │               ├── unit/
│       │               │   ├── entity/PatientTest.java
│       │               │   ├── service/PatientServiceTest.java
│       │               │   └── handler/GlobalExceptionHandlerTest.java
│       │               └── integration/
│       │                   ├── repository/PatientRepositoryTest.java
│       │                   ├── controller/PatientControllerIT.java
│       │                   └── service/PatientServiceIntegrationTest.java
│       │   
│       └── resources/application-test.properties
│           
├── Dockerfile
├── pom.xml
└── README.md
```

## 🏗️ Architecture
The application follows a layered architecture:
- **Controller Layer**: Handles HTTP requests/responses
- **Service Layer**: Contains business logic and validation
- **Repository Layer**: Data access using Spring Data JPA
- **Entity Layer**: JPA entities with validation annotations

## 🛠️ Prerequisites
- Java 21 or higher
- Maven 3.6+
- Docker (optional, for containerization)

## 🚀 Getting Started

### Local Development
1. Clone the repository:
```bash
git clone https://github.com/AlexH73/patient-care.git
cd patient-care
```

2. Build and run the application:
```bash
mvn clean spring-boot:run
```

3. Access the application:
    - Application: http://localhost:8080
    - H2 Console: http://localhost:8080/h2-console
        - JDBC URL: `jdbc:h2:file:./data/patientcare`
        - Username: `sa`
        - Password: (empty)
    - Swagger UI: http://localhost:8080/swagger-ui/index.html

### Using Docker
1. Build the Docker image:
```bash
docker build -t patient-care:1.0 .
```

2. Run the container:
```bash
docker run --name patientcare -p 8080:8080 patient-care:1.0
```

## 📊 Database Migrations
Database schema is managed using Liquibase:
- Migrations are located in `src/main/resources/db/changelog/`
- All database changes are version-controlled
- Test data is automatically loaded in `test` profile

## 🔍 API Documentation

### Available Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/patients` | Get all patients |
| GET | `/api/patients/{id}` | Get patient by ID |
| POST | `/api/patients` | Create new patient |
| PUT | `/api/patients/{id}` | Update patient |
| DELETE | `/api/patients/{id}` | Soft delete patient |
| GET | `/api/patients/search` | Search with filters |
| GET | `/api/patients/statistics` | Get statistics |

### Example Patient JSON
```json
  {
   "id": 1,
   "firstName": "John",
   "lastName": "Smith",
   "dateOfBirth": "1995-06-15",
   "gender": "MALE",
   "insuranceNumber": "INS00123456",
   "bloodType": "O_POS",
   "createdAt": "2024-01-01T10:00:00",
   "deleted": false
}
```

### Filtering Parameters
```
GET /api/patients/search?gender=MALE&ageFrom=18&ageTo=65
GET /api/patients/search?bloodType=O_POS
GET /api/patients/search?gender=FEMALE&ageFrom=30
```

## 🧪 Testing
Run tests with:
```bash
# All tests
mvn test

# Integration tests only
mvn test -Dtest="*IntegrationTest"

# Specific test class
mvn test -Dtest=PatientControllerIT
```

### Test Profile
- Uses in-memory H2 database
- Pre-populated with test data via Liquibase
- Separate configuration in `application-test.properties`

## 📝 Validation Rules
- First and last name: Not blank
- Date of birth: Must be in the past
- Insurance number: Unique and not blank
- Gender and blood type: Mandatory
- Age filtering: Optional range parameters

## 🔐 Security Note
⚠️ **Important**: This is a prototype. For production use, add:
- Authentication & Authorization
- HTTPS encryption
- API rate limiting
- Enhanced input validation
- Audit logging

## 📈 Monitoring
The application includes:
- Structured logging with different levels
- Health endpoint at `/health`
- H2 Console for database inspection
- Spring Boot Actuator (can be added)

## 🤝 Contributing
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 👥 Authors
- [Tetiana Anufriieva](https://github.com/TetianaAnufriieva)
- [AlexH73](https://github.com/AlexH73)
- [Cryweb2025](https://github.com/Cryweb2025)
- [Dace Liepina](https://github.com/DaceLiepina)
- [Dmitry Ned](https://github.com/dmitrined)
- [Gott-II](https://github.com/Gott-II)
- [Ilyana P](https://github.com/ilyana-P)
- [Julia](https://github.com/Juliaaa25)
- [Juri Buerkle](https://github.com/JuriBuerkle)
- [Olga Lange](https://github.com/Olga-Lange)
- [Timur Rashitov](https://github.com/TimurRashitov)
- [Wladimir](https://github.com/Wladimir-hub-commits)

## 📄 License
This project is [licensed](LICENSE.md) for educational purposes as part of the AIT coursework.

## 🐛 Known Issues & Todos
- [ ] Add comprehensive integration tests
- [ ] Implement pagination for patient list
- [ ] Add export functionality (CSV/PDF)
- [ ] Implement caching layer
- [ ] Add monitoring with Micrometer/Prometheus

## 📞 Support
For issues and questions, please open an issue in the GitHub repository.

## 🔗 Useful Links
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Data JPA Documentation](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [Liquibase Documentation](https://docs.liquibase.com/)
- [H2 Database Documentation](http://www.h2database.com/html/main.html)
- [Docker Documentation](https://docs.docker.com/)
- [OpenAPI Specification](https://swagger.io/specification/)
```
