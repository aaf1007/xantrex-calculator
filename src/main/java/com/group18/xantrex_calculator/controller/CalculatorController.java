package com.group18.xantrex_calculator.controller;

import com.group18.xantrex_calculator.model.CalculatorResult;
import com.group18.xantrex_calculator.service.CalculatorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class CalculatorController {

    private final CalculatorService calculatorService;

    public CalculatorController(CalculatorService calculatorService) {
        this.calculatorService = calculatorService;
    }

    @GetMapping("/calculator")
    public String calculator() {
        return "userdashboard";
    }

    @PostMapping("/calculator")
    public String calculate(
            @RequestParam double pmax,
            @RequestParam double voc,
            @RequestParam double isc,
            @RequestParam int series,
            @RequestParam int parallel,
            @RequestParam int battV,
            @RequestParam double tempFactor,
            Model model) {

        CalculatorResult result = calculatorService.calculate(pmax, voc, isc, series, parallel, battV, tempFactor);
        model.addAttribute("result", result);
        return "result";
    }
}
