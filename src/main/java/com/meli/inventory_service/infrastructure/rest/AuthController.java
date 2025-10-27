package com.meli.inventory_service.infrastructure.rest;

import com.meli.inventory_service.domain.model.Role;
import com.meli.inventory_service.domain.model.User;
import com.meli.inventory_service.infrastructure.persistence.spring.RoleRepository;
import com.meli.inventory_service.infrastructure.persistence.spring.UserRepository;
import com.meli.inventory_service.infrastructure.rest.dto.*;
import com.meli.inventory_service.infrastructure.security.jwt.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Controlador REST para autenticación y gestión de usuarios.
 * Proporciona endpoints para login, registro y refresh de tokens.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints para autenticación y registro de usuarios")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    /**
     * Endpoint para login de usuarios.
     *
     * @param loginRequest datos de login
     * @return respuesta con tokens JWT
     */
    @PostMapping("/login")
    @Operation(summary = "Login de usuario", description = "Autentica un usuario y retorna tokens JWT")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // Autenticar usuario
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Obtener token desde caché o generar nuevo
            LoginResponse response = getCachedLoginResponse(loginRequest.getUsername(), authentication);

            // Actualizar último login (async para no bloquear)
            updateLastLoginAsync(loginRequest.getUsername());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid username or password");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    @Cacheable(value = "tokens", key = "#username", unless = "#result == null")
    public LoginResponse getCachedLoginResponse(String username, Authentication authentication) {
        // Obtener usuario
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Extraer roles
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // Generar tokens
        String accessToken = tokenProvider.generateTokenFromUsername(user.getUsername(), roles);
        String refreshToken = tokenProvider.generateRefreshToken(user.getUsername());

        return new LoginResponse(
                accessToken,
                refreshToken,
                user.getUsername(),
                user.getEmail(),
                roles);
    }

    private void updateLastLoginAsync(String username) {
        // Ejecutar en thread separado para no bloquear
        CompletableFuture.runAsync(() -> {
            try {
                User user = userRepository.findByUsername(username).orElse(null);
                if (user != null) {
                    user.setLastLogin(java.time.LocalDateTime.now());
                    userRepository.save(user);
                }
            } catch (Exception e) {
                // Log error pero no fallar el login
            }
        });
    }

    /**
     * Endpoint para registro de nuevos usuarios.
     *
     * @param registerRequest datos de registro
     * @return respuesta con mensaje de éxito
     */
    @PostMapping("/register")
    @CacheEvict(value = "tokens", allEntries = true)
    @Operation(summary = "Registro de usuario", description = "Registra un nuevo usuario en el sistema")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            // Verificar si el usuario ya existe (ANTES de intentar crear)
            if (userRepository.existsByUsername(registerRequest.getUsername())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Username already exists");
                error.put("username", registerRequest.getUsername());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            if (userRepository.existsByEmail(registerRequest.getEmail())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Email already in use");
                error.put("email", registerRequest.getEmail());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            // Crear nuevo usuario
            User user = new User();
            user.setUsername(registerRequest.getUsername());
            user.setEmail(registerRequest.getEmail());
            user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
            user.setFirstName(registerRequest.getFirstName());
            user.setLastName(registerRequest.getLastName());
            user.setEnabled(true);
            user.setAccountNonExpired(true);
            user.setAccountNonLocked(true);
            user.setCredentialsNonExpired(true);

            // Asignar roles
            Set<Role> roles = new HashSet<>();
            if (registerRequest.getRoles() != null && !registerRequest.getRoles().isEmpty()) {
                registerRequest.getRoles().forEach(roleName -> {
                    try {
                        Role.RoleName roleEnum = Role.RoleName.valueOf(roleName);
                        Role role = roleRepository.findByName(roleEnum)
                                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
                        roles.add(role);
                    } catch (IllegalArgumentException e) {
                        throw new RuntimeException("Invalid role: " + roleName);
                    }
                });
            } else {
                // Asignar rol USER por defecto
                Role userRole = roleRepository.findByName(Role.RoleName.ROLE_USER)
                        .orElseThrow(() -> new RuntimeException("Default role not found"));
                roles.add(userRole);
            }

            user.setRoles(roles);

            // Guardar usuario con manejo de excepciones de unicidad
            try {
                userRepository.save(user);
            } catch (org.springframework.dao.DataIntegrityViolationException e) {
                // Capturar violación de constraint de unicidad
                Map<String, String> error = new HashMap<>();
                error.put("error", "User already exists");
                error.put("detail", "Username or email is already registered");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            Map<String, String> response = new HashMap<>();
            response.put("message", "User registered successfully");
            response.put("username", user.getUsername());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Registration failed");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Endpoint para renovar el access token usando el refresh token.
     *
     * @param refreshTokenRequest petición con refresh token
     * @return respuesta con nuevo access token
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Renueva el access token usando el refresh token")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        try {
            String refreshToken = refreshTokenRequest.getRefreshToken();

            // Validar refresh token
            if (!tokenProvider.validateToken(refreshToken) || !tokenProvider.isRefreshToken(refreshToken)) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid refresh token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            // Obtener username del token
            String username = tokenProvider.getUsernameFromToken(refreshToken);

            // Cargar usuario
            User user = userRepository.findByUsernameWithRoles(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Extraer roles
            List<String> roles = user.getRoles().stream()
                    .map(role -> role.getName().name())
                    .collect(Collectors.toList());

            // Generar nuevo access token
            String newAccessToken = tokenProvider.generateTokenFromUsername(username, roles);

            Map<String, String> response = new HashMap<>();
            response.put("accessToken", newAccessToken);
            response.put("tokenType", "Bearer");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Token refresh failed");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    /**
     * Endpoint para obtener información del usuario actual.
     *
     * @return información del usuario
     */
    @GetMapping("/me")
    @Operation(summary = "Usuario actual", description = "Obtiene información del usuario autenticado")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
            }

            String username = authentication.getName();
            User user = userRepository.findByUsernameWithRoles(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());
            response.put("firstName", user.getFirstName());
            response.put("lastName", user.getLastName());
            response.put("roles", user.getRoles().stream()
                    .map(role -> role.getName().name())
                    .collect(Collectors.toList()));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get user information");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
