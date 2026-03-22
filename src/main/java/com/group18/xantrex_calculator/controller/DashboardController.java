package com.group18.xantrex_calculator.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

        boolean isIntern = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_INTERN") || a.getAuthority().equals("INTERN"));
        model.addAttribute("isIntern", isIntern);

        // Pass current user email so template can hide self-delete button
        model.addAttribute("currentUserEmail", authentication != null ? authentication.getName() : "");

        // Fetch all users with INTERN role for the intern management table
        List<User> interns = userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.INTERN)
                .collect(Collectors.toList());
        model.addAttribute("interns", interns);
        
        return "dashboard";
    }

    @PreAuthorize("hasRole('INTERN')")
    @PostMapping("/add")
    public String addController(@ModelAttribute MpptController controller) {
        controllerRepository.save(controller);
        return "redirect:/dashboard";
    }

    @PreAuthorize("hasRole('INTERN')")
    @PostMapping("/delete")
    public String deleteController(@RequestParam Long id) {
        controllerRepository.deleteById(id);
        return "redirect:/dashboard";
    }
}
