package com.petapp.petapp.service;

import com.petapp.petapp.config.FacebookApiConfig;
import com.petapp.petapp.dto.FacebookApiResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@RequiredArgsConstructor
@Slf4j
public class FacebookGraphApiService {

    private final FacebookApiConfig facebookApiConfig;
    private final WebClient webClient;

    public FacebookApiResponseDTO fetchPosts() {
        validateConfiguration();

        try {
            String url = String.format("%s/%s/posts",
                facebookApiConfig.getBaseUrl(),
                facebookApiConfig.getPageId());

            log.info("Fetching posts from Facebook Graph API: {}", url);
            log.debug("Request params - fields: {}, limit: {}",
                facebookApiConfig.getFields(), facebookApiConfig.getLimit());

            FacebookApiResponseDTO response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path(String.format("/%s/posts", facebookApiConfig.getPageId()))
                    .queryParam("fields", facebookApiConfig.getFields())
                    .queryParam("limit", facebookApiConfig.getLimit())
                    .queryParam("access_token", facebookApiConfig.getPageAccessToken())
                    .build())
                .retrieve()
                .bodyToMono(FacebookApiResponseDTO.class)
                .block();

            log.info("Successfully fetched {} posts from Facebook",
                response != null && response.getData() != null ? response.getData().size() : 0);

            return response;

        } catch (WebClientResponseException e) {
            String errorBody = e.getResponseBodyAsString();
            log.error("=========================================");
            log.error("Facebook API Error - Status: {}", e.getStatusCode());
            log.error("Error Response Body: {}", errorBody);
            log.error("Request URL: {}/{}/posts", facebookApiConfig.getBaseUrl(), facebookApiConfig.getPageId());
            log.error("=========================================");

            String userMessage = buildUserFriendlyErrorMessage(e.getStatusCode().value(), errorBody);
            throw new RuntimeException(userMessage, e);
        } catch (Exception e) {
            log.error("Unexpected error fetching posts from Facebook", e);
            throw new RuntimeException("Failed to fetch posts from Facebook: " + e.getMessage(), e);
        }
    }

    private void validateConfiguration() {
        if (facebookApiConfig.getPageId() == null || facebookApiConfig.getPageId().isEmpty()) {
            throw new IllegalStateException("FACEBOOK_PAGE_ID is not configured. Please set it in your environment variables.");
        }
        if (facebookApiConfig.getPageAccessToken() == null || facebookApiConfig.getPageAccessToken().isEmpty()) {
            throw new IllegalStateException("FACEBOOK_PAGE_ACCESS_TOKEN is not configured. Please set it in your environment variables.");
        }
        log.debug("Facebook API configuration validated - Page ID: {}, Token length: {}",
            facebookApiConfig.getPageId(),
            facebookApiConfig.getPageAccessToken() != null ? facebookApiConfig.getPageAccessToken().length() : 0);
    }

    private String buildUserFriendlyErrorMessage(int statusCode, String errorBody) {
        switch (statusCode) {
            case 400:
                return "Facebook API Error (400 Bad Request): " +
                       "Invalid access token or missing permissions. " +
                       "Please verify:\n" +
                       "1. Your FACEBOOK_PAGE_ACCESS_TOKEN is valid and not expired\n" +
                       "2. The token has 'pages_read_engagement' and 'pages_show_list' permissions\n" +
                       "3. Your PAGE_ID is correct\n" +
                       "Error details: " + errorBody;
            case 401:
                return "Facebook API Error (401 Unauthorized): Invalid or expired access token. " +
                       "Error details: " + errorBody;
            case 403:
                return "Facebook API Error (403 Forbidden): Insufficient permissions. " +
                       "Make sure your token has the required permissions. " +
                       "Error details: " + errorBody;
            case 404:
                return "Facebook API Error (404 Not Found): Page not found. " +
                       "Verify your PAGE_ID is correct. " +
                       "Error details: " + errorBody;
            default:
                return "Facebook API Error (" + statusCode + "): " + errorBody;
        }
    }

    public FacebookApiResponseDTO fetchPostsWithCursor(String cursor) {
        try {
            log.info("Fetching posts from Facebook Graph API with cursor");

            FacebookApiResponseDTO response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path(String.format("/%s/posts", facebookApiConfig.getPageId()))
                    .queryParam("fields", facebookApiConfig.getFields())
                    .queryParam("limit", facebookApiConfig.getLimit())
                    .queryParam("after", cursor)
                    .queryParam("access_token", facebookApiConfig.getPageAccessToken())
                    .build())
                .retrieve()
                .bodyToMono(FacebookApiResponseDTO.class)
                .block();

            log.info("Successfully fetched {} posts from Facebook with cursor",
                response != null && response.getData() != null ? response.getData().size() : 0);

            return response;

        } catch (WebClientResponseException e) {
            log.error("Error fetching posts with cursor from Facebook Graph API. Status: {}, Response: {}",
                e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to fetch posts from Facebook: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error fetching posts with cursor from Facebook", e);
            throw new RuntimeException("Failed to fetch posts from Facebook: " + e.getMessage(), e);
        }
    }
}
