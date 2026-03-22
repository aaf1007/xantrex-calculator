package com.group18.xantrex_calculator.controller;

import com.group18.xantrex_calculator.entity.MpptController;
import com.group18.xantrex_calculator.model.CalculatorResult;
import com.group18.xantrex_calculator.service.CalculatorService;
import com.group18.xantrex_calculator.service.SolarPanelsService;
import com.group18.xantrex_calculator.service.WeatherService;
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
    private final SolarPanelsService solarPanelsService;
    private final WeatherService weatherService;

    public CalculatorController(CalculatorService calculatorService, SolarPanelsService solarPanelsService, WeatherService weatherService) {
        this.calculatorService = calculatorService;
        this.solarPanelsService = solarPanelsService;
        this.weatherService = weatherService;
    }

    @GetMapping({ "/", "/calculator" })
    public String calculator(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isLoggedIn = auth != null && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken);
        model.addAttribute("isLoggedIn", isLoggedIn);
        model.addAttribute("panels", solarPanelsService.getAllPanels());
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
            @RequestParam String city,
            @RequestParam String country,
            Model model) {

        // Calculate temperature factor based on minimum temperature
        double minTemp = weatherService.getMinTemperature(city, country);
        double tempFactor = calculateTemperatureFactor(minTemp);

        model.addAttribute("panels", solarPanelsService.getAllPanels());
        CalculatorResult result = calculatorService.calculate(pmax, voc, isc, series, parallel, battV, tempFactor);
        Optional<MpptController> match = calculatorService.findMatchingController(result, String.valueOf(battV));
        model.addAttribute("result", result);
        model.addAttribute("recommendedController", match.orElse(null));
        model.addAttribute("minTemp", minTemp);
        model.addAttribute("tempFactor", tempFactor);
        return "result";
    }

    private double calculateTemperatureFactor(double minTemp) {
        if (minTemp < -10) return 1.3;
        else if (minTemp < 0) return 1.25;
        else return 1.2;
    }
}
