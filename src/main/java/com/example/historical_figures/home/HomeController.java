package com.example.historical_figures.home;

import com.example.historical_figures.user.User;
import com.example.historical_figures.user.UserCreateRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String root() {
        return "redirect:/home";
    }

    @GetMapping("/home")
    public String index(Model model) {
        
        return "home/index";
    }
}
