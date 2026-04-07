package com.group18.xantrex_calculator.controller;

import com.group18.xantrex_calculator.entity.MpptController;
import com.group18.xantrex_calculator.entity.Role;
import com.group18.xantrex_calculator.entity.User;
import com.group18.xantrex_calculator.repository.MpptControllerRepository;
import com.group18.xantrex_calculator.repository.UserRepository;
import com.group18.xantrex_calculator.security.SecurityConfig;
import com.group18.xantrex_calculator.service.SolarPanelsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(DashboardController.class)
@Import(SecurityConfig.class)
class DashboardControllerTest {

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
        List<MpptController> controllers = Arrays.asList(
                new MpptController("Controller1", 100.0, 100.0, 100.0, "12V", null, null),
                new MpptController("Controller2", 200.0, 120.0, 105.0, "24V", null, null)
        );
        when(controllerRepository.findAll()).thenReturn(controllers);
        when(solarPanelsService.getAllPanels()).thenReturn(Collections.emptyList());
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/dashboard").with(user("test@xantrex.com").roles("ADMIN")))
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
    void addController_emptyFields_redirectsWithError() throws Exception {
        mockMvc.perform(post("/dashboard/add")
                        .with(user("test@xantrex.com").roles("ADMIN"))
                        .param("name", "")
                        .param("maxVoc", "150.0")
                        .param("maxCurrent", "80.0")
                        .param("maxIsc", "90.0")
                        .param("batteryBank", "48V"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard?error=empty-controller"));

        verify(controllerRepository, never()).findByNameIgnoreCase(any());
        verify(controllerRepository, never()).save(any(MpptController.class));
    }

    @Test
    void addController_duplicateName_redirectsWithError() throws Exception {
        when(controllerRepository.findByNameIgnoreCase("Existing Controller"))
                .thenReturn(Optional.of(new MpptController()));

        mockMvc.perform(post("/dashboard/add")
                        .with(user("test@xantrex.com").roles("ADMIN"))
                        .param("name", " Existing Controller ")
                        .param("maxVoc", "150.0")
                        .param("maxCurrent", "80.0")
                        .param("maxIsc", "90.0")
                        .param("batteryBank", "48V"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard?error=duplicate-controller"));

        verify(controllerRepository, times(1)).findByNameIgnoreCase("Existing Controller");
        verify(controllerRepository, never()).save(any(MpptController.class));
    }

    @Test
    void addController_success_redirectsWithSuccess() throws Exception {
        when(controllerRepository.findByNameIgnoreCase("New Controller")).thenReturn(Optional.empty());

        mockMvc.perform(post("/dashboard/add")
                        .with(user("test@xantrex.com").roles("ADMIN"))
                        .param("name", " New Controller ")
                        .param("maxVoc", "150.0")
                        .param("maxCurrent", "80.0")
                        .param("maxIsc", "90.0")
                        .param("batteryBank", "48V")
                        .param("imageUrl", " /images/controller.png ")
                        .param("productUrl", " https://example.com/controller "))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard?success=controller-added"));

        verify(controllerRepository, times(1)).findByNameIgnoreCase("New Controller");
        verify(controllerRepository, times(1)).save(any(MpptController.class));
    }

    @Test
    void deleteController_success_redirectsWithSuccess() throws Exception {
        mockMvc.perform(post("/dashboard/delete")
                        .with(user("test@xantrex.com").roles("ADMIN"))
                        .param("id", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard?success=controller-deleted"));

        verify(controllerRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDashboard_setsIsAdminTrue() throws Exception {
        when(controllerRepository.findAll()).thenReturn(Collections.emptyList());
        when(solarPanelsService.getAllPanels()).thenReturn(Collections.emptyList());
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/dashboard").with(user("test@xantrex.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("isAdmin", true));
    }

    @Test
    void testDashboard_setsCurrentUserEmail() throws Exception {
        when(controllerRepository.findAll()).thenReturn(Collections.emptyList());
        when(solarPanelsService.getAllPanels()).thenReturn(Collections.emptyList());
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/dashboard").with(user("test@xantrex.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("currentUserEmail", "test@xantrex.com"));
    }

    @Test
    void testDashboard_filtersAdminsByRole() throws Exception {
        User adminUser = new User();
        adminUser.setEmail("admin@xantrex.com");
        adminUser.setRole(Role.ADMIN);

        User clientUser = new User();
        clientUser.setEmail("client@xantrex.com");
        clientUser.setRole(Role.CLIENT);

        when(controllerRepository.findAll()).thenReturn(Collections.emptyList());
        when(solarPanelsService.getAllPanels()).thenReturn(Collections.emptyList());
        when(userRepository.findAll()).thenReturn(Arrays.asList(adminUser, clientUser));

        mockMvc.perform(get("/dashboard").with(user("test@xantrex.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("admins", hasSize(1)));
    }

    @Test
    void testAddController_clientRoleBlocked() throws Exception {
        mockMvc.perform(post("/dashboard/add")
                        .with(user("client@xantrex.com").roles("CLIENT"))
                        .param("name", "Unauthorized Controller")
                        .param("maxVoc", "150.0")
                        .param("maxCurrent", "80.0")
                        .param("maxIsc", "90.0")
                        .param("batteryBank", "48V"))
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
