package com.group18.xantrex_calculator.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.group18.xantrex_calculator.service.WeatherService;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {
    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping("/min-temp")
    public double getMinTemp(@RequestParam String city,
                             @RequestParam String country) {
        try {
            return weatherService.getMinTemperature(city, country);
        } catch (Exception e) {
            System.err.println("Error fetching temperature for " + city + ", " + country + ": " + e.getMessage());
            e.printStackTrace();
            return 20.0;
        }
    }
}
