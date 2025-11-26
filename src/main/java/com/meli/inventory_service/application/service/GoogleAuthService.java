package com.meli.inventory_service.application.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.meli.inventory_service.domain.model.Role;
import com.meli.inventory_service.domain.model.User;
import com.meli.inventory_service.infrastructure.persistence.spring.RoleRepository;
import com.meli.inventory_service.infrastructure.persistence.spring.UserRepository;
import com.meli.inventory_service.infrastructure.rest.dto.GoogleTokenInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Service
@Slf4j
public class GoogleAuthService {

    private static final String GOOGLE_CLIENT_ID = "84509552893-95o3to1q8s28ajflh7mgm7n9c8obsstr.apps.googleusercontent.com";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private final GoogleIdTokenVerifier verifier;

    public GoogleAuthService() {
        this.verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                new GsonFactory())
                .setAudience(Collections.singletonList(GOOGLE_CLIENT_ID))
                .build();
    }

    /**
     * Valida el ID token de Google y extrae la información del usuario.
     *
     * @param idToken el token ID de Google
     * @return información del usuario extraída del token
     * @throws IllegalArgumentException si el token es inválido
     */
    public GoogleTokenInfo validateGoogleToken(String idToken) {
        log.info("Validating Google ID token...");

        try {
            GoogleIdToken googleIdToken = verifier.verify(idToken);

            if (googleIdToken == null) {
                log.error("Invalid Google ID token: token verification failed");
                throw new IllegalArgumentException("Invalid ID token: verification failed");
            }

            GoogleIdToken.Payload payload = googleIdToken.getPayload();

            // Verificar que el audience coincida exactamente con nuestro CLIENT_ID
            String audience = payload.getAudience().toString();
            if (!GOOGLE_CLIENT_ID.equals(audience)) {
                log.error("Token audience mismatch. Expected: {}, Got: {}", GOOGLE_CLIENT_ID, audience);
                throw new IllegalArgumentException("Invalid ID token: audience mismatch");
            }

            // Extraer información del usuario
            String googleUid = payload.getSubject();
            String email = payload.getEmail();
            Boolean emailVerified = payload.getEmailVerified();
            String name = (String) payload.get("name");
            String pictureUrl = (String) payload.get("picture");

            log.info("Token validated successfully for user: {} ({})", email, googleUid);

            GoogleTokenInfo tokenInfo = new GoogleTokenInfo(
                    googleUid,
                    email,
                    name != null ? name : email.split("@")[0],
                    pictureUrl,
                    emailVerified != null ? emailVerified : false);

            return tokenInfo;

        } catch (GeneralSecurityException e) {
            log.error("Security error validating Google token: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Invalid ID token: security verification failed", e);
        } catch (IOException e) {
            log.error("IO error validating Google token: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Invalid ID token: unable to verify", e);
        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Busca o crea un usuario basado en la información de Google.
     * Si el usuario existe con contraseña local, mantiene la contraseña.
     *
     * @param tokenInfo información del token de Google
     * @return el usuario encontrado o creado
     */
    @Transactional
    public User loginOrCreateUserFromGoogle(GoogleTokenInfo tokenInfo) {
        log.info("Processing Google login for user: {}", tokenInfo.getEmail());

        // Buscar primero por googleSub
        User user = userRepository.findByGoogleSub(tokenInfo.getGoogleUid()).orElse(null);

        if (user == null) {
            // Si no existe por googleSub, buscar por email
            user = userRepository.findByEmail(tokenInfo.getEmail()).orElse(null);

            if (user != null) {
                // Usuario existe con email pero sin googleSub, actualizar
                log.info("Linking existing user {} with Google account", user.getUsername());
                user.setGoogleSub(tokenInfo.getGoogleUid());
                if (tokenInfo.getPicture() != null) {
                    user.setPictureUrl(tokenInfo.getPicture());
                }
            } else {
                // Usuario no existe, crear nuevo
                log.info("Creating new user from Google account: {}", tokenInfo.getEmail());
                user = createUserFromGoogle(tokenInfo);
            }
        } else {
            // Usuario existe por googleSub, actualizar información
            log.info("Updating existing Google user: {}", user.getUsername());
            user.setEmail(tokenInfo.getEmail());
            if (tokenInfo.getName() != null) {
                updateUserName(user, tokenInfo.getName());
            }
            if (tokenInfo.getPicture() != null) {
                user.setPictureUrl(tokenInfo.getPicture());
            }
        }

        // Actualizar último login
        user.setLastLogin(LocalDateTime.now());
        user = userRepository.save(user);

        log.info("Google authentication successful for user: {} (ID: {})", user.getUsername(), user.getId());
        return user;
    }

    private User createUserFromGoogle(GoogleTokenInfo tokenInfo) {
        User user = new User();

        // Username: email sin dominio o email completo si hay conflicto
        String baseUsername = tokenInfo.getEmail().split("@")[0];
        String username = baseUsername;
        int suffix = 1;
        while (userRepository.existsByUsername(username)) {
            username = baseUsername + suffix;
            suffix++;
        }

        user.setUsername(username);
        user.setEmail(tokenInfo.getEmail());
        user.setGoogleSub(tokenInfo.getGoogleUid());
        updateUserName(user, tokenInfo.getName());
        user.setPictureUrl(tokenInfo.getPicture());

        // Sin contraseña local (login solo por Google)
        user.setPassword(null);

        // Configuración de cuenta
        user.setEnabled(true);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        user.setEmailVerified(tokenInfo.isEmailVerified());

        // Asignar rol USER por defecto
        Role userRole = roleRepository.findByName(Role.RoleName.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Default role ROLE_USER not found"));
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);

        return user;
    }

    private void updateUserName(User user, String fullName) {
        if (fullName != null && !fullName.trim().isEmpty()) {
            String[] parts = fullName.trim().split("\\s+", 2);
            user.setFirstName(parts[0]);
            if (parts.length > 1) {
                user.setLastName(parts[1]);
            } else {
                user.setLastName("");
            }
        }
    }
}
