# Assignment Plagiarism / Similarity Checker

A full-stack college microproject that helps teachers spot high textual
similarity between student assignment submissions, so they can review
potential plagiarism cases fairly and efficiently.

## 1. Problem Statement

Manually comparing dozens of student submissions for copied or lightly
paraphrased content is slow and error-prone. Teachers need a fast, explainable
way to flag submission pairs that are unusually similar so they can focus
their review time where it matters.

## 2. Motivation

Academic integrity is foundational to the value of a degree. A lightweight,
transparent similarity checker — one whose algorithm can be fully explained,
not a black-box AI service — gives teachers a practical first pass without
requiring an expensive commercial plagiarism-detection subscription.

## 3. SDG Mapping — SDG 4: Quality Education

> The project supports SDG 4 – Quality Education by helping maintain academic
> integrity through efficient detection of potentially similar student
> submissions, supporting a fair and trustworthy learning environment.

## 4. Project Objective

Let teachers create assignments and students submit text answers. When a
teacher runs "Analyze Similarity", the backend preprocesses every submission,
builds TF-IDF vectors, computes pairwise cosine similarity, classifies each
pair as LOW / MODERATE / HIGH, and stores the results for the teacher to
review and compare side by side.

## 5. Features

**Teacher**
- Register / login
- Create, view, update, delete assignments
- View all submissions for an assignment
- Run similarity analysis (TF-IDF + cosine similarity, implemented in Java)
- View similarity results, filter by LOW / MODERATE / HIGH, see flagged pairs
- Open a side-by-side comparison of any two submissions with overlapping
  terms highlighted
- Dashboard with assignment/submission/flagged-pair counts and the highest
  similarity found

**Student**
- Register / login
- View available assignments and deadlines
- Submit assignment text (once per assignment, before the deadline)
- See submission status

## 6. Technology Stack

| Layer     | Technology                                             |
|-----------|---------------------------------------------------------|
| Frontend  | React 18 (hooks, props, state), React Router, Axios, Vite |
| Backend   | Java 17, Spring Boot 3 (Web, Data JPA, Security, Validation) |
| Database  | MySQL                                                   |
| Auth      | JWT (stateless), BCrypt password hashing                |
| Algorithm | TF-IDF + Cosine Similarity — implemented from scratch in Java, no external plagiarism API |

No Node/Express backend, no Python/Django, no Firebase/MongoDB, no
microservices, and no AI/LLM API is used for the similarity detection itself —
per the prescribed stack.

## 7. System Architecture

```
React (Vite dev server, :5173)
        │  REST / JSON, JWT bearer token
        ▼
Spring Boot monolith (:8080)
 ├─ controller   (HTTP in/out)
 ├─ service      (business logic incl. TF-IDF + cosine similarity)
 ├─ repository   (Spring Data JPA)
 ├─ entity       (JPA entities)
 ├─ dto          (request/response shapes)
 ├─ security     (JWT filter, UserDetails, SecurityConfig)
 ├─ exception    (global error handling)
 └─ config       (CORS, demo data seeding)
        │
        ▼
     MySQL
```

Backend package layout:

```
backend/src/main/java/com/example/plagiarismchecker/
    controller/   AuthController, AssignmentController, SubmissionController,
                  AnalysisController, DashboardController
    service/      AuthService, AssignmentService, SubmissionService,
                  AnalysisService, TextPreprocessor, TfIdfCalculator,
                  CosineSimilarityCalculator
    repository/   UserRepository, AssignmentRepository, SubmissionRepository,
                  SimilarityResultRepository
    entity/       User, Assignment, Submission, SimilarityResult, Role,
                  SimilarityLevel
    dto/          Request/response objects
    security/     JwtUtil, JwtAuthFilter, UserPrincipal,
                  CustomUserDetailsService, SecurityUtils
    exception/    ResourceNotFoundException, BadRequestException,
                  UnauthorizedException, GlobalExceptionHandler
    config/       SecurityConfig, DataSeeder
```

Frontend layout:

```
frontend/src/
    components/   Layout, PrivateRoute, SimilarityBadge, StatCard
    pages/        Login, Register, TeacherDashboard, StudentDashboard,
                  Assignments, CreateAssignment, AssignmentDetails,
                  SubmitAssignment, Submissions, SimilarityResults, Comparison
    services/     api.js, authService, assignmentService, submissionService,
                  analysisService
    context/      AuthContext.jsx
    styles/       index.css
```

## 8. Database Schema

