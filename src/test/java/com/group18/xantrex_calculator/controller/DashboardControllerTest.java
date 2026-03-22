package com.group18.xantrex_calculator.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.group18.xantrex_calculator.entity.MpptController;
import com.group18.xantrex_calculator.repository.MpptControllerRepository;
import com.group18.xantrex_calculator.service.SolarPanelsService;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DashboardController.class)
public class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MpptControllerRepository controllerRepository;

    @MockitoBean
    private SolarPanelsService solarPanelsService;

    @Test
    void testDashboard() throws Exception {
        // Arrange
        List<MpptController> controllers = Arrays.asList(
            new MpptController("Controller1", 100.0, 100.0,100.0, "Type1", null, null),
            new MpptController("Controller2", 200.0, 120.0,105.0, "Type2", null, null)
        );
        when(controllerRepository.findAll()).thenReturn(controllers);

        // Act & Assert
        mockMvc.perform(get("/dashboard"))
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
                        .param("id", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));

        verify(controllerRepository, times(1)).deleteById(1L);
    }
}
