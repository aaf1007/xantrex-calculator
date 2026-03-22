package com.group18.xantrex_calculator.service;

import com.group18.xantrex_calculator.entity.Role;
import com.group18.xantrex_calculator.entity.User;
import com.group18.xantrex_calculator.exception.InvalidDomainException;
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
        userService.register("user@xantrex.com", "raw");
        verify(userRepository).save(captor.capture());

        assertEquals("hashed", captor.getValue().getPassword(),
                "Stored password must be the BCrypt-encoded value, not the raw input");
    }

    @Test
    void registerAssignsInternRoleForXantrexEmail() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("pass")).thenReturn("hashed");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        userService.register("user@xantrex.com", "pass");
        verify(userRepository).save(captor.capture());

        assertEquals(Role.INTERN, captor.getValue().getRole(),
                "@xantrex.com addresses should be assigned Role.INTERN");
    }

    @Test
    void registerThrowsForNonXantrexEmail() {
        assertThrows(InvalidDomainException.class,
                () -> userService.register("user@gmail.com", "pass"),
                "register() must throw InvalidDomainException for non-xantrex emails");
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerThrowsOnDuplicateEmail() {
        when(userRepository.findByEmail("dup@xantrex.com")).thenReturn(Optional.of(new User()));

        assertThrows(UserAlreadyExistsException.class,
                () -> userService.register("dup@xantrex.com", "pass"),
                "register() must throw UserAlreadyExistsException when email already exists");
    }

    @Test
    void loadUserByUsernameReturnsUserDetails() {
        User user = new User();
        user.setEmail("found@xantrex.com");
        user.setPassword("hashed");
        user.setRole(Role.INTERN);

        when(userRepository.findByEmail("found@xantrex.com")).thenReturn(Optional.of(user));

        UserDetails details = userService.loadUserByUsername("found@xantrex.com");

        assertEquals("found@xantrex.com", details.getUsername(),
                "Returned UserDetails username must match the queried email");
    }

    @Test
    void loadUserByUsernameThrowsForMissingUser() {
        when(userRepository.findByEmail("nope@xantrex.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> userService.loadUserByUsername("nope@xantrex.com"),
                "loadUserByUsername() must throw UsernameNotFoundException for unknown emails");
    }

    @Test
    void loadUserByUsernameThrowsForNonXantrexEmail() {
        assertThrows(UsernameNotFoundException.class,
                () -> userService.loadUserByUsername("user@gmail.com"),
                "loadUserByUsername() must throw UsernameNotFoundException for non-xantrex emails");
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    void registerAcceptsMixedCaseXantrexEmail() {
        when(userRepository.findByEmail("user@xantrex.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("pass")).thenReturn("hashed");

        assertDoesNotThrow(() -> userService.register("User@XANTREX.COM", "pass"),
                "register() must accept mixed-case @xantrex.com emails");

        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerStoresEmailAsLowercase() {
        when(userRepository.findByEmail("user@xantrex.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("pass")).thenReturn("hashed");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        userService.register("User@Xantrex.COM", "pass");
        verify(userRepository).save(captor.capture());

        assertEquals("user@xantrex.com", captor.getValue().getEmail(),
                "Email must be stored lowercase regardless of input case");
    }

    @Test
    void loadUserByUsernameAcceptsMixedCaseEmail() {
        User user = new User();
        user.setEmail("user@xantrex.com");
        user.setPassword("hashed");
        user.setRole(Role.INTERN);

        when(userRepository.findByEmail("user@xantrex.com")).thenReturn(Optional.of(user));

        assertDoesNotThrow(() -> userService.loadUserByUsername("User@XANTREX.COM"),
                "loadUserByUsername() must accept mixed-case @xantrex.com emails");
    }
}