```
users
  id, name, email (unique), password (bcrypt hash), role (TEACHER|STUDENT), created_at

assignments
  id, title, description, deadline, teacher_id -> users.id, created_at

submissions
  id, assignment_id -> assignments.id, student_id -> users.id, content,
  submitted_at
  UNIQUE (assignment_id, student_id)  -- one submission per student per assignment

similarity_results
  id, assignment_id -> assignments.id,
  submission_id_1 -> submissions.id, submission_id_2 -> submissions.id,
  similarity_percentage, similarity_level (LOW|MODERATE|HIGH),
  flagged (boolean), created_at
```

Tables are created/updated automatically by Hibernate
(`spring.jpa.hibernate.ddl-auto=update`) — no manual DDL required.

## 9. REST API Documentation

All endpoints except `/api/auth/**` require an `Authorization: Bearer <token>`
header, obtained from `/api/auth/login` or `/api/auth/register`.

**Auth**
| Method | Endpoint             | Notes                        |
|--------|-----------------------|-------------------------------|
| POST   | `/api/auth/register`  | body: name, email, password, role |
| POST   | `/api/auth/login`     | body: email, password         |

**Assignments**
| Method | Endpoint                     | Role    |
|--------|-------------------------------|---------|
| GET    | `/api/assignments`            | any     | list own (teacher) or all (student) |
| POST   | `/api/assignments`            | TEACHER |
| GET    | `/api/assignments/{id}`       | any     |
| PUT    | `/api/assignments/{id}`       | TEACHER, owner only |
| DELETE | `/api/assignments/{id}`       | TEACHER, owner only |

**Submissions**
| Method | Endpoint                                  | Role    |
|--------|---------------------------------------------|---------|
| POST   | `/api/submissions`                          | STUDENT |
| GET    | `/api/submissions/{id}`                     | owner or assignment's teacher |
| GET    | `/api/assignments/{id}/submissions`         | TEACHER, owner only |

**Analysis**
| Method | Endpoint                                       | Role    |
|--------|---------------------------------------------------|---------|
| POST   | `/api/analysis/assignments/{assignmentId}`        | TEACHER, owner only — runs the analysis |
| GET    | `/api/analysis/assignments/{assignmentId}`        | TEACHER, owner only — fetches stored results |
| GET    | `/api/analysis/{resultId}`                        | TEACHER, owner only |
| GET    | `/api/analysis/{resultId}/comparison`             | TEACHER, owner only — side-by-side view |

**Dashboard**
| Method | Endpoint               | Role    |
|--------|-------------------------|---------|
| GET    | `/api/dashboard/teacher`| TEACHER |

## 10. TF-IDF Explanation

For a term *t* in document *d*, within a collection (corpus) of *N* documents:

- **Term Frequency**: `tf(t, d) = count(t in d) / totalTerms(d)`
  How often a word shows up in *this* submission, relative to its length.
- **Inverse Document Frequency** (smoothed, so a word in every document still
  gets a small non-zero weight): `idf(t) = ln((1 + N) / (1 + df(t))) + 1`,
  where `df(t)` is the number of submissions containing *t*.
- **TF-IDF**: `tfidf(t, d) = tf(t, d) * idf(t)`

Each submission becomes a sparse vector of `{term: tfidf weight}`. Common
words (stop words) are removed before this step, and words that appear in
*every* submission for the assignment naturally end up with a low weight,
while words unique to a smaller subset of submissions carry more weight.

See `TfIdfCalculator.java`.

## 11. Cosine Similarity Explanation

Given two TF-IDF vectors A and B:

```
cosine similarity = (A · B) / (‖A‖ * ‖B‖)
```

`A · B` is the dot product over shared terms, and `‖A‖`, `‖B‖` are the vector
magnitudes (Euclidean norms). The result is between 0 (no shared weighted
vocabulary) and 1 (identical vocabulary and emphasis), and is converted to a
percentage by multiplying by 100.

See `CosineSimilarityCalculator.java`.

## 12. Similarity Threshold Explanation

Configurable in `application.properties`:

```
app.similarity.threshold.moderate=40
app.similarity.threshold.high=70
```

| Range        | Label     |
|--------------|-----------|
| 0% – 39%     | LOW       |
| 40% – 69%    | MODERATE  |
| 70% – 100%   | HIGH (also flagged for teacher review) |

## 13. Installation Instructions

### Prerequisites
- Java 17+ and Maven 3.9+
- Node.js 18+ and npm
- MySQL 8.x running locally (or reachable)

### 14. MySQL Setup

