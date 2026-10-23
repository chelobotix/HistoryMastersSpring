package com.example.historical_figures.home;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String root() {
        return "redirect:/home";
    }

    // TODO:
    @GetMapping("/home")
    public String index(Model model) {
        
        return "home/index";
    }
}
