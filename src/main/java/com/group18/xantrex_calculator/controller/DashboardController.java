package com.group18.xantrex_calculator.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.group18.xantrex_calculator.repository.MpptControllerRepository;
import com.group18.xantrex_calculator.service.SolarPanelsService;

import com.group18.xantrex_calculator.entity.MpptController;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {
    private final MpptControllerRepository controllerRepository;
    private final SolarPanelsService solarPanelsService;

    public DashboardController(MpptControllerRepository controllerRepository, SolarPanelsService solarPanelsService) {
        this.controllerRepository = controllerRepository;
        this.solarPanelsService = solarPanelsService;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("controllers", controllerRepository.findAll());
        model.addAttribute("panels", solarPanelsService.getAllPanels());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isIntern = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_INTERN"));
        model.addAttribute("isIntern", isIntern);

        return "dashboard";
    }

    @PostMapping("/add")
    public String addController(@ModelAttribute MpptController controller) {
        controllerRepository.save(controller);
        return "redirect:/dashboard";
    }

    @PostMapping("/delete")
    public String deleteController(@RequestParam Long id) {
        controllerRepository.deleteById(id);
        return "redirect:/dashboard";
    }
}
