package com.example.historical_figures.security.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login(@RequestParam(required = false) Boolean error,
                        @RequestParam(required = false) Boolean logout,
                        @RequestParam(required = false) Boolean expired,
                        Model model) {
        if (Boolean.TRUE.equals(error)) {
            model.addAttribute("errorMessage", "Correo o contraseña incorrectos.");
        }
        if (Boolean.TRUE.equals(logout)) {
            model.addAttribute("successMessage", "Has cerrado sesión correctamente.");
        }
        if (Boolean.TRUE.equals(expired)) {
            model.addAttribute("errorMessage", "Tu sesión ha expirado. Inicia sesión de nuevo.");
        }

        return "login";
    }
}
