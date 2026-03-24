package com.group18.xantrex_calculator.controller;

import com.group18.xantrex_calculator.entity.SolarPanels;
import com.group18.xantrex_calculator.service.SolarPanelsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/dashboard")
@PreAuthorize("hasRole('ADMIN')")
public class SolarPanelController {

    private final SolarPanelsService solarPanelsService;

    public SolarPanelController(SolarPanelsService solarPanelsService) {
        this.solarPanelsService = solarPanelsService;
    }

    // Add new panel
    @PostMapping("/panels/add")
    public String addController(@ModelAttribute SolarPanels panel) {
        solarPanelsService.saveSolarPanels(panel);
        return "redirect:/dashboard";
    }

    // Delete panel
    @PostMapping("/panels/delete")
    public String deletePanel(@RequestParam Long id, RedirectAttributes redirectAttributes) {
        solarPanelsService.deletePanel(id);
        return "redirect:/dashboard";
    }
}
