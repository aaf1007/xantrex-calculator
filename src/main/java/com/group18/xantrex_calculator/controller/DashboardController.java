package com.group18.xantrex_calculator.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.group18.xantrex_calculator.repository.MpptControllerRepository;

import com.group18.xantrex_calculator.entity.MpptController;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {
    private final MpptControllerRepository controllerRepository;

    public DashboardController(MpptControllerRepository controllerRepository) {
        this.controllerRepository = controllerRepository;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("controllers", controllerRepository.findAll());
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
