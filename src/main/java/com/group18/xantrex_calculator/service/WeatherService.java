package com.group18.xantrex_calculator.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WeatherService {
    private final RestTemplate restTemplate = new RestTemplate();

    private double[] getCoordinates(String city, String country) {
        try {
            String url = "https://geocoding-api.open-meteo.com/v1/search?name="
                    + city + "&count=5&language=en&format=json";

            Map response = restTemplate.getForObject(url, Map.class);

            if (response == null || !response.containsKey("results")) {
                throw new CityNotFoundException(city, country);
            }

            List results = (List) response.get("results");
            if (results.isEmpty()) {
                throw new CityNotFoundException(city, country);
            }

            // Try to match the country code if provided
            if (country != null && !country.isBlank()) {
                for (Object item : results) {
                    Map location = (Map) item;
                    String resultCountry = (String) location.get("country_code");
                    if (country.equalsIgnoreCase(resultCountry)) {
                        double lat = ((Number) location.get("latitude")).doubleValue();
                        double lon = ((Number) location.get("longitude")).doubleValue();
                        return new double[]{lat, lon};
                    }
                }
                // No result matched the given country code
                throw new CityNotFoundException(city, country);
            }

            // No country filter — use the top result
            Map location = (Map) results.get(0);
            double lat = ((Number) location.get("latitude")).doubleValue();
            double lon = ((Number) location.get("longitude")).doubleValue();
            return new double[]{lat, lon};

        } catch (CityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Geocoding failed for: " + city + " - " + e.getMessage());
        }
    }

    public double getMinTemperature(String city, String country) {
        double[] coords = getCoordinates(city, country); // throws CityNotFoundException if invalid
        double lat = coords[0];
        double lon = coords[1];

        try {
            LocalDate endDate   = LocalDate.now().minusDays(2);
            LocalDate startDate = endDate.minusYears(1);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            String url = "https://archive-api.open-meteo.com/v1/archive"
                    + "?latitude="   + lat
                    + "&longitude="  + lon
                    + "&start_date=" + startDate.format(fmt)
                    + "&end_date="   + endDate.format(fmt)
                    + "&daily=temperature_2m_min"
                    + "&temperature_unit=celsius"
                    + "&timezone=auto";

            Map response = restTemplate.getForObject(url, Map.class);

            if (response == null || !response.containsKey("daily")) {
                throw new RuntimeException("Historical weather data not found for: " + city);
            }

            Map daily = (Map) response.get("daily");
            List tempMinList = (List) daily.get("temperature_2m_min");

            if (tempMinList == null || tempMinList.isEmpty()) {
                throw new RuntimeException("No historical temperature data available");
            }

            List<Double> temps = (List<Double>) tempMinList.stream()
                    .filter(t -> t != null)
                    .map(t -> (t instanceof Number)
                            ? ((Number) t).doubleValue()
                            : Double.parseDouble(t.toString()))
                    .collect(Collectors.toList());

            if (temps.isEmpty()) {
                return 20.0;
            }

            return temps.stream().mapToDouble(Double::doubleValue).min().orElse(20.0);

        } catch (Exception e) {
            System.err.println("WeatherService error for " + city + ": " + e.getMessage());
            return 20.0;
        }
    }

    // Custom exception
    public static class CityNotFoundException extends RuntimeException {
        private final String city;
        private final String country;

        public CityNotFoundException(String city, String country) {
            super("City not found: " + city + (country != null && !country.isBlank() ? ", " + country.toUpperCase() : ""));
            this.city    = city;
            this.country = country;
        }

        public String getCity()    { return city; }
        public String getCountry() { return country; }
    }
}