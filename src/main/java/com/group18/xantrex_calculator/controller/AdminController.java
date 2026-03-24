package com.group18.xantrex_calculator.controller;

import com.group18.xantrex_calculator.exception.InvalidDomainException;
import com.group18.xantrex_calculator.exception.UserAlreadyExistsException;
import com.group18.xantrex_calculator.repository.UserRepository;
import com.group18.xantrex_calculator.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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

    @PostMapping("/admins/change-password")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 Authentication authentication) {
        // VALID-02: blank check
        if (currentPassword == null || currentPassword.trim().isEmpty()
                || newPassword == null || newPassword.trim().isEmpty()
                || confirmPassword == null || confirmPassword.trim().isEmpty()) {
            return "redirect:/dashboard?error=password-empty";
        }
        // VALID-03: mismatch check
        if (!newPassword.equals(confirmPassword)) {
            return "redirect:/dashboard?error=password-mismatch";
        }
        String email = authentication.getName();
        try {
            userService.changePassword(email, currentPassword, newPassword);
        } catch (BadCredentialsException e) {
            // VALID-04: wrong current password
            return "redirect:/dashboard?error=wrong-password";
        }
        // ACCT-03: refresh SecurityContext so session stays valid
        UserDetails updatedDetails = userService.loadUserByUsername(email);
        UsernamePasswordAuthenticationToken newToken =
                new UsernamePasswordAuthenticationToken(
                        updatedDetails, null, updatedDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(newToken);
        // ACCT-04: success redirect
        return "redirect:/dashboard?success=password-changed";
    }
}
