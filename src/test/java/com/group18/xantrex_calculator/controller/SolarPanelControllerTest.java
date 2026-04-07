package com.group18.xantrex_calculator.controller;

import com.group18.xantrex_calculator.entity.SolarPanels;
import com.group18.xantrex_calculator.security.SecurityConfig;
import com.group18.xantrex_calculator.service.SolarPanelsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SolarPanelController.class)
@Import(SecurityConfig.class)
class SolarPanelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SolarPanelsService solarPanelsService;

    @Test
    void addPanel_emptyFields_redirectsWithError() throws Exception {
        mockMvc.perform(post("/dashboard/panels/add")
                        .with(user("admin@xantrex.com").roles("ADMIN"))
                        .param("name", "")
                        .param("pmax", "350.0")
                        .param("voc", "48.5")
                        .param("isc", "9.2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard?error=empty-panel"));

        verify(solarPanelsService, never()).findByNameIgnoreCase(any());
        verify(solarPanelsService, never()).saveSolarPanels(any(SolarPanels.class));
    }

    @Test
    void addPanel_duplicateName_redirectsWithError() throws Exception {
        when(solarPanelsService.findByNameIgnoreCase("Existing Panel"))
                .thenReturn(Optional.of(new SolarPanels()));

        mockMvc.perform(post("/dashboard/panels/add")
                        .with(user("admin@xantrex.com").roles("ADMIN"))
                        .param("name", " Existing Panel ")
                        .param("pmax", "350.0")
                        .param("voc", "48.5")
                        .param("isc", "9.2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard?error=duplicate-panel"));

        verify(solarPanelsService, times(1)).findByNameIgnoreCase("Existing Panel");
        verify(solarPanelsService, never()).saveSolarPanels(any(SolarPanels.class));
    }

    @Test
    void addPanel_success_redirectsWithSuccess() throws Exception {
        when(solarPanelsService.findByNameIgnoreCase("Test Panel")).thenReturn(Optional.empty());

        mockMvc.perform(post("/dashboard/panels/add")
                        .with(user("admin@xantrex.com").roles("ADMIN"))
                        .param("name", " Test Panel ")
                        .param("pmax", "350.0")
                        .param("voc", "48.5")
                        .param("isc", "9.2")
                        .param("imageUrl", " /images/test-panel.png "))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard?success=panel-added"));

        verify(solarPanelsService, times(1)).findByNameIgnoreCase("Test Panel");
        verify(solarPanelsService, times(1)).saveSolarPanels(any(SolarPanels.class));
    }

    @Test
    void deletePanel_success_redirectsWithSuccess() throws Exception {
        mockMvc.perform(post("/dashboard/panels/delete")
                        .with(user("admin@xantrex.com").roles("ADMIN"))
                        .param("id", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard?success=panel-deleted"));

        verify(solarPanelsService, times(1)).deletePanel(1L);
    }

    @Test
    void addPanel_userRoleBlocked() throws Exception {
        mockMvc.perform(post("/dashboard/panels/add")
                        .with(user("user@xantrex.com").roles("USER"))
                        .param("name", "Test Panel")
                        .param("pmax", "350.0")
                        .param("voc", "48.5")
                        .param("isc", "9.2"))
                .andExpect(status().isForbidden());

        verify(solarPanelsService, never()).saveSolarPanels(any(SolarPanels.class));
    }

    @Test
    void deletePanel_unauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(post("/dashboard/panels/delete")
                        .param("id", "1"))
                .andExpect(status().is3xxRedirection());

        verify(solarPanelsService, never()).deletePanel(any());
    }
}
