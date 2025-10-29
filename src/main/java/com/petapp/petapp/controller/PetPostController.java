package com.petapp.petapp.controller;

import com.petapp.petapp.entity.FacebookPost;
import com.petapp.petapp.service.PetPostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pet-posts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Pet Posts", description = "Endpoints para gerenciar posts de pets do Facebook")
public class PetPostController {

    private final PetPostService petPostService;

    @Operation(summary = "Sincronizar posts do Facebook",
               description = "Busca a primeira página de posts da API do Facebook e salva no banco de dados")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Posts sincronizados com sucesso"),
        @ApiResponse(responseCode = "500", description = "Erro ao sincronizar posts")
    })
    @PostMapping("/sync")
    public ResponseEntity<Map<String, String>> syncPosts() {
        log.info("Received request to sync posts from Facebook");
        try {
            petPostService.syncPostsFromFacebook();
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Posts synchronized successfully"
            ));
        } catch (Exception e) {
            log.error("Error syncing posts", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", "Failed to sync posts: " + e.getMessage()
            ));
        }
    }

    @Operation(summary = "Sincronizar todos os posts do Facebook",
               description = "Busca todas as páginas de posts da API do Facebook com paginação e salva no banco de dados")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Todos os posts sincronizados com sucesso"),
        @ApiResponse(responseCode = "500", description = "Erro ao sincronizar todos os posts")
    })
    @PostMapping("/sync-all")
    public ResponseEntity<Map<String, String>> syncAllPosts() {
        log.info("Received request to sync all posts from Facebook with pagination");
        try {
            petPostService.syncAllPostsFromFacebook();
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "All posts synchronized successfully"
            ));
        } catch (Exception e) {
            log.error("Error syncing all posts", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", "Failed to sync all posts: " + e.getMessage()
            ));
        }
    }

    @Operation(summary = "Listar todos os posts",
               description = "Retorna todos os posts de pets salvos no banco de dados")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Posts retornados com sucesso",
                     content = @Content(mediaType = "application/json",
                                        schema = @Schema(implementation = FacebookPost.class))),
        @ApiResponse(responseCode = "500", description = "Erro ao buscar posts")
    })
    @GetMapping
    public ResponseEntity<List<FacebookPost>> getAllPosts() {
        log.info("Received request to get all posts");
        try {
            List<FacebookPost> posts = petPostService.getAllPosts();
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            log.error("Error fetching posts", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Buscar post por ID do Facebook",
               description = "Retorna um post específico usando o ID do Facebook")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Post encontrado com sucesso",
                     content = @Content(mediaType = "application/json",
                                        schema = @Schema(implementation = FacebookPost.class))),
        @ApiResponse(responseCode = "404", description = "Post não encontrado"),
        @ApiResponse(responseCode = "500", description = "Erro ao buscar post")
    })
    @GetMapping("/{facebookPostId}")
    public ResponseEntity<FacebookPost> getPostByFacebookId(
            @Parameter(description = "ID do post no Facebook", required = true)
            @PathVariable String facebookPostId) {
        log.info("Received request to get post by Facebook ID: {}", facebookPostId);
        try {
            FacebookPost post = petPostService.getPostByFacebookId(facebookPostId);
            return ResponseEntity.ok(post);
        } catch (RuntimeException e) {
            log.error("Post not found: {}", facebookPostId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error fetching post", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
