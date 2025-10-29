package com.petapp.petapp.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI petAppOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Pet App API")
                        .description("API para integração com Facebook Graph API - Gerenciamento de posts sobre pets encontrados/desaparecidos")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Pet App Team")
                                .email("contato@petapp.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}
