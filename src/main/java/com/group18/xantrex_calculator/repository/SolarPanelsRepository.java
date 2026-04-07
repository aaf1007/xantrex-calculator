package com.group18.xantrex_calculator.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.group18.xantrex_calculator.entity.SolarPanels;

import java.util.Optional;

public interface SolarPanelsRepository extends JpaRepository<SolarPanels, Long> {
    Optional<SolarPanels> findByNameIgnoreCase(String name);
}
