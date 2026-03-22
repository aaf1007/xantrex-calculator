package com.group18.xantrex_calculator.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WeatherService {
    private final RestTemplate restTemplate = new RestTemplate();

    private double[] getCoordinates(String city) {
        try {
            String url = "https://geocoding-api.open-meteo.com/v1/search?name="
                    + city + "&count=1&language=en&format=json";

            Map response = restTemplate.getForObject(url, Map.class);

            if (response == null || !response.containsKey("results")) {
                throw new RuntimeException("City not found: " + city);
            }

            List results = (List) response.get("results");
            if (results.isEmpty()) {
                throw new RuntimeException("City not found: " + city);
            }

            Map location = (Map) results.get(0);

            double lat = ((Number) location.get("latitude")).doubleValue();
            double lon = ((Number) location.get("longitude")).doubleValue();

            return new double[]{lat, lon};

        } catch (Exception e) {
            throw new RuntimeException("Failed to geocode city: " + city + " - " + e.getMessage());
        }
    }

    public double getMinTemperature(String city, String country) {
        try {
            double[] coords = getCoordinates(city);
            double lat = coords[0];
            double lon = coords[1];

            String url = "https://api.open-meteo.com/v1/forecast?latitude=" + lat
                    + "&longitude=" + lon
                    + "&daily=temperature_2m_min"
                    + "&temperature_unit=celsius"
                    + "&forecast_days=7"
                    + "&timezone=auto";

            Map response = restTemplate.getForObject(url, Map.class);

            if (response == null || !response.containsKey("daily")) {
                throw new RuntimeException("Weather data not found for: " + city);
            }

            Map daily = (Map) response.get("daily");
            List tempMinList = (List) daily.get("temperature_2m_min");

            if (tempMinList == null || tempMinList.isEmpty()) {
                throw new RuntimeException("No temperature data available");
            }

            // Convert to double list
            List<Double> temps = (List<Double>) tempMinList.stream()
                    .map(t -> (t instanceof Number)
                            ? ((Number) t).doubleValue()
                            : Double.parseDouble(t.toString()))
                    .collect(Collectors.toList());

            double avg = temps.stream().mapToDouble(Double::doubleValue).average().orElse(20.0);
            double min = temps.stream().mapToDouble(Double::doubleValue).min().orElse(20.0);

            //HYBRID LOGIC
            double hybridTemp = Math.min(avg, min + 2);

            return hybridTemp;

        } catch (Exception e) {
            return 20.0;
        }
    }
}