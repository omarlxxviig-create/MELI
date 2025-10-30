package com.meli.inventory_service.infrastructure.rest;

import com.meli.inventory_service.application.service.BookingUseCase;
import com.meli.inventory_service.domain.model.ServicePost;
import com.meli.inventory_service.domain.ports.in.CreateServicePostCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador REST para gestión de publicaciones de servicios
 */
@RestController
@RequestMapping("/api/v1/posts")
@Tag(name = "Service Posts", description = "Gestión de publicaciones de servicios de transporte")
public class PostController {

    private static final Logger logger = LoggerFactory.getLogger(PostController.class);

    private final BookingUseCase bookingUseCase;

    public PostController(BookingUseCase bookingUseCase) {
        this.bookingUseCase = bookingUseCase;
    }

    @PostMapping
    @Operation(summary = "Crear nueva publicación de servicio")
    public ResponseEntity<ServicePostResponse> createPost(@Valid @RequestBody CreatePostRequest request) {
        logger.info("POST /api/v1/posts - Creating new service post");

        CreateServicePostCommand command = new CreateServicePostCommand(
                request.getOwnerId(),
                request.getOrigin(),
                request.getDestination(),
                request.getDepartureDateTime(),
                request.getSeatsTotal(),
                request.getPrice());
        command.setDescription(request.getDescription());

        ServicePost post = bookingUseCase.createPost(command);

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(post));
    }

    @PutMapping("/{id}/publish")
    @PreAuthorize("hasRole('ADMIN') or @postAuthorizationHandler.isOwner(#id)")
    @Operation(summary = "Publicar un post")
    public ResponseEntity<?> publishPost(@PathVariable String id) {
        logger.info("PUT /api/v1/posts/{}/publish - Publishing post", id);

        ServicePost post = bookingUseCase.publishPost(id);

        return ResponseEntity.ok(toResponse(post));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener detalle de una publicación")
    public ResponseEntity<ServicePostResponse> getPost(@PathVariable String id) {
        logger.info("GET /api/v1/posts/{} - Retrieving post", id);

        return bookingUseCase.getPostById(id)
                .map(post -> ResponseEntity.ok(toResponse(post)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Listar todas las publicaciones disponibles")
    public ResponseEntity<List<ServicePostResponse>> listPosts() {
        logger.info("GET /api/v1/posts - Listing all available posts");

        List<ServicePost> posts = bookingUseCase.listAvailablePosts();
        List<ServicePostResponse> response = posts.stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar publicaciones por ruta y fecha")
    public ResponseEntity<List<ServicePostResponse>> searchPosts(
            @RequestParam String origin,
            @RequestParam String destination,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate) {

        logger.info("GET /api/v1/posts/search - Searching posts: {} -> {}", origin, destination);

        LocalDateTime from = fromDate != null ? fromDate : LocalDateTime.now();
        LocalDateTime to = toDate != null ? toDate : LocalDateTime.now().plusDays(30);

        List<ServicePost> posts = bookingUseCase.searchPosts(origin, destination, from, to);
        List<ServicePostResponse> response = posts.stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    private ServicePostResponse toResponse(ServicePost post) {
        ServicePostResponse response = new ServicePostResponse();
        response.setId(post.getId());
        response.setOwnerId(post.getOwnerId());
        response.setOrigin(post.getOrigin());
        response.setDestination(post.getDestination());
        response.setDepartureDateTime(post.getDepartureDateTime());
        response.setSeatsTotal(post.getSeatsTotal());
        response.setSeatsAvailable(post.getSeatsAvailable());
        response.setPrice(post.getPrice());
        response.setDescription(post.getDescription());
        response.setStatus(post.getStatus().name());
        response.setCreatedAt(post.getCreatedAt());
        response.setUpdatedAt(post.getUpdatedAt());
        return response;
    }

    private static class ErrorResponse {
        private String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
