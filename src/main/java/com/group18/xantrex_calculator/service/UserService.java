package com.group18.xantrex_calculator.service;

import com.group18.xantrex_calculator.entity.Role;
import com.group18.xantrex_calculator.entity.User;
import com.group18.xantrex_calculator.exception.InvalidDomainException;
import com.group18.xantrex_calculator.exception.UserAlreadyExistsException;
import com.group18.xantrex_calculator.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, @Lazy BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void register(String email, String password) {
        String normalizedEmail = email.toLowerCase();
        if (!normalizedEmail.endsWith("@xantrex.com")) {
            throw new InvalidDomainException("Only @xantrex.com email addresses are allowed.");
        }
        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new UserAlreadyExistsException("Email already registered: " + normalizedEmail);
        }
        User user = new User();
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(Role.ADMIN);
        userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        String normalizedEmail = email.toLowerCase();
        if (!normalizedEmail.endsWith("@xantrex.com")) {
            throw new UsernameNotFoundException("User not found: " + normalizedEmail);
        }
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + normalizedEmail));
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
    }
}
