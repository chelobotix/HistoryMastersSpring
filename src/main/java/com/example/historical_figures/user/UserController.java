package com.example.historical_figures.user;

import com.example.historical_figures.local_credential.LocalCredentialCreateRequest;
import com.example.historical_figures.local_credential.LocalCredentialService;
import com.example.historical_figures.security.config.AuthenticationFacade;
import com.example.historical_figures.shared.exceptions.DomainException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Locale;

@Controller
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final LocalCredentialService localCredentialService;
    private final UserService userService;
    private final AuthenticationFacade authFacade;
    private final LocaleResolver localeResolver;

    @GetMapping("/register")
    public String newUser(Model model) {
        model.addAttribute("localCredentialCreateRequest", new LocalCredentialCreateRequest(null, null));
        
        return "register";
    }

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String create(@Valid @ModelAttribute("localCredentialCreateRequest") LocalCredentialCreateRequest localCredentialCreateRequest,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            localCredentialService.saveLocal(localCredentialCreateRequest);
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

    @PostMapping("/update_locale")
    public String updateLocale(
            @RequestParam String locale,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        if (authFacade.isAuthenticated()) {
            User currentUser = authFacade.getCurrentUser();
            userService.setLocale(currentUser, locale);
        } else {
            UserLocaleSupportedLanguages.validate(locale);
            localeResolver.setLocale(request, response, Locale.of(locale.toLowerCase()));
        }

        return "redirect:/home";
    }
}
