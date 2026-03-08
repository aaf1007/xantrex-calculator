  package com.group18.xantrex_calculator.controller;                            
                                                                                
  import org.springframework.stereotype.Controller;                             
  import org.springframework.ui.Model;
  import org.springframework.web.bind.annotation.*;

  import java.util.ArrayList;

  @Controller
  @RequestMapping("/dashboard")
  public class DashboardController {

      @GetMapping
      public String dashboard(Model model) {
          model.addAttribute("controllers", new ArrayList<>());
          return "dashboard";
      }

      @PostMapping("/add")
      public String addController() {
          // TODO: After Antons work
          return "redirect:/dashboard";
      }

      @PostMapping("/delete")
      public String deleteController() {
          // TODO: After Antons work
          return "redirect:/dashboard";
      }
  }
