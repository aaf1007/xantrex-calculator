package com.group18.xantrex_calculator.service;

import com.group18.xantrex_calculator.entity.SolarPanels;
import com.group18.xantrex_calculator.repository.SolarPanelsRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SolarPanelsService {

    private final SolarPanelsRepository solarPanelsRepository;

    public SolarPanelsService(SolarPanelsRepository solarPanelsRepository) {
        this.solarPanelsRepository = solarPanelsRepository;
    }

    public List<SolarPanels> getAllPanels() {
        return solarPanelsRepository.findAll();
    }

    public SolarPanels getPanel(Long id) {
        return solarPanelsRepository.findById(id).orElse(null);
    }

    public void saveSolarPanels(SolarPanels panel) {
        solarPanelsRepository.save(panel);
    }

    public void deletePanel(Long id) {
        solarPanelsRepository.deleteById(id);
    }
}
