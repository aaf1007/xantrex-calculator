package com.group18.xantrex_calculator.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WeatherService {
    private final RestTemplate restTemplate = new RestTemplate();

    // Step 1: Geocoding API — convert city name to lat/lon
    private double[] getCoordinates(String city) {
        try {
            String url = "https://geocoding-api.open-meteo.com/v1/search?name="
                    + city + "&count=1&language=en&format=json";

            Map response = restTemplate.getForObject(url, Map.class);

            if (response == null || !response.containsKey("results")) {
                throw new RuntimeException("City not found: " + city + " - no results in response");
            }

            List results = (List) response.get("results");
            if (results == null || results.isEmpty()) {
                throw new RuntimeException("City not found: " + city + " - empty results list");
            }

            Map location = (Map) results.get(0);
            
            Object latObj = location.get("latitude");
            Object lonObj = location.get("longitude");
            
            if (latObj == null || lonObj == null) {
                throw new RuntimeException("City coordinates not found for: " + city);
            }
            
            double lat, lon;
            if (latObj instanceof Number) {
                lat = ((Number) latObj).doubleValue();
            } else {
                lat = Double.parseDouble(latObj.toString());
            }
            
            if (lonObj instanceof Number) {
                lon = ((Number) lonObj).doubleValue();
            } else {
                lon = Double.parseDouble(lonObj.toString());
            }
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

            // daily=temperature_2m_min gives the forecasted minimum temp for today
            String url = "https://api.open-meteo.com/v1/forecast?latitude=" + lat
                    + "&longitude=" + lon
                    + "&daily=temperature_2m_min&temperature_unit=celsius&forecast_days=1&timezone=auto";

            Map response = restTemplate.getForObject(url, Map.class);

            if (response == null || !response.containsKey("daily")) {
                throw new RuntimeException("Could not fetch weather data for: " + city + " - no daily data in response");
            }

            Map daily = (Map) response.get("daily");
            List tempMinList = (List) daily.get("temperature_2m_min");

            if (tempMinList == null || tempMinList.isEmpty()) {
                throw new RuntimeException("temperature_2m_min was empty for: " + city + " - no temperature data available");
            }
 
            Object rawTemp = tempMinList.get(0);
 
            if (rawTemp == null) {
                throw new RuntimeException(
                    "temperature_2m_min[0] was null — Open-Meteo may not have data for this location yet");
            }

            double minTemp;
            if (rawTemp instanceof Number) {
                minTemp = ((Number) rawTemp).doubleValue();
            } else if (rawTemp instanceof String) {
                minTemp = Double.parseDouble((String) rawTemp);
            } else {
                minTemp = Double.parseDouble(rawTemp.toString());
            }

            return minTemp;
        } catch (Exception e) {
            return 20.0;
        }
    }
}