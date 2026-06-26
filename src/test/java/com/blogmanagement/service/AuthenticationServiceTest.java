package com.blogmanagement.service;

import com.blogmanagement.dto.AuthResponseDTO;
import com.blogmanagement.dto.LoginRequestDTO;
import com.blogmanagement.dto.RegisterRequestDTO;
import com.blogmanagement.entity.Role;
import com.blogmanagement.entity.User;
import com.blogmanagement.exception.UserAlreadyExistsException;
import com.blogmanagement.repository.UserRepository;
import com.blogmanagement.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationService authenticationService;

    private User sampleUser;
    private RegisterRequestDTO registerRequest;
    private LoginRequestDTO loginRequest;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .passwordHash("encodedPassword")
                .role(Role.USER)
                .build();

        registerRequest = new RegisterRequestDTO("John Doe", "john@example.com", "password123", Role.USER);
        loginRequest = new LoginRequestDTO("john@example.com", "password123");
    }

    @Test
    void register_ShouldRegisterUserAndReturnToken_WhenEmailIsUnique() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);
        when(tokenProvider.generateToken(sampleUser.getEmail())).thenReturn("jwt-token");

        AuthResponseDTO response = authenticationService.register(registerRequest);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals("john@example.com", response.getUser().getEmail());
        verify(userRepository, times(1)).existsByEmail(registerRequest.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_ShouldThrowUserAlreadyExistsException_WhenEmailExists() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> authenticationService.register(registerRequest));
        verify(userRepository, times(1)).existsByEmail(registerRequest.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_ShouldAuthenticateUserAndReturnToken_WhenCredentialsAreValid() {
        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(tokenProvider.generateToken(auth)).thenReturn("jwt-token");
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(sampleUser));

        AuthResponseDTO response = authenticationService.login(loginRequest);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("john@example.com", response.getUser().getEmail());
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, times(1)).findByEmail(loginRequest.getEmail());
    }
}
