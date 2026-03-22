package com.group18.xantrex_calculator.controller;

import com.group18.xantrex_calculator.exception.InvalidDomainException;
import com.group18.xantrex_calculator.exception.UserAlreadyExistsException;
import com.group18.xantrex_calculator.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private RegisterController registerController;

    @Test
    void registerUser_withNewEmail_redirectsToRegistered() {
        doNothing().when(userService).register(any(), any());

        String result = registerController.registerUser(
                "new@xantrex.com",
                "pass",
                mock(RedirectAttributes.class)
        );

        assertEquals("redirect:/login?registered", result);
    }

    @Test
    void registerUser_withDuplicateEmail_redirectsToRegistrationError() {
        doThrow(new UserAlreadyExistsException("dup"))
                .when(userService).register(eq("dup@xantrex.com"), any());

        String result = registerController.registerUser(
                "dup@xantrex.com",
                "pass",
                mock(RedirectAttributes.class)
        );

        assertEquals("redirect:/login?registrationError", result);
    }

    @Test
    void registerUser_withXantrexEmail_savesUser() {
        doNothing().when(userService).register(any(), any());

        String result = registerController.registerUser(
                "employee@xantrex.com",
                "pass",
                mock(RedirectAttributes.class)
        );

        verify(userService).register(eq("employee@xantrex.com"), eq("pass"));
        assertEquals("redirect:/login?registered", result);
    }

    @Test
    void registerUser_withNonXantrexEmail_redirectsToDomainError() {
        doThrow(new InvalidDomainException("bad domain"))
                .when(userService).register(eq("user@gmail.com"), any());

        String result = registerController.registerUser(
                "user@gmail.com",
                "pass",
                mock(RedirectAttributes.class)
        );

        assertEquals("redirect:/login?domainError", result);
    }
}
