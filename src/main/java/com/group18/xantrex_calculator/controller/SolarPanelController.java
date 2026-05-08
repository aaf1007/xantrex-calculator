package com.group18.xantrex_calculator.controller;

import com.group18.xantrex_calculator.entity.SolarPanels;
import com.group18.xantrex_calculator.service.SolarPanelsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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
    public String addController(@RequestParam(required = false) String name,
                                @RequestParam(required = false) String pmax,
                                @RequestParam(required = false) String voc,
                                @RequestParam(required = false) String isc,
                                @RequestParam(required = false) String imageUrl) {
        if (isBlank(name) || isBlank(pmax) || isBlank(voc) || isBlank(isc)) {
            return "redirect:/dashboard?error=empty-panel";
        }

        String trimmedName = name.trim();
        if (solarPanelsService.findByNameIgnoreCase(trimmedName).isPresent()) {
            return "redirect:/dashboard?error=duplicate-panel";
        }

        SolarPanels panel = new SolarPanels();
        panel.setName(trimmedName);
        panel.setImageUrl(normalizeOptional(imageUrl));

        try {
            double parsedPmax = Double.parseDouble(pmax.trim());
            double parsedVoc = Double.parseDouble(voc.trim());
            double parsedIsc = Double.parseDouble(isc.trim());
            if (parsedPmax < 0 || parsedVoc < 0 || parsedIsc < 0) {
                return "redirect:/dashboard?error=empty-panel";
            }
            panel.setPmax(parsedPmax);
            panel.setVoc(parsedVoc);
            panel.setIsc(parsedIsc);
        } catch (NumberFormatException ex) {
            return "redirect:/dashboard?error=empty-panel";
        }

        solarPanelsService.saveSolarPanels(panel);
        return "redirect:/dashboard?success=panel-added";
    }

    // Delete panel
    @PostMapping("/panels/delete")
    public String deletePanel(@RequestParam Long id) {
        solarPanelsService.deletePanel(id);
        return "redirect:/dashboard?success=panel-deleted";
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String normalizeOptional(String value) {
        return isBlank(value) ? null : value.trim();
    }
}
