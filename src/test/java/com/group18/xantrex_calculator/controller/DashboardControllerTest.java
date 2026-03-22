package com.group18.xantrex_calculator.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.group18.xantrex_calculator.entity.MpptController;
import com.group18.xantrex_calculator.entity.Role;
import com.group18.xantrex_calculator.entity.User;
import com.group18.xantrex_calculator.repository.MpptControllerRepository;
import com.group18.xantrex_calculator.repository.UserRepository;
import com.group18.xantrex_calculator.security.SecurityConfig;
import com.group18.xantrex_calculator.service.SolarPanelsService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DashboardController.class)
@Import(SecurityConfig.class)
public class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MpptControllerRepository controllerRepository;

    @MockitoBean
    private SolarPanelsService solarPanelsService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void testDashboard() throws Exception {
        // Arrange
        List<MpptController> controllers = Arrays.asList(
            new MpptController("Controller1", 100.0, 100.0, 100.0, "Type1", null, null),
            new MpptController("Controller2", 200.0, 120.0, 105.0, "Type2", null, null)
        );
        when(controllerRepository.findAll()).thenReturn(controllers);
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/dashboard").with(user("test@xantrex.com").roles("INTERN")))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attribute("controllers", hasSize(2)))
                .andExpect(model().attribute("controllers", contains(
                    hasProperty("name", is("Controller1")),
                    hasProperty("name", is("Controller2"))
                )));

        verify(controllerRepository, times(1)).findAll();
    }

    @Test
    void testAddController() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/dashboard/add")
                        .with(user("test@xantrex.com").roles("INTERN"))
                        .param("id", "1")
                        .param("name", "New Controller")
                        .param("type", "Type1")
                        .param("power", "150.0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));

        verify(controllerRepository, times(1)).save(any(MpptController.class));
    }

    @Test
    void testDeleteController() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/dashboard/delete")
                        .with(user("test@xantrex.com").roles("INTERN"))
                        .param("id", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));

        verify(controllerRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDashboard_setsIsInternTrue() throws Exception {
        when(controllerRepository.findAll()).thenReturn(Collections.emptyList());
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/dashboard").with(user("test@xantrex.com").roles("INTERN")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("isIntern", true));
    }

    @Test
    void testDashboard_setsCurrentUserEmail() throws Exception {
        when(controllerRepository.findAll()).thenReturn(Collections.emptyList());
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/dashboard").with(user("test@xantrex.com").roles("INTERN")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("currentUserEmail", "test@xantrex.com"));
    }

    @Test
    void testDashboard_filtersInternsByRole() throws Exception {
        User internUser = new User();
        internUser.setEmail("intern@xantrex.com");
        internUser.setRole(Role.INTERN);

        User clientUser = new User();
        clientUser.setEmail("client@xantrex.com");
        clientUser.setRole(Role.CLIENT);

        when(controllerRepository.findAll()).thenReturn(Collections.emptyList());
        when(userRepository.findAll()).thenReturn(Arrays.asList(internUser, clientUser));

        mockMvc.perform(get("/dashboard").with(user("test@xantrex.com").roles("INTERN")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("interns", hasSize(1)));
    }

    @Test
    void testAddController_clientRoleBlocked() throws Exception {
        mockMvc.perform(post("/dashboard/add")
                        .with(user("client@xantrex.com").roles("CLIENT"))
                        .param("name", "Unauthorized Controller")
                        .param("batteryBank", "12V"))
                .andExpect(status().isForbidden());

        verify(controllerRepository, never()).save(any(MpptController.class));
    }

    @Test
    void testDeleteController_clientRoleBlocked() throws Exception {
        mockMvc.perform(post("/dashboard/delete")
                        .with(user("client@xantrex.com").roles("CLIENT"))
                        .param("id", "1"))
                .andExpect(status().isForbidden());

        verify(controllerRepository, never()).deleteById(any());
    }
}
