package com.group18.xantrex_calculator.controller;

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
                "new@gmail.com",
                "pass",
                mock(RedirectAttributes.class)
        );

        assertEquals("redirect:/?registered", result);
    }

    @Test
    void registerUser_withDuplicateEmail_redirectsToRegistrationError() {
        doThrow(new UserAlreadyExistsException("dup"))
                .when(userService).register(eq("dup@example.com"), any());

        String result = registerController.registerUser(
                "dup@example.com",
                "pass",
                mock(RedirectAttributes.class)
        );

        assertEquals("redirect:/?registrationError", result);
    }

    @Test
    void registerUser_withSfuEmail_savesUserWithInternRole() {
        doNothing().when(userService).register(any(), any());

        String result = registerController.registerUser(
                "student@sfu.ca",
                "pass",
                mock(RedirectAttributes.class)
        );

        verify(userService).register(eq("student@sfu.ca"), eq("pass"));
        assertEquals("redirect:/?registered", result);
    }

    @Test
    void registerUser_withGmailEmail_savesUserWithClientRole() {
        doNothing().when(userService).register(any(), any());

        String result = registerController.registerUser(
                "user@gmail.com",
                "pass",
                mock(RedirectAttributes.class)
        );

        verify(userService).register(eq("user@gmail.com"), eq("pass"));
        assertEquals("redirect:/?registered", result);
    }
}
