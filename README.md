
---

# ☁️ Cloud File Storage

This project is a multi-user cloud storage service, allowing users to upload, store and manage their files. The application is built as a REST API, providing a backend for a client application to interact with.

This project is based on the technical specification from the [Java Backend Roadmap by Sergey Zhukov](https://zhukovsd.github.io/java-backend-learning-course/projects/cloud-file-storage/).

- [Built With](#built-with)
- [Features](#features)
- [API Reference](#api-reference)
- [Prerequisites](#prerequisites)
- [How to Run Locally](#how-to-run-locally)

## Built With

### Backend
- Java 17+
- Spring Boot
- Spring Web
- Spring Security
- Spring Sessions
- Lombok
- Mapstruct
- Gradle 
- Swagger / OpenAPI 3

### Database & Storage
- Spring Data JPA
- PostgreSQL
- Redis (for session storage)
- MinIO (S3-compatible object storage)
- Liquibase (for database migrations)

### Testing
- JUnit 5
- Testcontainers

## Features

The application provides the following functionalities through its REST API:

-   **User Management**: Secure user registration and authentication (sign-up, sign-in, sign-out)
-   **File and Folder Operations**:
    -   Upload files and entire folder structures
    -   Download any file or folder (folders are downloaded as a ZIP archive)
    -   Create new empty folders
    -   Delete files and folders
    -   Rename and move files or folders
-   **Search**: Search for resources within a user's personal storage space
-   **Security**: Each user can only access and manage their own files

## API Reference

The API is documented using Swagger. Once the application is running, the Swagger UI is available at:

[`http://localhost:8080/swagger-ui/index.html`](http://localhost:8080/swagger-ui/index.html)

Key API endpoints include:
-   `POST /api/auth/sign-up`: Register a new user
-   `POST /api/auth/sign-in`: Authenticate a user and start a session
-   `POST /api/auth/sign-out`: Log out the current user
-   `GET /api/resource/search`: Search for files and folders
-   `POST /api/resource`: Upload files and folders
-   `GET /api/resource/download`: Download a file or folder
-   `DELETE /api/resource`: Delete a file or folder


... and more for managing resources.

## Prerequisites

To run this project, you will need the following installed on your machine:
-   Java 17 or higher
-   Docker and Docker Compose
-   Git
-   IntelliJ IDEA

## How to Run Locally

1.  **Clone the repository**

    ```bash
    git https://github.com/jsteran/CloudFileStorage.git
    ```

2.  **Create Environment File**

    Create a `.env` file in the root directory of the project. Use the template below and fill in the values.

    ```env
    # PostgreSQL Settings
    DB_NAME=cloud_storage_db
    DB_URL=jdbc:postgresql://localhost:5432/cloud_storage_db
    DB_USER=your_db_user
    DB_PASSWORD=your_db_password
    
    # MinIO Settings
    MINIO_USER=your_minio_user
    MINIO_PASSWORD=your_minio_password
    MINIO_BUCKET_NAME=user-files
    MINIO_TEST_BUCKET_NAME=test-bucket-name
    MINIO_URL=http://localhost:9000
    ```

3.  **Launch the Application**

    Open a terminal in the project's root directory and run the following command to build and start services (Postgres, MinIO and Redis):

    ```bash
    docker-compose up -d
    ```
4. **Build project and click 'Run' button in IntelliJ IDEA to start the application itself**
5. **Access the Application**

    The application's API will be available at `http://localhost:8080`.

    You can now interact with the API using tools like Postman, or by exploring the Swagger UI at `http://localhost:8080/swagger-ui/index.html`.