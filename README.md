# Spring Boot Webhook Solver (customized)

Candidate:
- name: Gagan R
- regNo: U25UV23T006065
- email: gagan15011ce@gmail.com

## How it works
On startup the app:
1. POSTs to generateWebhook/JAVA with the candidate details.
2. Receives `webhook` and `accessToken`.
3. Determines question using last two digits of `regNo`.
4. Reads `finalQuery` string in `WebhookService` (replace the placeholder).
5. Saves `finalQuery.sql` locally and POSTs the query to testWebhook/JAVA with Authorization: Bearer <token>.

## Replace final SQL
Open `src/main/java/com/example/webhooksolver/service/WebhookService.java` and edit the line:

```java
String finalQuery = "/* REPLACE THIS: put your final SQL query here */";
```

Put your final SQL query as a single-line Java string (escape double quotes if any).

## Build
Requirements: JDK 17+, Maven

```bash
mvn clean package
<img width="1800" height="1136" alt="Screenshot 2025-11-11 155147" src="https://github.com/user-attachments/assets/f2528136-01f8-44e1-a515-8f48ce07a8c2" />

java -jar target/webhook-solver-1.0.0.jar
```

## Notes
- The project will attempt real network calls on startup. If you want to test offline, modify `generateWebhook()` to return a stubbed `WebhookResponse`.
