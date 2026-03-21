package com.group18.xantrex_calculator.controller;

import com.group18.xantrex_calculator.entity.MpptController;
import com.group18.xantrex_calculator.model.CalculatorResult;
import com.group18.xantrex_calculator.service.CalculatorService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;
import org.springframework.security.core.Authentication;

@Controller
public class CalculatorController {

    private final CalculatorService calculatorService;

    public CalculatorController(CalculatorService calculatorService) {
        this.calculatorService = calculatorService;
    }

    @GetMapping({ "/", "/calculator" })
    public String calculator(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isLoggedIn = auth != null && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken);
        model.addAttribute("isLoggedIn", isLoggedIn);
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
        Optional<MpptController> match = calculatorService.findMatchingController(result, String.valueOf(battV));
        model.addAttribute("result", result);
        model.addAttribute("recommendedController", match.orElse(null));
        return "result";
    }
}
