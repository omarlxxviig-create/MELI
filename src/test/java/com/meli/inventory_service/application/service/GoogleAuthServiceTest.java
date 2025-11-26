package com.meli.inventory_service.application.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.meli.inventory_service.domain.model.Role;
import com.meli.inventory_service.domain.model.User;
import com.meli.inventory_service.infrastructure.persistence.spring.RoleRepository;
import com.meli.inventory_service.infrastructure.persistence.spring.UserRepository;
import com.meli.inventory_service.infrastructure.rest.dto.GoogleTokenInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoogleAuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private GoogleAuthService googleAuthService;

    private Role userRole;

    @BeforeEach
    void setUp() {
        userRole = new Role();
        userRole.setId(1L);
        userRole.setName(Role.RoleName.ROLE_USER);
    }

    @Test
    void testLoginOrCreateUserFromGoogle_NewUser() {
        // Arrange
        GoogleTokenInfo tokenInfo = new GoogleTokenInfo(
                "google-sub-123",
                "test@gmail.com",
                "Test User",
                "https://picture.url",
                true);

        when(userRepository.findByGoogleSub(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(roleRepository.findByName(Role.RoleName.ROLE_USER)).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        // Act
        User result = googleAuthService.loginOrCreateUserFromGoogle(tokenInfo);

        // Assert
        assertNotNull(result);
        assertEquals("test@gmail.com", result.getEmail());
        assertEquals("google-sub-123", result.getGoogleSub());
        assertEquals("Test", result.getFirstName());
        assertEquals("User", result.getLastName());
        // assertTrue(result.isEnabled());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testLoginOrCreateUserFromGoogle_ExistingUserByGoogleSub() {
        // Arrange
        GoogleTokenInfo tokenInfo = new GoogleTokenInfo(
                "google-sub-123",
                "test@gmail.com",
                "Test User Updated",
                "https://picture.url",
                true);

        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setUsername("testuser");
        existingUser.setEmail("old@gmail.com");
        existingUser.setGoogleSub("google-sub-123");
        existingUser.setRoles(new HashSet<>(Set.of(userRole)));

        when(userRepository.findByGoogleSub("google-sub-123")).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = googleAuthService.loginOrCreateUserFromGoogle(tokenInfo);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("test@gmail.com", result.getEmail());
        assertEquals("Test", result.getFirstName());
        assertEquals("User Updated", result.getLastName());
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    void testLoginOrCreateUserFromGoogle_ExistingUserByEmail() {
        // Arrange
        GoogleTokenInfo tokenInfo = new GoogleTokenInfo(
                "google-sub-123",
                "test@gmail.com",
                "Test User",
                "https://picture.url",
                true);

        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setUsername("testuser");
        existingUser.setEmail("test@gmail.com");
        existingUser.setPassword("hashed-password");
        existingUser.setRoles(new HashSet<>(Set.of(userRole)));

        when(userRepository.findByGoogleSub(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = googleAuthService.loginOrCreateUserFromGoogle(tokenInfo);

        // Assert
        assertNotNull(result);
        assertEquals("google-sub-123", result.getGoogleSub());
        assertEquals("hashed-password", result.getPassword()); // Contraseña local se
        // mantiene
        verify(userRepository, times(1)).save(existingUser);
    }
}
