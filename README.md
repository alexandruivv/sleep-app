# Sleep Logger API

This project provides a REST API for logging and analyzing sleep data.  
The API is designed to be integrated later into the Noom web interface and focuses on tracking sleep information and computing useful statistics for users.

---

## 🚀 Getting Started

### Prerequisites
- Docker

---

## ▶️ Running the Application

The application can be started using Docker Compose.

From the project root, run:

    docker compose up

This will start **one container** that runs the entire application.

Once the container is up and running, the API will be available at:

    http://localhost:8080

To stop the application:

    docker compose down

---

## 📘 API Documentation (Swagger)

The API is documented using Swagger UI.

You can access the interactive API documentation at:

    http://localhost:8080/swagger-ui/index.html

Swagger allows you to:
- Explore available endpoints
- View request and response models
- Execute API calls directly from the browser

---

## 🛌 Functional Overview

The Sleep Logger API supports the following core features:

---

### 1️⃣ Create Sleep Log (Last Night)

Allows creating a sleep log entry for the last night.

Sleep data includes:
- Date of sleep (today)
- Time-in-bed interval (start and end)
- Total time spent in bed
- Morning feeling (one of: BAD, OK, GOOD)

---

### 2️⃣ Fetch Last Night’s Sleep

Retrieves the sleep information recorded for the most recent night.

This endpoint is useful for:
- Displaying the latest sleep details to the user
- Verifying that sleep data was logged correctly

---

### 3️⃣ Get Last 30-Day Averages

Computes sleep statistics over the last 30 days.

Response includes:
- Date range used for the averages
- Average total time spent in bed
- Average time the user goes to bed
- Average time the user gets out of bed
- Frequency distribution of morning feelings (BAD, OK, GOOD)


## Postman collection

Postman collection is attached in the project for importing it and testing the endpoints.

## Initial data script

There is an initial data script that runs in order to create the tables and import data for 30 days for user 11111111-1111-1111-1111-111111111111, in order to test the endpoint for average.

## User header

Make sure you include the header `X-User-Id` in the endpoints in order to create / access the data.