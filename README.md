# L2 assist service

A small service that receives, analyzes, and provides results on production incidents.

The project is organized as a multi-module Gradle build (domain/usecase/dao/controller/infrastructure) and is designed
to run locally via **Docker Compose**.

---

## What this service does

Given incident data, the service can:

- Takes raw incident data as input
- Cleans up sensitive data
- Checks the security of the prompt
- Sends the data to the LLM for analysis
- Provides processed results (incident description and resolution hypotheses)

---

## Tech stack (high level)

- Java / Spring Boot (web)
- Spring-Ai for llm request
- Docker + Docker Compose for local environment

---

## Local run (Docker Compose)

The repository includes `docker-compose-local.yml` for a full local setup.

### Prerequisites

- Docker + Docker Compose v2

### Start

docker compose -f docker-compose-local.yml up --

After startup the service is expected to be available on:

- `http://localhost:8080`

---

## Configuration

The Docker Compose file passes configuration via environment variables.

Typical variables (names may vary by module/config):

- `OPENAI_API_KEY` — your OpenAi api key

> Note: All variables that are needed for local start added in config by default.

---

## API

The service exposes HTTP endpoints to request statistics.

If Swagger/OpenAPI UI is enabled, it is typically available at one of:

- `http://localhost:8080/swagger-ui/index.html`

### Example localhost requests (curl for Postman import)

curl --location 'http://localhost:8080/api/v1/incidents' \
--header 'Content-Type: application/json' \
--data-raw '{                       
"incidentDescription": "Клиент Петров Алексей Сергеевич обратился по поводу недоступности личного кабинета. Контакт:
+7 (912) 345-67-89, peter.alex@gmail.com. Счёт IBAN RU49 0445 2560 0404 1577 7154 125 заблокирован. Ошибка с 09:15 MSK:
HTTP 503 от payment-service. В логах: connection pool exhausted, max=50."         
} '

---

