package com.group18.xantrex_calculator.config;

import com.group18.xantrex_calculator.entity.MpptController;
import com.group18.xantrex_calculator.entity.SolarPanels;
import com.group18.xantrex_calculator.repository.MpptControllerRepository;
import com.group18.xantrex_calculator.repository.SolarPanelsRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DataSeeder {

    @Bean
    ApplicationRunner seedControllers(MpptControllerRepository repo) {
        return args -> {
            if (repo.count() > 0) return;
            repo.saveAll(List.of(
                new MpptController(
                    "MPPT Charge Controller 30A",
                    100.0, 30.0, 30.0,
                    "12/24V",
                    "https://xantrex.com/wp-content/uploads/2024/08/mpptchargecontroller_656x476.png",
                    "https://xantrex.com/products/solar-panels/xantrex-mppt-charge-controller-30a/"
                ),
                new MpptController(
                    "MPPT Charge Controller 60A",
                    150.0, 60.0, 60.0,
                    "12/24/36/48V",
                    "https://xantrex.com/wp-content/uploads/2024/01/MPPT_60_150-8-no-shadow.png",
                    "https://xantrex.com/products/solar-panels/xantrex-mppt-charge-controller-60a/"
                )
            ));
        };
    }

    @Bean
    ApplicationRunner seedPanels(SolarPanelsRepository repo) {
        return args -> {
            if (repo.count() > 0) return;
            repo.saveAll(List.of(
                new SolarPanels(
                    "Xantrex Solar Max Panel 115W",
                    115.0, 27.4, 5.6,
                    "https://xantrex.com/wp-content/uploads/2024/01/Solar6-1024x597.jpg"
                ),
                new SolarPanels(
                    "Xantrex Solar Max Panel Slim 115W",
                    115.0, 27.4, 5.6,
                    "https://xantrex.com/wp-content/uploads/2024/01/Solar8-1024x597.jpg"
                ),
                new SolarPanels(
                    "Xantrex Solar Max Panel 220W",
                    220.0, 25.8, 11.2,
                    "https://xantrex.com/wp-content/uploads/2021/11/solarmax_220W.jpg"
                ),
                new SolarPanels(
                    "Xantrex Solar Max Panel 330W",
                    330.0, 43.8, 9.7,
                    "https://xantrex.com/wp-content/uploads/2021/11/solarmax_330W.jpg"
                ),
                new SolarPanels(
                    "Xantrex Solar Flex Panel 110W",
                    110.0, 23.3, 5.95,
                    "https://xantrex.com/wp-content/uploads/2021/11/solarflex_110W.jpg"
                ),
                new SolarPanels(
                    "Custom",
                    0.0, 0.0, 0.0,
                    "https://xantrex.com/wp-content/uploads/2021/11/flexsolarpanels_floating_1048_964.png"
                )
            ));
        };
    }
}
