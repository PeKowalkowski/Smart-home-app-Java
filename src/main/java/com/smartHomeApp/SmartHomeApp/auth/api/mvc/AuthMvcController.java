package com.smartHomeApp.SmartHomeApp.auth.api.mvc;

import com.smartHomeApp.SmartHomeApp.auth.application.dto.RegisterRequest;
import com.smartHomeApp.SmartHomeApp.auth.application.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
public class AuthMvcController {

  private final AuthService authService;

  public AuthMvcController(AuthService authService) {
    this.authService = authService;
  }

  @GetMapping("/register")
  public String showRegisterForm(Model model) {
    if (!model.containsAttribute("registerRequest")) {
      model.addAttribute("registerRequest", new RegisterRequest("", "", ""));
    }
    return "auth/register";
  }

  @PostMapping("/register")
  public String submitRegister(@Valid @ModelAttribute("registerRequest") RegisterRequest request,
                               BindingResult bindingResult,
                               Model model,
                               RedirectAttributes redirectAttributes) {
    if (bindingResult.hasErrors()) {
      return "auth/register";
    }

    try {
      authService.registerUser(request);
    } catch (Exception ex) {
      model.addAttribute("error", ex.getMessage() == null ? "Registration failed" : ex.getMessage());
      return "auth/register";
    }

    redirectAttributes.addFlashAttribute("flashMessage", "Account created. Please login.");
    return "redirect:/auth/login";
  }

 @GetMapping("/login")
  public String showLogin(Model model, String error, String logout) {
    if (error != null) {
      model.addAttribute("error", "Invalid username or password");
    }
    if (logout != null) {
      model.addAttribute("message", "You have been logged out");
    }
    return "auth/login";
  }
}
