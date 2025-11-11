package com.example.webhooksolver.service;

import com.example.webhooksolver.model.WebhookResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class WebhookService {
    private static final Logger log = LoggerFactory.getLogger(WebhookService.class);
    private final RestTemplate restTemplate;
    private final ObjectMapper mapper = new ObjectMapper();

    // Customized candidate details
    private final String candidateName = "Gagan R";
    private final String regNo = "U25UV23T006065";
    private final String email = "gagan15011ce@gmail.com";

    @Autowired
    public WebhookService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void executeFlow() {
        try {
            log.info("Starting webhook generation flow...");
            WebhookResponse response = generateWebhook();
            if (response == null || response.getWebhook() == null || response.getAccessToken() == null) {
                log.error("Invalid response from generateWebhook: {}", response);
                return;
            }

            log.info("Received webhook: {}", response.getWebhook());

            int lastTwo = lastTwoDigitsFromRegNo(regNo);
            boolean isOdd = (lastTwo % 2) == 1;
            String questionUrl = isOdd ?
                "https://drive.google.com/file/d/1IeSI6l6KoSQAFfRihIT9tEDICtoz-G/view?usp=sharing" :
                "https://drive.google.com/file/d/143MR5cLFrlNEuHzzWJ5RHnEWuijuM9X/view?usp=sharing";

            log.info("Determined regNo last two digits: {} (odd? {}) -> question: {}", lastTwo, isOdd, questionUrl);

            // === PLACEHOLDER: replace with your final SQL query ===
            String finalQuery = "SELECT department, COUNT(*) FROM employees GROUP BY department;";

            // store locally
            storeFinalQueryLocally(finalQuery);

            // submit final query to webhook
            submitFinalQuery(response.getWebhook(), response.getAccessToken(), finalQuery);

            log.info("Flow completed.");
        } catch (Exception e) {
            log.error("Error during flow", e);
        }
    }

    private WebhookResponse generateWebhook() {
        String url = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";
        Map<String,String> body = new HashMap<>();
        body.put("name", candidateName);
        body.put("regNo", regNo);
        body.put("email", email);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String,String>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> resp = restTemplate.postForEntity(url, entity, String.class);
        if (resp.getStatusCode().is2xxSuccessful()) {
            try {
                JsonNode root = mapper.readTree(resp.getBody());
                WebhookResponse wr = new WebhookResponse();
                if (root.has("webhook")) wr.setWebhook(root.get("webhook").asText());
                if (root.has("accessToken")) wr.setAccessToken(root.get("accessToken").asText());
                if (wr.getAccessToken() == null && root.has("token")) wr.setAccessToken(root.get("token").asText());
                return wr;
            } catch (IOException e) {
                log.error("Failed to parse response body", e);
            }
        } else {
            log.error("generateWebhook responded with status {} and body {}", resp.getStatusCode(), resp.getBody());
        }
        return null;
    }

    private int lastTwoDigitsFromRegNo(String regNo) {
        if (regNo == null) return 0;
        String digits = regNo.replaceAll("\\D+", "");
        if (digits.length() >= 2) {
            String lastTwo = digits.substring(digits.length() - 2);
            try {
                return Integer.parseInt(lastTwo);
            } catch (NumberFormatException e) {
                log.warn("Unable to parse last two digits from {}", regNo);
            }
        }
        if (digits.length() == 1) {
            return Integer.parseInt(digits);
        }
        return 0;
    }

    private void storeFinalQueryLocally(String finalQuery) {
        File out = new File("finalQuery.sql");
        try (FileWriter fw = new FileWriter(out)) {
            fw.write(finalQuery);
            log.info("Saved final query to {}", out.getAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to write final query to disk", e);
        }
    }

    private void submitFinalQuery(String webhookUrl, String accessToken, String finalQuery) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // Use Bearer token in Authorization header
        headers.setBearerAuth(accessToken);

        Map<String,String> body = new HashMap<>();
        body.put("finalQuery", finalQuery);

        HttpEntity<Map<String,String>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> resp = restTemplate.postForEntity("https://bfhldevapigw.healthrx.co.in/hiring/testWebhook/JAVA", entity, String.class);
            log.info("submitFinalQuery status: {}", resp.getStatusCode());
            log.info("submitFinalQuery body: {}", resp.getBody());
        } catch (Exception e) {
            log.error("Failed to submit final query to webhook", e);
        }
    }
}
