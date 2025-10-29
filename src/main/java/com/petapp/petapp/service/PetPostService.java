package com.petapp.petapp.service;

import com.petapp.petapp.dto.*;
import com.petapp.petapp.entity.*;
import com.petapp.petapp.repository.FacebookPostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PetPostService {

    private final FacebookPostRepository facebookPostRepository;
    private final FacebookGraphApiService facebookGraphApiService;
    private final TextExtractionService textExtractionService;

    @Transactional
    public void syncPostsFromFacebook() {
        log.info("Starting Facebook posts synchronization");

        try {
            FacebookApiResponseDTO response = facebookGraphApiService.fetchPosts();

            if (response == null || response.getData() == null) {
                log.warn("No data received from Facebook API");
                return;
            }

            int savedCount = 0;
            int skippedCount = 0;

            for (FacebookPostDTO postDTO : response.getData()) {
                if (facebookPostRepository.existsByFacebookPostId(postDTO.getId())) {
                    log.debug("Post {} already exists, skipping", postDTO.getId());
                    skippedCount++;
                    continue;
                }

                FacebookPost post = convertToEntity(postDTO);
                facebookPostRepository.save(post);
                savedCount++;
                log.debug("Saved post: {}", postDTO.getId());
            }

            log.info("Synchronization completed. Saved: {}, Skipped: {}", savedCount, skippedCount);

        } catch (Exception e) {
            log.error("Error during Facebook posts synchronization", e);
            throw new RuntimeException("Failed to sync posts from Facebook", e);
        }
    }

    @Transactional
    public void syncAllPostsFromFacebook() {
        log.info("Starting full Facebook posts synchronization with pagination");

        try {
            int totalSaved = 0;
            int totalSkipped = 0;
            String cursor = null;
            boolean hasMorePages = true;

            while (hasMorePages) {
                FacebookApiResponseDTO response;

                if (cursor == null) {
                    response = facebookGraphApiService.fetchPosts();
                } else {
                    response = facebookGraphApiService.fetchPostsWithCursor(cursor);
                }

                if (response == null || response.getData() == null) {
                    log.warn("No data received from Facebook API");
                    break;
                }

                int savedCount = 0;
                int skippedCount = 0;

                for (FacebookPostDTO postDTO : response.getData()) {
                    if (facebookPostRepository.existsByFacebookPostId(postDTO.getId())) {
                        log.debug("Post {} already exists, skipping", postDTO.getId());
                        skippedCount++;
                        continue;
                    }

                    FacebookPost post = convertToEntity(postDTO);
                    facebookPostRepository.save(post);
                    savedCount++;
                    log.debug("Saved post: {}", postDTO.getId());
                }

                totalSaved += savedCount;
                totalSkipped += skippedCount;

                log.info("Page completed. Saved: {}, Skipped: {}", savedCount, skippedCount);

                // Check if there are more pages
                if (response.getPaging() != null &&
                    response.getPaging().getCursors() != null &&
                    response.getPaging().getCursors().getAfter() != null) {
                    cursor = response.getPaging().getCursors().getAfter();
                } else {
                    hasMorePages = false;
                }
            }

            log.info("Full synchronization completed. Total Saved: {}, Total Skipped: {}", totalSaved, totalSkipped);

        } catch (Exception e) {
            log.error("Error during full Facebook posts synchronization", e);
            throw new RuntimeException("Failed to sync all posts from Facebook", e);
        }
    }

    private FacebookPost convertToEntity(FacebookPostDTO postDTO) {
        FacebookPost post = new FacebookPost();
        post.setFacebookPostId(postDTO.getId());

        String description = null;

        if (postDTO.getAttachments() != null && postDTO.getAttachments().getData() != null) {
            for (FacebookAttachmentDTO attachmentDTO : postDTO.getAttachments().getData()) {
                Attachment attachment = convertAttachmentToEntity(attachmentDTO);
                post.addAttachment(attachment);

                // Capturar a primeira descrição encontrada
                if (description == null && attachmentDTO.getDescription() != null) {
                    description = attachmentDTO.getDescription();
                }
            }
        }

        // Processar automaticamente a descrição e extrair informações estruturadas
        if (description != null && !description.trim().isEmpty()) {
            try {
                PetInfo petInfo = textExtractionService.extractInformation(description);
                if (petInfo != null) {
                    petInfo.setFacebookPost(post);
                    post.setPetInfo(petInfo);
                    log.debug("Successfully extracted structured data for post: {}", postDTO.getId());
                }
            } catch (Exception e) {
                log.error("Error extracting information from post {}: {}", postDTO.getId(), e.getMessage());
                // Continua sem falhar o salvamento do post
            }
        }

        return post;
    }

    private Attachment convertAttachmentToEntity(FacebookAttachmentDTO attachmentDTO) {
        Attachment attachment = new Attachment();
        attachment.setDescription(attachmentDTO.getDescription());
        attachment.setType(attachmentDTO.getType());
        attachment.setUrl(attachmentDTO.getUrl());

        if (attachmentDTO.getMedia() != null) {
            Media media = new Media();
            if (attachmentDTO.getMedia().getImage() != null) {
                Image image = new Image();
                image.setHeight(attachmentDTO.getMedia().getImage().getHeight());
                image.setSrc(attachmentDTO.getMedia().getImage().getSrc());
                image.setWidth(attachmentDTO.getMedia().getImage().getWidth());
                media.setImage(image);
            }
            attachment.setMedia(media);
        }

        if (attachmentDTO.getTarget() != null) {
            Target target = new Target();
            target.setId(attachmentDTO.getTarget().getId());
            target.setUrl(attachmentDTO.getTarget().getUrl());
            attachment.setTarget(target);
        }

        return attachment;
    }

    public List<FacebookPost> getAllPosts() {
        return facebookPostRepository.findAll();
    }

    public FacebookPost getPostByFacebookId(String facebookPostId) {
        return facebookPostRepository.findByFacebookPostId(facebookPostId)
            .orElseThrow(() -> new RuntimeException("Post not found with Facebook ID: " + facebookPostId));
    }
}
