package com.group18.xantrex_calculator.controller;

import com.group18.xantrex_calculator.exception.InvalidDomainException;
import com.group18.xantrex_calculator.exception.UserAlreadyExistsException;
import com.group18.xantrex_calculator.repository.UserRepository;
import com.group18.xantrex_calculator.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/dashboard")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final UserRepository userRepository;

    public AdminController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @PostMapping("/admins/add")
    public String addAdmin(@RequestParam String email,
                           @RequestParam String password,
                           Authentication authentication) {
        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return "redirect:/dashboard?error=empty";
        }
        try {
            userService.register(email, password);
        } catch (InvalidDomainException e) {
            return "redirect:/dashboard?error=invalid-domain";
        } catch (UserAlreadyExistsException e) {
            return "redirect:/dashboard?error=duplicate";
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/admins/delete")
    public String deleteAdmin(@RequestParam Long id, Authentication authentication) {
        return userRepository.findById(id)
                .map(user -> {
                    if (user.getEmail().equalsIgnoreCase(authentication.getName())) {
                        return "redirect:/dashboard?error=self-delete";
                    }
                    userRepository.deleteById(id);
                    return "redirect:/dashboard";
                })
                .orElse("redirect:/dashboard");
    }
}
