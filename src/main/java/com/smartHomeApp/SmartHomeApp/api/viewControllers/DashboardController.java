package com.smartHomeApp.SmartHomeApp.api.viewControllers;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

  @GetMapping("/dashboard")
  public String dashboard(Model model, Authentication authentication) {
    model.addAttribute("pageTitle", "SmartHome - Dashboard");

    String currentUsername = null;
    if (authentication != null && authentication.getName() != null) {
      currentUsername = authentication.getName();
    }
    model.addAttribute("currentUsername", currentUsername);

    return "dashboard/index";
  }
}
