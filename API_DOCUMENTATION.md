# AI Detector Backend - API Documentation
---

### Key Components:

**Controllers**: Handle HTTP requests and route them to appropriate services
**DTOs (Data Transfer Objects)**: Define the structure of request/response bodies
**Services**: Contain business logic for user management and AI model processing
**Repository**: Manages database operations using JPA
**Security**: Handles JWT token generation and validation
**Config**: Configures Spring beans (e.g., RestTemplate)

---
## API Endpoints

### Authentication Endpoints

#### 1. User Registration
**Endpoint**: `POST /auth/register`

**Description**: Register a new user account with login credentials and email

**Request Headers**:
```
Content-Type: application/json
```

**Request Body**:
```json
{
  "login": "john_doe",
  "password": "SecurePass123!",
  "email": "john@example.com"
}
```

**Field Requirements**:
- `login`: 3-20 characters, alphanumeric and underscore only
- `password`: Minimum 8 characters, must include:
  - At least one uppercase letter
  - At least one digit
  - At least one special character (!@#$%^&*()_+-[]{}|;:'"`,.<>/?~)
- `email`: Valid email format

**Success Response** (201 Created):
```json
"User with login john_doe has been created"
```

**Error Responses**:

- 400 Bad Request (Invalid data):
```json
{
  "error": "Bad Request",
  "message": "invalid data: [field validation errors]",
  "status": 400
}
```

- 409 Conflict (User already exists):
```json
"User with this login already exists"
```

---

#### 2. User Login
**Endpoint**: `POST /auth/login`

**Description**: Authenticate user and receive JWT token for subsequent requests

**Request Headers**:
```
Content-Type: application/json
```

**Request Body**:
```json
{
  "login": "john_doe",
  "password": "SecurePass123!",
  "email": "john@example.com"
}
```

**Success Response** (200 OK):
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqb2huX2RvZSIsImlhdCI6MTUxNjIzOTAyMn0...."
}
```

**Error Responses**:

- 400 Bad Request (Invalid data):
```json
{
  "error": "Bad Request",
  "message": "invalid data: [field validation errors]",
  "status": 400
}
```

- 401 Unauthorized (Invalid credentials):
```json
"User does not exist or invalid password"
```

---

### AI Model Endpoints

#### 3. Analyze Image with AI Model
**Endpoint**: `POST /api/useModel`

**Description**: Upload an image for AI analysis to detect if it's AI-generated

**Authentication**: Required (JWT token must be included in request header)

**Request Headers**:
```
Authorization: Bearer <your-jwt-token>
Content-Type: multipart/form-data
```

**Request Body** (Form Data):
```
image: <binary-image-file>
```

**Supported Image Formats**:
- JPEG (.jpg, .jpeg)
- PNG (.png)
- GIF (.gif)
- BMP (.bmp)
- TIFF (.tiff, .tif)
- WebP (.webp)

**Constraints**:
- Maximum file size: Configurable in `application.properties` (default: typically 10MB)
- File must be a valid image
- File must not be empty

**Success Response** (200 OK):
```json
{
  "certainty": 0.95,
  "modelUsed": "ResNet50",
  "processingTimeMs": 2450
}
```

**Response Fields**:
- `certainty` (Double): Confidence score between 0.0 and 1.0
  - Values closer to 1.0 indicate higher confidence that the image is AI-generated
  - Values closer to 0.0 indicate higher confidence that the image is genuine
- `modelUsed` (String): Name of the AI model used for analysis
- `processingTimeMs` (Long): Time taken to process the image in milliseconds

**Error Responses**:

- 400 Bad Request (Empty file):
```json
{
  "error": "Bad Request",
  "message": "Empty file provided",
  "status": 400
}
```

- 400 Bad Request (Invalid image):
```json
{
  "error": "Bad Request",
  "message": "Provided file was not an image",
  "status": 400
}
```

- 400 Bad Request (File too large):
```json
{
  "error": "Bad Request",
  "message": "File size too large. Maximum allowed size is 10 MB",
  "status": 400
}
```

- 400 Bad Request (Invalid content type):
```json
{
  "error": "Bad Request",
  "message": "File must be an image",
  "status": 400
}
```

- 500 Internal Server Error:
```json
{
  "error": "Internal Server Error",
  "message": "Failed to process image",
  "status": 500
}
```

---