```sql
CREATE DATABASE IF NOT EXISTS plagiarism_checker;
CREATE USER IF NOT EXISTS 'plagiarism_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON plagiarism_checker.* TO 'plagiarism_user'@'localhost';
FLUSH PRIVILEGES;
```

(`createDatabaseIfNotExist=true` is already set in the JDBC URL, so the
`CREATE DATABASE` step is optional if your MySQL user has permission to
create databases.)

### 15. Backend Setup

```bash
cd backend
# Edit src/main/resources/application.properties:
#   spring.datasource.username=YOUR_MYSQL_USERNAME
#   spring.datasource.password=YOUR_MYSQL_PASSWORD
mvn spring-boot:run
```

The API starts on `http://localhost:8080`. On first run against an empty
database it seeds demo data automatically (see Demo Credentials below).

### 16. Frontend Setup

```bash
cd frontend
npm install
cp .env.example .env   # defaults already point at http://localhost:8080/api
npm run dev
```

Open `http://localhost:5173`.

## 17. Demo Credentials

All demo accounts use the password: `password123`

| Role    | Email                     |
|---------|----------------------------|
| Teacher | teacher1@college.edu       |
| Teacher | teacher2@college.edu       |
| Student | student1@college.edu       |
| Student | student2@college.edu       |
| Student | student3@college.edu       |
| Student | student4@college.edu       |
| Student | student5@college.edu       |

Two demo assignments are pre-seeded, each with several submissions —
some intentionally paraphrased/similar, others clearly different — so you can
log in as `teacher1@college.edu`, open an assignment, and click
**Analyze Similarity** immediately to see real (non-mocked) results.

Set `app.seed.enabled=false` in `application.properties` to disable this.

## 18. Testing Instructions

```bash
cd backend
mvn test
```

This runs:
- `TextPreprocessorTest` — lowercasing, punctuation removal, tokenization, stop words
- `TfIdfCalculatorTest` — term frequency, inverse document frequency, vector generation
- `CosineSimilarityCalculatorTest` — identical / disjoint / partial-overlap vectors
- `AssignmentServiceTest`, `SubmissionServiceTest` — creation rules, ownership, validation
- `AnalysisServiceTest` — full pairwise analysis using the real algorithm classes
  (identical text ≈100%, different text low, paraphrased text moderate/high,
  4 submissions → 6 pairs, threshold classification)
- `EndToEndWorkflowIntegrationTest` — a real HTTP-level walk through
  register → login → create assignment → submit → analyze → view results →
  compare, against an in-memory H2 database

Manual smoke test with the demo data:
1. Log in as `teacher1@college.edu`.
2. Open "Introduction to Object-Oriented Programming".
3. Click **Analyze Similarity**.
4. Confirm Rahul Sharma ↔ Amit Verma shows as **HIGH SIMILARITY** (paraphrased
   text) and John David's submission shows **LOW SIMILARITY** against everyone
   else (unrelated topic).

## 19. Limitations

1. A high similarity score is a **screening signal**, not proof of plagiarism.
2. The system detects **textual** similarity only — it has no understanding
   of meaning, so it cannot detect semantic plagiarism from heavily reworded
   or translated text.
3. Careful paraphrasing can lower the similarity score.
4. Common phrasing, terminology, or short shared quotations can raise the
   score even between honestly independent submissions.
5. **Teacher review is required** before any academic-integrity decision is
   made — the app deliberately avoids labeling anything as "plagiarized."
6. This is a college microproject, not a commercial-grade plagiarism
   detection platform (no cross-semester corpus, no internet-wide matching,
   no image/code-specific detectors).

## 20. Future Enhancements

- File upload (.docx/.pdf) submissions in addition to plain text
- Cross-assignment and cross-semester similarity checks
- N-gram / sentence-level highlighting instead of single-term overlap
- Email notifications when a new pair is flagged
- Pagination and search on large submission sets
- Role-based admin panel for account management

## 21. Notes on This Implementation

- Passwords are hashed with BCrypt (`spring-boot-starter-security`) — never
  stored in plain text.
- Authentication uses stateless JWTs; the frontend stores the token in
  `localStorage` and attaches it as a Bearer token on every request via an
  Axios interceptor.
- The similarity algorithm (`TextPreprocessor` → `TfIdfCalculator` →
  `CosineSimilarityCalculator`) has zero third-party NLP/ML dependencies —
  every step is plain Java, so it can be read and explained line by line.
- Re-running "Analyze Similarity" on an assignment replaces its previous
  results, so the dashboard always reflects the current set of submissions.
