package com.group18.xantrex_calculator.controller;

import com.group18.xantrex_calculator.entity.SolarPanels;
import com.group18.xantrex_calculator.service.SolarPanelsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SolarPanelController.class)
class SolarPanelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SolarPanelsService solarPanelsService;

    /**
     * Test adding a new solar panel with valid data and ADMIN role
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void testAddController_withValidPanel() throws Exception {
        SolarPanels panel = new SolarPanels("Test Panel", 350.0, 48.5, 9.2, "/images/test-panel.png");

        mockMvc.perform(post("/dashboard/panels/add")
                .with(csrf())
                .param("name", "Test Panel")
                .param("pmax", "350.0")
                .param("voc", "48.5")
                .param("isc", "9.2")
                .param("imageUrl", "/images/test-panel.png"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));

        verify(solarPanelsService, times(1)).saveSolarPanels(any(SolarPanels.class));
    }

    /**
     * Test adding a solar panel without ADMIN role (should redirect)
     */
    @Test
    @WithMockUser(roles = "USER")
    void testAddController_withoutAdminRole() throws Exception {
        mockMvc.perform(post("/dashboard/panels/add")
                .with(csrf())
                .param("name", "Test Panel")
                .param("pmax", "350.0")
                .param("voc", "48.5")
                .param("isc", "9.2")
                .param("imageUrl", "/images/test-panel.png"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }

    /**
     * Test adding a solar panel without authentication (should redirect to dashboard)
     */
    @Test
    void testAddController_withoutAuthentication() throws Exception {
        mockMvc.perform(post("/dashboard/panels/add")
                .with(csrf())
                .param("name", "Test Panel")
                .param("pmax", "350.0")
                .param("voc", "48.5")
                .param("isc", "9.2")
                .param("imageUrl", "/images/test-panel.png"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }

    /**
     * Test adding a solar panel with missing CSRF token (should redirect)
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void testAddController_withoutCsrfToken() throws Exception {
        mockMvc.perform(post("/dashboard/panels/add")
                .param("name", "Test Panel")
                .param("pmax", "350.0")
                .param("voc", "48.5")
                .param("isc", "9.2")
                .param("imageUrl", "/images/test-panel.png"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }

    /**
     * Test adding a solar panel with minimal data
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void testAddController_withMinimalData() throws Exception {
        mockMvc.perform(post("/dashboard/panels/add")
                .with(csrf())
                .param("name", "Simple Panel")
                .param("pmax", "100.0")
                .param("voc", "30.0")
                .param("isc", "5.0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));

        verify(solarPanelsService, times(1)).saveSolarPanels(any(SolarPanels.class));
    }

    /**
     * Test deleting a solar panel with valid ID and ADMIN role
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeletePanel_withValidId() throws Exception {
        Long panelId = 1L;

        mockMvc.perform(post("/dashboard/panels/delete")
                .with(csrf())
                .param("id", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));

        verify(solarPanelsService, times(1)).deletePanel(panelId);
    }

    /**
     * Test deleting a solar panel without ADMIN role (should redirect)
     */
    @Test
    @WithMockUser(roles = "USER")
    void testDeletePanel_withoutAdminRole() throws Exception {
        mockMvc.perform(post("/dashboard/panels/delete")
                .with(csrf())
                .param("id", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }

    /**
     * Test deleting a solar panel without authentication (should redirect to dashboard)
     */
    @Test
    void testDeletePanel_withoutAuthentication() throws Exception {
        mockMvc.perform(post("/dashboard/panels/delete")
                .with(csrf())
                .param("id", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }

    /**
     * Test deleting a solar panel without CSRF token (should redirect)
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeletePanel_withoutCsrfToken() throws Exception {
        mockMvc.perform(post("/dashboard/panels/delete")
                .param("id", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }

    /**
     * Test deleting a solar panel with non-existent ID
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeletePanel_withNonExistentId() throws Exception {
        doNothing().when(solarPanelsService).deletePanel(999L);

        mockMvc.perform(post("/dashboard/panels/delete")
                .with(csrf())
                .param("id", "999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));

        verify(solarPanelsService, times(1)).deletePanel(999L);
    }

    /**
     * Test deleting a solar panel with multiple sequential deletions
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeletePanel_multipleSequentialDeletions() throws Exception {
        // First deletion
        mockMvc.perform(post("/dashboard/panels/delete")
                .with(csrf())
                .param("id", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));

        // Second deletion
        mockMvc.perform(post("/dashboard/panels/delete")
                .with(csrf())
                .param("id", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));

        verify(solarPanelsService, times(2)).deletePanel(any());
    }
}
