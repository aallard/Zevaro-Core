package ai.zevaro.core.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI zevaroOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Zevaro Core API")
                        .description("Continuous Outcome Engineering Platform — API v2.0")
                        .version("2.0.0")
                        .contact(new Contact()
                                .name("Zevaro Engineering")
                                .url("https://zevaro.ai")
                                .email("support@zevaro.ai"))
                        .license(new License()
                                .name("Proprietary")))
                .externalDocs(new ExternalDocumentation()
                        .description("Zevaro Documentation")
                        .url("https://docs.zevaro.ai"))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter JWT token")));
    }
}
