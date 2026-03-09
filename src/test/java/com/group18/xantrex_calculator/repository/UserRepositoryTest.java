package com.group18.xantrex_calculator.repository;

import com.group18.xantrex_calculator.entity.Role;
import com.group18.xantrex_calculator.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User buildUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setPassword("plaintext-not-encoded");
        user.setRole(Role.CLIENT);
        return user;
    }

    @Test
    void saveUserPersistsWithGeneratedId() {
        User saved = userRepository.save(buildUser("id-test@example.com"));
        assertNotNull(saved.getId(), "Saved user should have a non-null generated id");
    }

    @Test
    void findByEmailReturnsUser() {
        userRepository.save(buildUser("repo@example.com"));
        Optional<User> result = userRepository.findByEmail("repo@example.com");
        assertTrue(result.isPresent(), "findByEmail should return present Optional for existing email");
        assertEquals("repo@example.com", result.get().getEmail());
    }

    @Test
    void findByEmailMissingReturnsEmpty() {
        Optional<User> result = userRepository.findByEmail("missing@example.com");
        assertTrue(result.isEmpty(), "findByEmail should return empty Optional for non-existent email");
    }
}
