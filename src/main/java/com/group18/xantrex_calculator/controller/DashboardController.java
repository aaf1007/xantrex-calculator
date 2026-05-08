package com.group18.xantrex_calculator.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.group18.xantrex_calculator.entity.MpptController;
import com.group18.xantrex_calculator.entity.Role;
import com.group18.xantrex_calculator.entity.User;
import com.group18.xantrex_calculator.repository.MpptControllerRepository;
import com.group18.xantrex_calculator.repository.UserRepository;
import com.group18.xantrex_calculator.service.SolarPanelsService;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {
    private final MpptControllerRepository controllerRepository;
    private final SolarPanelsService solarPanelsService;
    private final UserRepository userRepository;

    public DashboardController(MpptControllerRepository controllerRepository,
                               SolarPanelsService solarPanelsService,
                               UserRepository userRepository) {
        this.controllerRepository = controllerRepository;
        this.solarPanelsService = solarPanelsService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String dashboard(Model model, Authentication authentication) {
        model.addAttribute("controllers", controllerRepository.findAll());
        model.addAttribute("panels", solarPanelsService.getAllPanels());

        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ADMIN"));
        model.addAttribute("isAdmin", isAdmin);

        // Pass current user email so template can hide self-delete button
        model.addAttribute("currentUserEmail", authentication != null ? authentication.getName() : "");

        // Fetch all users with ADMIN role for the admin management table
        List<User> admins = userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.ADMIN)
                .collect(Collectors.toList());
        model.addAttribute("admins", admins);
        
        return "dashboard";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/add")
    public String addController(@RequestParam(required = false) String name,
                                @RequestParam(required = false) String maxVoc,
                                @RequestParam(required = false) String maxCurrent,
                                @RequestParam(required = false) String maxIsc,
                                @RequestParam(required = false) String batteryBank,
                                @RequestParam(required = false) String imageUrl,
                                @RequestParam(required = false) String productUrl) {
        if (isBlank(name) || isBlank(maxVoc) || isBlank(maxCurrent) || isBlank(maxIsc) || isBlank(batteryBank)) {
            return "redirect:/dashboard?error=empty-controller";
        }

        String trimmedName = name.trim();
        if (controllerRepository.findByNameIgnoreCase(trimmedName).isPresent()) {
            return "redirect:/dashboard?error=duplicate-controller";
        }

        MpptController controller = new MpptController();
        controller.setName(trimmedName);
        controller.setBatteryBank(batteryBank.trim());
        controller.setImageUrl(normalizeOptional(imageUrl));
        controller.setProductUrl(normalizeOptional(productUrl));

        try {
            double parsedMaxVoc = Double.parseDouble(maxVoc.trim());
            double parsedMaxCurrent = Double.parseDouble(maxCurrent.trim());
            double parsedMaxIsc = Double.parseDouble(maxIsc.trim());
            if (parsedMaxVoc < 0 || parsedMaxCurrent < 0 || parsedMaxIsc < 0) {
                return "redirect:/dashboard?error=empty-controller";
            }
            controller.setMaxVoc(parsedMaxVoc);
            controller.setMaxCurrent(parsedMaxCurrent);
            controller.setMaxIsc(parsedMaxIsc);
        } catch (NumberFormatException ex) {
            return "redirect:/dashboard?error=empty-controller";
        }

        controllerRepository.save(controller);
        return "redirect:/dashboard?success=controller-added";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/delete")
    public String deleteController(@RequestParam Long id) {
        controllerRepository.deleteById(id);
        return "redirect:/dashboard?success=controller-deleted";
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String normalizeOptional(String value) {
        return isBlank(value) ? null : value.trim();
    }
}
