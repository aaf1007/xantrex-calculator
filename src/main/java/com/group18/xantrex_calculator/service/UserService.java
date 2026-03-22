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
        if (!email.endsWith("@xantrex.com")) {
            throw new InvalidDomainException("Only @xantrex.com email addresses are allowed.");
        }
        if (userRepository.findByEmail(email.toLowerCase()).isPresent()) {
            throw new UserAlreadyExistsException("Email already registered: " + email);
        }
        User user = new User();
        user.setEmail(email.toLowerCase());
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(Role.INTERN);
        userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        if (!email.endsWith("@xantrex.com")) {
            throw new UsernameNotFoundException("User not found: " + email);
        }
        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
    }
}
