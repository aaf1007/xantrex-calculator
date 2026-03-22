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

@WebMvcTest(InternController.class)
@Import(SecurityConfig.class)
public class InternControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserRepository userRepository;

    // --- addIntern tests ---

    @Test
    void addIntern_success() throws Exception {
        doNothing().when(userService).register("new@xantrex.com", "secret");

        mockMvc.perform(post("/dashboard/interns/add")
                        .with(user("admin@xantrex.com").roles("INTERN"))
                        .param("email", "new@xantrex.com")
                        .param("password", "secret"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));

        verify(userService, times(1)).register("new@xantrex.com", "secret");
    }

    @Test
    void addIntern_emptyEmail_redirectsWithError() throws Exception {
        mockMvc.perform(post("/dashboard/interns/add")
                        .with(user("admin@xantrex.com").roles("INTERN"))
                        .param("email", "")
                        .param("password", "secret"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard?error=empty"));

        verify(userService, never()).register(anyString(), anyString());
    }

    @Test
    void addIntern_emptyPassword_redirectsWithError() throws Exception {
        mockMvc.perform(post("/dashboard/interns/add")
                        .with(user("admin@xantrex.com").roles("INTERN"))
                        .param("email", "intern@xantrex.com")
                        .param("password", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard?error=empty"));

        verify(userService, never()).register(anyString(), anyString());
    }

    @Test
    void addIntern_invalidDomain_redirectsWithError() throws Exception {
        doThrow(new InvalidDomainException("bad domain"))
                .when(userService).register("outsider@gmail.com", "pass");

        mockMvc.perform(post("/dashboard/interns/add")
                        .with(user("admin@xantrex.com").roles("INTERN"))
                        .param("email", "outsider@gmail.com")
                        .param("password", "pass"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard?error=invalid-domain"));
    }

    @Test
    void addIntern_duplicateEmail_redirectsWithError() throws Exception {
        doThrow(new UserAlreadyExistsException("already exists"))
                .when(userService).register("dup@xantrex.com", "pass");

        mockMvc.perform(post("/dashboard/interns/add")
                        .with(user("admin@xantrex.com").roles("INTERN"))
                        .param("email", "dup@xantrex.com")
                        .param("password", "pass"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard?error=duplicate"));
    }

    @Test
    void addIntern_unauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(post("/dashboard/interns/add")
                        .param("email", "intern@xantrex.com")
                        .param("password", "pass"))
                .andExpect(status().is3xxRedirection());

        verify(userService, never()).register(anyString(), anyString());
    }

    // --- deleteIntern tests ---

    @Test
    void deleteIntern_success() throws Exception {
        User target = new User();
        target.setEmail("other@xantrex.com");
        target.setRole(Role.INTERN);
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));

        mockMvc.perform(post("/dashboard/interns/delete")
                        .with(user("admin@xantrex.com").roles("INTERN"))
                        .param("id", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));

        verify(userRepository, times(1)).deleteById(2L);
    }

    @Test
    void deleteIntern_selfDelete_redirectsWithError() throws Exception {
        User self = new User();
        self.setEmail("admin@xantrex.com");
        self.setRole(Role.INTERN);
        when(userRepository.findById(1L)).thenReturn(Optional.of(self));

        mockMvc.perform(post("/dashboard/interns/delete")
                        .with(user("admin@xantrex.com").roles("INTERN"))
                        .param("id", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard?error=self-delete"));

        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteIntern_nonExistentId_redirectsToDashboard() throws Exception {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/dashboard/interns/delete")
                        .with(user("admin@xantrex.com").roles("INTERN"))
                        .param("id", "999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));

        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteIntern_unauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(post("/dashboard/interns/delete")
                        .param("id", "1"))
                .andExpect(status().is3xxRedirection());

        verify(userRepository, never()).deleteById(anyLong());
    }
}
