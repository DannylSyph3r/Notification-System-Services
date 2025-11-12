# Template Service 📧

This service is part of the Distributed Notification System. Its sole responsibility is to store, manage, and version control notification templates (for email and push notifications).

It provides internal API endpoints for other services (like the Email and Push services) to fetch compiled templates.

## Technology Stack

* **Framework**: [NestJS](https://nestjs.com/)
* **Language**: [TypeScript](https://www.typescriptlang.org/)
* **Database**: [PostgreSQL](https://www.postgresql.org/) (for storing templates and versions)
* **Cache**: [Redis](https://redis.io/) (for caching frequently accessed templates)
* **Containerization**: [Docker](https://www.docker.com/)



***

## Setup & Installation

### 1. Prerequisites

* [Node.js](https://nodejs.org/) (v18 or higher)
* [Docker](https://www.docker.com/products/docker-desktop/) (and Docker Compose)
* [NPM](https://www.npmjs.com/)

### 2. Install Dependencies

Clone the main project repository and navigate into this service's directory:

```bash
cd template-service
npm install