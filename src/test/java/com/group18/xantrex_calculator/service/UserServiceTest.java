package com.group18.xantrex_calculator.service;

import com.group18.xantrex_calculator.entity.Role;
import com.group18.xantrex_calculator.entity.User;
import com.group18.xantrex_calculator.exception.UserAlreadyExistsException;
import com.group18.xantrex_calculator.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void registerHashesPassword() {
        when(passwordEncoder.encode("raw")).thenReturn("hashed");
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        userService.register("user@example.com", "raw");
        verify(userRepository).save(captor.capture());

        assertEquals("hashed", captor.getValue().getPassword(),
                "Stored password must be the BCrypt-encoded value, not the raw input");
    }

    @Test
    void registerAssignsInternRoleForSfuEmail() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("pass")).thenReturn("hashed");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        userService.register("student@sfu.ca", "pass");
        verify(userRepository).save(captor.capture());

        assertEquals(Role.INTERN, captor.getValue().getRole(),
                "@sfu.ca addresses should be assigned Role.INTERN");
    }

    @Test
    void registerAssignsClientRoleForOtherEmail() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("pass")).thenReturn("hashed");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        userService.register("user@gmail.com", "pass");
        verify(userRepository).save(captor.capture());

        assertEquals(Role.CLIENT, captor.getValue().getRole(),
                "Non-SFU addresses should be assigned Role.CLIENT");
    }

    @Test
    void registerThrowsOnDuplicateEmail() {
        when(userRepository.findByEmail("dup@example.com")).thenReturn(Optional.of(new User()));

        assertThrows(UserAlreadyExistsException.class,
                () -> userService.register("dup@example.com", "pass"),
                "register() must throw UserAlreadyExistsException when email already exists");
    }

    @Test
    void loadUserByUsernameReturnsUserDetails() {
        User user = new User();
        user.setEmail("found@example.com");
        user.setPassword("hashed");
        user.setRole(Role.CLIENT);

        when(userRepository.findByEmail("found@example.com")).thenReturn(Optional.of(user));

        UserDetails details = userService.loadUserByUsername("found@example.com");

        assertEquals("found@example.com", details.getUsername(),
                "Returned UserDetails username must match the queried email");
    }

    @Test
    void loadUserByUsernameThrowsForMissingUser() {
        when(userRepository.findByEmail("nope@example.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> userService.loadUserByUsername("nope@example.com"),
                "loadUserByUsername() must throw UsernameNotFoundException for unknown emails");
    }
}
