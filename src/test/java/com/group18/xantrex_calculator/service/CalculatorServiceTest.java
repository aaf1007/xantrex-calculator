package com.group18.xantrex_calculator.service;

import com.group18.xantrex_calculator.entity.MpptController;
import com.group18.xantrex_calculator.model.CalculatorResult;
import com.group18.xantrex_calculator.repository.MpptControllerRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalculatorServiceTest {

    @Mock
    private MpptControllerRepository controllerRepository;

    @InjectMocks
    private CalculatorService calculatorService;

    // --- totalPower ---

    @Test
    void totalPower_standardInput() {
        assertEquals(3120.0, calculatorService.totalPower(260, 6, 2), 0.001);
    }

    @Test
    void totalPower_singlePanel() {
        assertEquals(300.0, calculatorService.totalPower(300, 1, 1), 0.001);
    }

    @Test
    void totalPower_zeroSeries_returnsZero() {
        assertEquals(0.0, calculatorService.totalPower(300, 0, 2), 0.001);
    }

    // --- correctedVoc ---

    @Test
    void correctedVoc_withTempFactor() {
        // 23.8 * 6 * 1.2 = 171.36
        assertEquals(171.36, calculatorService.correctedVoc(23.8, 6, 1.2), 0.001);
    }

    @Test
    void correctedVoc_singlePanel_noCorrection() {
        assertEquals(23.8, calculatorService.correctedVoc(23.8, 1, 1.0), 0.001);
    }

    // --- maxChargeCurrent ---

    @Test
    void maxChargeCurrent_12V_uses14point7() {
        // 3000 / 14.7 ≈ 204.08
        assertEquals(3000.0 / 14.7, calculatorService.maxChargeCurrent(3000, 12), 0.001);
    }

    @Test
    void maxChargeCurrent_24V_uses29point4() {
        // 3000 / 29.4 ≈ 102.04
        assertEquals(3000.0 / 29.4, calculatorService.maxChargeCurrent(3000, 24), 0.001);
    }

    // --- shortCircuitCurrent ---

    @Test
    void shortCircuitCurrent_parallel() {
        assertEquals(20.0, calculatorService.shortCircuitCurrent(10.0, 2), 0.001);
    }

    @Test
    void shortCircuitCurrent_singleString() {
        assertEquals(9.5, calculatorService.shortCircuitCurrent(9.5, 1), 0.001);
    }

    // --- calculate (integration) ---

    @Test
    void calculate_returnsCorrectResult() {
        CalculatorResult result = calculatorService.calculate(260, 23.8, 10.0, 6, 2, 12, 1.2);

        assertEquals(3120.0, result.getTotalPower(), 0.001);
        assertEquals(23.8 * 6 * 1.2, result.getCorrectedVoc(), 0.001);
        assertEquals(3120.0 / 14.7, result.getMaxChargeCurrent(), 0.001);
        assertEquals(20.0, result.getShortCircuitCurrent(), 0.001);
    }

    // --- findMatchingController ---

    @Test
    void findMatchingController_returnsBestFit() {
        MpptController small = new MpptController("Small", 200.0, 250.0, 25.0, "12V", null, null);
        MpptController large = new MpptController("Large", 300.0, 250.0, 25.0, "12V", null, null);
        when(controllerRepository.findAll()).thenReturn(Arrays.asList(large, small));

        // Result that both controllers can handle
        CalculatorResult result = new CalculatorResult(3000, 180.0, 200.0, 20.0);
        Optional<MpptController> match = calculatorService.findMatchingController(result, "12");

        assertTrue(match.isPresent());
        assertEquals("Small", match.get().getName(), "Should pick controller with lowest maxVoc that still qualifies");
    }

    @Test
    void findMatchingController_noMatch_returnsEmpty() {
        MpptController controller = new MpptController("Too Small", 100.0, 50.0, 10.0, "12V", null, null);
        when(controllerRepository.findAll()).thenReturn(Collections.singletonList(controller));

        // Result exceeds controller capacity
        CalculatorResult result = new CalculatorResult(5000, 200.0, 300.0, 30.0);
        Optional<MpptController> match = calculatorService.findMatchingController(result, "12");

        assertFalse(match.isPresent());
    }

    @Test
    void findMatchingController_batteryBankMismatch_returnsEmpty() {
        MpptController controller24V = new MpptController("24V Ctrl", 300.0, 250.0, 25.0, "24V", null, null);
        when(controllerRepository.findAll()).thenReturn(Collections.singletonList(controller24V));

        // Looking for 12V match
        CalculatorResult result = new CalculatorResult(3000, 180.0, 200.0, 20.0);
        Optional<MpptController> match = calculatorService.findMatchingController(result, "12");

        assertFalse(match.isPresent(), "12V result should not match a 24V-only controller");
    }

    @Test
    void findMatchingController_nullFields_skipsNullControllers() {
        // Controller with null fields — should not throw NPE, just be skipped
        MpptController nullBattery = new MpptController("Null Battery", 300.0, 250.0, 25.0, null, null, null);
        MpptController nullVoc = new MpptController("Null Voc", null, 250.0, 25.0, "12V", null, null);
        MpptController valid = new MpptController("Valid", 300.0, 250.0, 25.0, "12V", null, null);
        when(controllerRepository.findAll()).thenReturn(Arrays.asList(nullBattery, nullVoc, valid));

        CalculatorResult result = new CalculatorResult(3000, 180.0, 200.0, 20.0);

        assertDoesNotThrow(() -> {
            Optional<MpptController> match = calculatorService.findMatchingController(result, "12");
            assertTrue(match.isPresent());
            assertEquals("Valid", match.get().getName());
        });
    }

    @Test
    void findMatchingController_emptyDatabase_returnsEmpty() {
        when(controllerRepository.findAll()).thenReturn(Collections.emptyList());

        CalculatorResult result = new CalculatorResult(3000, 180.0, 200.0, 20.0);
        Optional<MpptController> match = calculatorService.findMatchingController(result, "12");

        assertFalse(match.isPresent());
    }

    @Test
    void totalPower_seriesOnly_3panels() {
        assertEquals(780.0, calculatorService.totalPower(260, 3, 1), 0.001);
    }

    @Test
    void totalPower_parallelOnly_3panels() {
        assertEquals(780.0, calculatorService.totalPower(260, 1, 3), 0.001);
    }

    @Test
    void totalPower_seriesAndParallel_3x2() {
        assertEquals(1560.0, calculatorService.totalPower(260, 3, 2), 0.001);
    }

    @Test
    void totalPower_singlePanel_1x1() {
        assertEquals(260.0, calculatorService.totalPower(260, 1, 1), 0.001);
    }
}
