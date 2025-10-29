package com.petapp.petapp.controller;

import com.petapp.petapp.config.FacebookApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/facebook-config")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Facebook Configuration", description = "Endpoints para testar e validar configurações do Facebook")
public class FacebookConfigTestController {

    private final FacebookApiConfig facebookApiConfig;
    private final WebClient webClient;

    @Operation(summary = "Testar configuração do Facebook",
               description = "Valida se as credenciais do Facebook estão configuradas corretamente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Configuração válida"),
        @ApiResponse(responseCode = "500", description = "Erro na configuração")
    })
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testFacebookConfig() {
        Map<String, Object> result = new HashMap<>();

        try {
            // Validar configurações
            result.put("baseUrl", facebookApiConfig.getBaseUrl());
            result.put("pageId", facebookApiConfig.getPageId());
            result.put("fields", facebookApiConfig.getFields());
            result.put("limit", facebookApiConfig.getLimit());
            result.put("tokenConfigured", facebookApiConfig.getPageAccessToken() != null && !facebookApiConfig.getPageAccessToken().isEmpty());
            result.put("tokenLength", facebookApiConfig.getPageAccessToken() != null ? facebookApiConfig.getPageAccessToken().length() : 0);

            // Testar chamada simples à API do Facebook (buscar info da página)
            log.info("Testing Facebook API connection...");

            String testUrl = String.format("%s/%s?fields=id,name,category&access_token=%s",
                facebookApiConfig.getBaseUrl(),
                facebookApiConfig.getPageId(),
                facebookApiConfig.getPageAccessToken());

            Map pageInfo = webClient.get()
                .uri(testUrl)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

            result.put("connectionTest", "SUCCESS");
            result.put("pageInfo", pageInfo);
            result.put("message", "Facebook API configuration is valid and working!");

            log.info("Facebook API test successful: {}", pageInfo);

            return ResponseEntity.ok(result);

        } catch (WebClientResponseException e) {
            String errorBody = e.getResponseBodyAsString();

            log.error("Facebook API test failed - Status: {}, Body: {}", e.getStatusCode(), errorBody);

            result.put("connectionTest", "FAILED");
            result.put("error", errorBody);
            result.put("statusCode", e.getStatusCode().value());
            result.put("message", buildErrorGuidance(e.getStatusCode().value()));

            return ResponseEntity.status(500).body(result);

        } catch (Exception e) {
            log.error("Error testing Facebook configuration", e);

            result.put("connectionTest", "FAILED");
            result.put("error", e.getMessage());
            result.put("message", "Unexpected error testing Facebook configuration");

            return ResponseEntity.status(500).body(result);
        }
    }

    @GetMapping("/status")
    @Operation(summary = "Status da configuração",
               description = "Retorna o status das variáveis de ambiente configuradas")
    public ResponseEntity<Map<String, Object>> getConfigStatus() {
        Map<String, Object> status = new HashMap<>();

        status.put("baseUrl", facebookApiConfig.getBaseUrl() != null ? "Configured" : "Not configured");
        status.put("pageId", facebookApiConfig.getPageId() != null && !facebookApiConfig.getPageId().isEmpty() ? "Configured" : "Not configured");
        status.put("pageAccessToken", facebookApiConfig.getPageAccessToken() != null && !facebookApiConfig.getPageAccessToken().isEmpty() ? "Configured" : "Not configured");
        status.put("fields", facebookApiConfig.getFields());
        status.put("limit", facebookApiConfig.getLimit());

        boolean allConfigured = facebookApiConfig.getPageId() != null && !facebookApiConfig.getPageId().isEmpty()
            && facebookApiConfig.getPageAccessToken() != null && !facebookApiConfig.getPageAccessToken().isEmpty();

        status.put("ready", allConfigured);

        if (!allConfigured) {
            status.put("message", "Missing configuration. Please set FACEBOOK_PAGE_ID and FACEBOOK_PAGE_ACCESS_TOKEN environment variables.");
        } else {
            status.put("message", "All required configurations are set. Use /test endpoint to validate API connection.");
        }

        return ResponseEntity.ok(status);
    }

    private String buildErrorGuidance(int statusCode) {
        switch (statusCode) {
            case 400:
                return "Bad Request - Possible causes:\n" +
                       "1. Invalid Page ID\n" +
                       "2. Invalid Access Token\n" +
                       "3. Token doesn't have permission to access this page\n\n" +
                       "How to fix:\n" +
                       "- Go to https://developers.facebook.com/tools/explorer/\n" +
                       "- Generate a new Page Access Token with 'pages_read_engagement' permission\n" +
                       "- Make sure you select the correct page\n" +
                       "- Copy the token and update your FACEBOOK_PAGE_ACCESS_TOKEN environment variable";

            case 190:
                return "Invalid Access Token - Your token is expired or invalid.\n" +
                       "Generate a new token at https://developers.facebook.com/tools/explorer/";

            default:
                return "Check Facebook API documentation for error code " + statusCode;
        }
    }
}
