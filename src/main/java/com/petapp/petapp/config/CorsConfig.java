package com.petapp.petapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();

        // Permite requisições de qualquer origem durante desenvolvimento
        // Em produção, substitua por suas origens específicas
        corsConfiguration.setAllowedOriginPatterns(Arrays.asList("*"));

        // Ou especifique origens específicas:
        // corsConfiguration.setAllowedOrigins(Arrays.asList(
        //     "http://localhost:5174",
        //     "http://localhost:3000",
        //     "http://localhost:4200"
        // ));

        // Permite todos os métodos HTTP
        corsConfiguration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));

        // Permite todos os headers
        corsConfiguration.setAllowedHeaders(Arrays.asList("*"));

        // Permite credenciais (cookies, authorization headers)
        corsConfiguration.setAllowCredentials(true);

        // Tempo que o navegador pode cachear a resposta de preflight (em segundos)
        corsConfiguration.setMaxAge(3600L);

        // Headers que podem ser expostos ao cliente
        corsConfiguration.setExposedHeaders(Arrays.asList(
            "Access-Control-Allow-Origin",
            "Access-Control-Allow-Credentials"
        ));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);

        return new CorsFilter(source);
    }
}
