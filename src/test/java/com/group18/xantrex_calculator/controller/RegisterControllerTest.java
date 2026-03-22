package com.group18.xantrex_calculator.controller;

import com.group18.xantrex_calculator.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class RegisterControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private RegisterController registerController;

    @Test
    void loginPage_returnsIndexTemplate() {
        String result = registerController.loginPage();
        assertEquals("index", result);
    }

    // Registration disabled — POST /register is commented out in RegisterController.
    // Tests for registerUser() are disabled alongside the endpoint.
    // They will be restored or replaced when intern management endpoints (Plan 01-03)
    // supersede the old public registration flow.
}
