package com.blogmanagement.service;

import com.blogmanagement.entity.Role;
import com.blogmanagement.entity.User;
import com.blogmanagement.exception.ResourceNotFoundException;
import com.blogmanagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L)
                .name("Jane Doe")
                .email("jane@example.com")
                .passwordHash("encodedPassword")
                .role(Role.USER)
                .build();
    }

    @Test
    void getUserByEmail_ShouldReturnUser_WhenUserExists() {
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(sampleUser));

        User result = userService.getUserByEmail("jane@example.com");

        assertNotNull(result);
        assertEquals("jane@example.com", result.getEmail());
        assertEquals("Jane Doe", result.getName());
        verify(userRepository, times(1)).findByEmail("jane@example.com");
    }

    @Test
    void getUserByEmail_ShouldThrowResourceNotFoundException_WhenUserDoesNotExist() {
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserByEmail("nonexistent@example.com"));
        verify(userRepository, times(1)).findByEmail("nonexistent@example.com");
    }

    @Test
    void getUserById_ShouldReturnUser_WhenUserExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

        User result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getUserById_ShouldThrowResourceNotFoundException_WhenUserDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(99L));
        verify(userRepository, times(1)).findById(99L);
    }
}
