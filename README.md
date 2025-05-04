# CSV Flow - Microservices Project

## Project Structure

The `csv-flow` project is a microservices-based application built with **Java**, **Spring Boot**, and **Maven**. Below is an overview of the structure:

### Layers
1. **Controller Layer**: Handles HTTP requests and responses.
   - Example: `BasesController` manages API endpoints for CRUD operations and file handling.
2. **Service Layer**: Contains business logic.
   - Example: `FileImportService` processes CSV files and interacts with the database.
3. **Repository Layer**: Interfaces with the database using JPA.
   - Example: `BasesRepository` and `ImportErrosRepository`.
4. **Model Layer**: Defines the data models/entities.
   - Example: `Bases` and `ImportErros`.

### Key Components
- **Spring Boot**: Provides the framework for building REST APIs.
- **MySQL**: Used as the relational database.
- **Hibernate**: Manages ORM (Object-Relational Mapping).
- **Apache Commons CSV**: Handles CSV file parsing and generation.
- **EntityManager**: Executes native SQL queries.

### Configuration
- **`application.yml`**: Configures the server, database, and logging.
- **Port**: `8080`
- **Database**: MySQL connection details are provided.

---

## API Endpoints

### Base Management
| Method | Endpoint               | Description                          | Request Body         |
|--------|-------------------------|--------------------------------------|----------------------|
| POST   | `/v1/bases`            | Create a new base                   | `BasesDTO`           |
| GET    | `/v1/bases`            | Retrieve all bases                  | N/A                  |
| GET    | `/v1/bases/{id}`       | Retrieve a base by ID               | N/A                  |
| PUT    | `/v1/bases/{id}`       | Update a base by ID                 | `BasesDTO`           |
| DELETE | `/v1/bases/{id}`       | Delete a base by ID                 | N/A                  |

### CSV File Operations
| Method | Endpoint                          | Description                          | Request Body         |
|--------|------------------------------------|--------------------------------------|----------------------|
| GET    | `/v1/bases/download/csv/{baseId}` | Download a CSV file for a base       | N/A                  |
| POST   | `/v1/bases/import/{baseId}`       | Import a CSV file into a base        | `MultipartFile`      |

---

## How to Run

1. Clone the repository:
   ```bash
   git clone https://github.com/VL0511/CSV-Flow.git