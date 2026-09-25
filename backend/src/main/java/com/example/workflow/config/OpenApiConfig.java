package com.example.workflow.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI workflowOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Workflow API")
                .version("1.0")
                .description("面談キャッチアップ用の申請・承認API"));
    }
}
