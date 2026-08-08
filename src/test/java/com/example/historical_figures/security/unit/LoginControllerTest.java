package com.example.historical_figures.security.unit;

import com.example.historical_figures.security.controllers.LoginController;
import com.example.historical_figures.shared.services.ExceptionMapperService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(controllers = LoginController.class)
@Import(ExceptionMapperService.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("LoginController")
class LoginControllerTest {

    private static final CsrfToken CSRF = new DefaultCsrfToken("X-CSRF-TOKEN", "_csrf", "test-csrf-token");

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("renders the login view without messages")
    void rendersPlainLogin() throws Exception {
        mockMvc.perform(loginGet())
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeDoesNotExist("errorMessage"));
    }

    @Test
    @DisplayName("shows the credentials message for the form-login failure url")
    void showsCredentialsError() throws Exception {
        mockMvc.perform(loginGet().param("error", "true").param("type", "credentials"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("errorMessage", "Correo o contraseña incorrectos."));
    }

    /**
     * The url is duplicated on purpose: it is the contract shared with the oauth2Login
     * failureUrl and with GoogleOAuth2LoginSuccessHandler#rejectOAuth2. A non-boolean
     * value for "error" makes this endpoint answer 400 instead of rendering the page.
     */
    @Test
    @DisplayName("shows the oauth2 message for the oauth2 rejection url")
    void showsOauth2Error() throws Exception {
        mockMvc.perform(loginGet().param("error", "true").param("type", "oauth2"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("errorMessage", "Fallo el inicio de sesion con cuenta externa"));
    }

    @Test
    @DisplayName("shows no message for an unknown error type")
    void ignoresUnknownType() throws Exception {
        mockMvc.perform(loginGet().param("error", "true").param("type", "something-else"))
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("errorMessage"));
    }

    @Test
    @DisplayName("shows no message when error is absent")
    void ignoresTypeWithoutError() throws Exception {
        mockMvc.perform(loginGet().param("type", "credentials"))
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("errorMessage"));
    }

    private static MockHttpServletRequestBuilder loginGet() {
        return get("/login")
                .requestAttr("_csrf", CSRF)
                .requestAttr(CsrfToken.class.getName(), CSRF);
    }
}
