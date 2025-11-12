# Distributed Email Notification Service

> **Note:** This is part of a group project. Please coordinate with team members before making major changes.

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Setup & Installation](#setup--installation)
- [Environment Variables](#environment-variables)
- [Running the Service](#running-the-service)
- [Health Checks](#health-checks)
- [Contributing](#contributing)
- [License](#license)

---

## Overview

The **Distributed Email Notification Service** is a NestJS-based microservice designed to handle email notifications asynchronously.  
It consumes messages from RabbitMQ, formats emails using templates, and sends them via SendGrid.  
It also includes Redis and RabbitMQ health checks to ensure robust operations.

---

## Features

- Asynchronous email delivery using RabbitMQ queues
- Template-based email formatting
- SendGrid integration for reliable email delivery
- Redis & RabbitMQ health checks
- Modular and extensible architecture for future notification channels

---

## Tech Stack

- **Backend:** Node.js, NestJS
- **Messaging:** RabbitMQ
- **Caching:** Redis
- **Email Delivery:** SendGrid
- **Validation:** class-validator (optional, for DTOs)
- **Configuration:** @nestjs/config, dotenv

---

## Setup & Installation

1. Clone the repository:

```bash
git clone https://github.com/DannylSyph3r/Notification-System-Services.git
cd email-service
Install dependencies:

bash
Copy code
npm install
Create a .env file based on .env.example and fill in your credentials.

Environment Variables
The service expects the following environment variables:

Variable	Description
PORT	Port on which the service runs (default: 3002)
SENDGRID_API_KEY	Your SendGrid API key
FROM_EMAIL	Verified email address to send from
RABBITMQ_HOST	RabbitMQ server host
RABBITMQ_PORT	RabbitMQ server port
RABBITMQ_USER	RabbitMQ username
RABBITMQ_PASSWORD	RabbitMQ password
REDIS_HOST	Redis server host
REDIS_PORT	Redis server port

Ensure that your SendGrid sender identity is verified to avoid delivery errors.

Running the Service
Start the service in development mode:

bash
Copy code
npm run start:dev
Or run directly with TS-Node for testing:

bash
Copy code
npx ts-node src/main.ts
Health Checks
The service exposes health checks via the HealthCheckService:

Redis – checks connectivity

RabbitMQ – ensures connection is active

Email provider (SendGrid) – confirms service is operational

You can invoke the health check programmatically or via the HealthController.

Contributing
This is a group project, so please follow these guidelines:

Use feature branches: feature/xyz or fix/xyz.

Ensure all email-related features go through SendGridService.

Update the README and .env.example if adding new environment variables.

Write clean, modular code and test email flows locally before pushing.
