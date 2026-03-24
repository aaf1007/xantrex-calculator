package com.group18.xantrex_calculator.controller;

import com.group18.xantrex_calculator.entity.Role;
import com.group18.xantrex_calculator.entity.User;
import com.group18.xantrex_calculator.exception.InvalidDomainException;
import com.group18.xantrex_calculator.exception.UserAlreadyExistsException;
import com.group18.xantrex_calculator.repository.UserRepository;
import com.group18.xantrex_calculator.security.SecurityConfig;
import com.group18.xantrex_calculator.service.UserService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@Import(SecurityConfig.class)
public class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserRepository userRepository;

    // --- addAdmin tests ---

    @Test
    void addAdmin_success() throws Exception {
        doNothing().when(userService).register("new@xantrex.com", "secret");

        mockMvc.perform(post("/dashboard/admins/add")
                        .with(user("admin@xantrex.com").roles("ADMIN"))
                        .param("email", "new@xantrex.com")
                        .param("password", "secret"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));

        verify(userService, times(1)).register("new@xantrex.com", "secret");
    }

    @Test
    void addAdmin_emptyEmail_redirectsWithError() throws Exception {
        mockMvc.perform(post("/dashboard/admins/add")
                        .with(user("admin@xantrex.com").roles("ADMIN"))
                        .param("email", "")
                        .param("password", "secret"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard?error=empty"));

        verify(userService, never()).register(anyString(), anyString());
    }

    @Test
    void addAdmin_emptyPassword_redirectsWithError() throws Exception {
        mockMvc.perform(post("/dashboard/admins/add")
                        .with(user("admin@xantrex.com").roles("ADMIN"))
                        .param("email", "admin@xantrex.com")
                        .param("password", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard?error=empty"));

        verify(userService, never()).register(anyString(), anyString());
    }

    @Test
    void addAdmin_invalidDomain_redirectsWithError() throws Exception {
        doThrow(new InvalidDomainException("bad domain"))
                .when(userService).register("outsider@gmail.com", "pass");

        mockMvc.perform(post("/dashboard/admins/add")
                        .with(user("admin@xantrex.com").roles("ADMIN"))
                        .param("email", "outsider@gmail.com")
                        .param("password", "pass"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard?error=invalid-domain"));
    }

    @Test
    void addAdmin_duplicateEmail_redirectsWithError() throws Exception {
        doThrow(new UserAlreadyExistsException("already exists"))
                .when(userService).register("dup@xantrex.com", "pass");

        mockMvc.perform(post("/dashboard/admins/add")
                        .with(user("admin@xantrex.com").roles("ADMIN"))
                        .param("email", "dup@xantrex.com")
                        .param("password", "pass"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard?error=duplicate"));
    }

    @Test
    void addAdmin_unauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(post("/dashboard/admins/add")
                        .param("email", "admin@xantrex.com")
                        .param("password", "pass"))
                .andExpect(status().is3xxRedirection());

        verify(userService, never()).register(anyString(), anyString());
    }

    // --- deleteAdmin tests ---

    @Test
    void deleteAdmin_success() throws Exception {
        User target = new User();
        target.setEmail("other@xantrex.com");
        target.setRole(Role.ADMIN);
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));

        mockMvc.perform(post("/dashboard/admins/delete")
                        .with(user("admin@xantrex.com").roles("ADMIN"))
                        .param("id", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));

        verify(userRepository, times(1)).deleteById(2L);
    }

    @Test
    void deleteAdmin_selfDelete_redirectsWithError() throws Exception {
        User self = new User();
        self.setEmail("admin@xantrex.com");
        self.setRole(Role.ADMIN);
        when(userRepository.findById(1L)).thenReturn(Optional.of(self));

        mockMvc.perform(post("/dashboard/admins/delete")
                        .with(user("admin@xantrex.com").roles("ADMIN"))
                        .param("id", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard?error=self-delete"));

        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteAdmin_nonExistentId_redirectsToDashboard() throws Exception {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/dashboard/admins/delete")
                        .with(user("admin@xantrex.com").roles("ADMIN"))
                        .param("id", "999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));

        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteAdmin_unauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(post("/dashboard/admins/delete")
                        .param("id", "1"))
                .andExpect(status().is3xxRedirection());

        verify(userRepository, never()).deleteById(anyLong());
    }

    // --- changePassword tests ---

    @Test
    void changePassword_success_redirectsWithSuccessParam() throws Exception {
        doNothing().when(userService).changePassword(anyString(), anyString(), anyString());

        UserDetails mockDetails = org.springframework.security.core.userdetails.User.builder()
                .username("admin@xantrex.com")
                .password("encoded")
                .roles("ADMIN")
                .build();
        when(userService.loadUserByUsername("admin@xantrex.com")).thenReturn(mockDetails);

        mockMvc.perform(post("/dashboard/admins/change-password")
                        .with(user("admin@xantrex.com").roles("ADMIN"))
                        .param("currentPassword", "oldPass")
                        .param("newPassword", "newPass")
                        .param("confirmPassword", "newPass"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard?success=password-changed"));

        verify(userService, times(1)).changePassword("admin@xantrex.com", "oldPass", "newPass");
    }

    @Test
    void changePassword_emptyField_redirectsWithPasswordEmptyError() throws Exception {
        mockMvc.perform(post("/dashboard/admins/change-password")
                        .with(user("admin@xantrex.com").roles("ADMIN"))
                        .param("currentPassword", "")
                        .param("newPassword", "newPass")
                        .param("confirmPassword", "newPass"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard?error=password-empty"));

        verify(userService, never()).changePassword(anyString(), anyString(), anyString());
    }

    @Test
    void changePassword_mismatch_redirectsWithMismatchError() throws Exception {
        mockMvc.perform(post("/dashboard/admins/change-password")
                        .with(user("admin@xantrex.com").roles("ADMIN"))
                        .param("currentPassword", "oldPass")
                        .param("newPassword", "newPass")
                        .param("confirmPassword", "differentPass"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard?error=password-mismatch"));

        verify(userService, never()).changePassword(anyString(), anyString(), anyString());
    }

    @Test
    void changePassword_wrongCurrentPassword_redirectsWithWrongPasswordError() throws Exception {
        doThrow(new BadCredentialsException("wrong"))
                .when(userService).changePassword("admin@xantrex.com", "badPass", "newPass");

        mockMvc.perform(post("/dashboard/admins/change-password")
                        .with(user("admin@xantrex.com").roles("ADMIN"))
                        .param("currentPassword", "badPass")
                        .param("newPassword", "newPass")
                        .param("confirmPassword", "newPass"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard?error=wrong-password"));
    }

    @Test
    void changePassword_unauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(post("/dashboard/admins/change-password")
                        .param("currentPassword", "oldPass")
                        .param("newPassword", "newPass")
                        .param("confirmPassword", "newPass"))
                .andExpect(status().is3xxRedirection());

        verify(userService, never()).changePassword(anyString(), anyString(), anyString());
    }
}
