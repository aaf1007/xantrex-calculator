package com.group18.xantrex_calculator.controller;

import com.group18.xantrex_calculator.exception.UserAlreadyExistsException;
import com.group18.xantrex_calculator.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class RegisterController {

    private final UserService userService;

    public RegisterController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "index";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String email,
                               @RequestParam String password, RedirectAttributes redirectAttributes) {
        try {
            userService.register(email, password);
            return "redirect:/login?registered";
        } catch (UserAlreadyExistsException e) {
            return "redirect:/login?registrationError";
        }
    }
}
