package com.example.historical_figures.user;

import com.example.historical_figures.shared.exceptions.DomainException;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String newUser(Model model) {
        model.addAttribute("userCreateRequest", new UserCreateRequest(null, null));
        
        return "register";
    }

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String create(@Valid @ModelAttribute("userCreateRequest") UserCreateRequest userCreateRequest,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            userService.save(userCreateRequest);
        } catch (DomainException ex) {
            if (ex.getErrorCode() == UserErrorCodes.USER_ALREADY_EXISTS) {
                model.addAttribute("errorMessage", "Ya existe una cuenta con ese correo electrónico.");
                return "register";
            }
            throw ex;
        }

        redirectAttributes.addFlashAttribute("successMessage", "Cuenta creada correctamente. Ya puedes iniciar sesión.");
        return "redirect:/login";
    }
}
