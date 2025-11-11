package com.example.webhooksolver;

import com.example.webhooksolver.service.WebhookService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class WebhookSolverApplication {
    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(WebhookSolverApplication.class, args);
        // Only execute the webhook flow at startup when RUN_FLOW=true is set in the environment.
        String runFlow = System.getenv("RUN_FLOW");
        if ("true".equalsIgnoreCase(runFlow)) {
            WebhookService service = ctx.getBean(WebhookService.class);
            service.executeFlow();
        } else {
            // RUN_FLOW not enabled; starting application without executing the flow.
        }
    }
}
