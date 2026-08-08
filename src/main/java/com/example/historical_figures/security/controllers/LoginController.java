package com.example.historical_figures.security.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login(@RequestParam(required = false) Boolean error,
                        @RequestParam(required = false) String type,
                        Model model) {
        if (Boolean.TRUE.equals(error)) {
            if ("credentials".equals(type)) {
                model.addAttribute("errorMessage", "Correo o contraseña incorrectos.");
            }
            if ("oauth2".equals(type)) {
                model.addAttribute("errorMessage", "Fallo el inicio de sesion con cuenta externa");
            }
        }

        return "login";
    }
}
