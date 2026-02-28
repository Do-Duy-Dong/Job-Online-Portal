# Job-Online-Portal

A production-style **Job Portal REST API** built with **Java Spring Boot 3.4.0**. It enables job seekers to discover jobs, upload CVs, and apply for positions — while giving employers powerful tools to post jobs and manage applications.

---

## Features

- **JWT Authentication** — Secure, stateless login/registration for Job Seekers and Employers.
- **Role-Based Access Control** — Separate protected routes for `USER` and `EMPLOYER` roles via Spring Security.
- **Job Listings** — Keyset-based pagination for fast, scalable job browsing with keyword, salary, and position filters.
- **Job Applications** — Job seekers can apply using an uploaded CV; employers can view and update application statuses.
- **CV Management** — Upload, list with signed url, and delete CVs with cloud storage backed by **AWS S3**.
- **Category & Tag System** — Organize jobs with categories and tags.
- **Automated Job Expiry** — A scheduled cron job automatically marks expired job listings as inactive.
- **Email Notifications** — Thymeleaf-powered HTML email sending via Spring Mail.

---

## Tech Stack

| Layer           | Technology                                |
|-----------------|-------------------------------------------|
| Language        | Java 17                                   |
| Framework       | Spring Boot 3.4.0                         |
| Database        | PostgreSQL (Spring Data JPA / Hibernate)  |
| Security        | Spring Security + JWT (jjwt 0.11.5)       |
| Cloud Storage   | AWS S3 (Spring Cloud AWS 3.1.1)           |
| Email           | Spring Boot Mail + Thymeleaf              |
| Utilities       | Lombok, ModelMapper 3.1.1                 |
| Containerization| Docker, Docker Compose                    |

---

### Prerequisites

- JDK 17+
- Maven 3.8+
- Docker & Docker Compose
- AWS account with an S3 bucket configured (for CV uploads)

### Performance Optimizations

Index on:
- job.status
- job.created_at
- job.position
- salary range
- Pagination (limit/offset)
Optimization:
- Prevent N+1 query
- Use batch update for job expiry
- Use projection for DTO mapping