package com.group18.xantrex_calculator.controller;

import com.group18.xantrex_calculator.model.CalculatorResult;
import com.group18.xantrex_calculator.entity.MpptController;
import com.group18.xantrex_calculator.service.CalculatorService;
import com.group18.xantrex_calculator.service.SolarPanelsService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CalculatorController.class)
public class CalculatorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CalculatorService calculatorService;

    @MockitoBean
    private SolarPanelsService solarPanelsService;

    @Test
    void testCalculatorPage() throws Exception {

        mockMvc.perform(get("/calculator"))
                .andExpect(status().isOk())
                .andExpect(view().name("userdashboard"));
    }

    /* Test POST /calculator calculation*/
    @Test
    void testCalculate() throws Exception {

        CalculatorResult result = new CalculatorResult(
                3120.0,
                142.8,
                212.2,
                20.0
        );

        MpptController controller = new MpptController();
        controller.setId(1L);
        controller.setBatteryBank("12V");
        controller.setMaxVoc(150.0);
        controller.setMaxCurrent(250.0);
        controller.setMaxIsc(30.0);

        when(calculatorService.calculate(
                260, 23.8, 10.0, 6, 2, 12, 1.0))
                .thenReturn(result);

        when(calculatorService.findMatchingController(result, "12"))
                .thenReturn(Optional.of(controller));

        mockMvc.perform(post("/calculator")
                        .param("pmax", "260")
                        .param("voc", "23.8")
                        .param("isc", "10.0")
                        .param("series", "6")
                        .param("parallel", "2")
                        .param("battV", "12")
                        .param("tempFactor", "1.0"))
                .andExpect(status().isOk())
                .andExpect(view().name("result"))
                .andExpect(model().attributeExists("result"))
                .andExpect(model().attributeExists("recommendedController"));

        verify(calculatorService, times(1))
                .calculate(260, 23.8, 10.0, 6, 2, 12, 1.0);

        verify(calculatorService, times(1))
                .findMatchingController(result, "12");
    }

    /*Test when no controller matches*/
}