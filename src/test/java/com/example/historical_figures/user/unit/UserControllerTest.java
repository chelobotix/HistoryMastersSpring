package com.example.historical_figures.user.unit;

import com.example.historical_figures.local_credential.LocalCredentialService;
import com.example.historical_figures.role.RoleErrorCodes;
import com.example.historical_figures.security.config.AuthenticationFacade;
import com.example.historical_figures.shared.exceptions.DomainException;
import com.example.historical_figures.shared.services.ExceptionMapperService;
import com.example.historical_figures.user.User;
import com.example.historical_figures.user.UserController;
import com.example.historical_figures.user.UserErrorCodes;
import com.example.historical_figures.user.UserResponse;
import com.example.historical_figures.user.UserService;
import com.example.historical_figures.user.support.UserFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.LocaleResolver;

import java.util.List;
import java.util.Locale;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(controllers = UserController.class)
@Import(ExceptionMapperService.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("UserController")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LocalCredentialService localCredentialService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AuthenticationFacade authFacade;

    @MockitoBean
    private LocaleResolver localeResolver;

    @BeforeEach
    void stubLocaleResolution() {
        // Thymeleaf requires a non-null Locale when rendering views; the mock
        // resolver would otherwise return null. Lenient because the redirecting
        // endpoints never trigger locale resolution.
        lenient().when(localeResolver.resolveLocale(any())).thenReturn(Locale.ENGLISH);
    }

    @Nested
    @DisplayName("GET /users/register")
    class ShowRegisterForm {

        @Test
        @DisplayName("returns register view with empty localCredentialCreateRequest")
        void returnsRegisterView() throws Exception {
            mockMvc.perform(get("/users/register"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("register"))
                    .andExpect(model().attributeExists("localCredentialCreateRequest"));
        }
    }

    @Nested
    @DisplayName("POST /users/register")
    class CreateUser {

        @Test
        @DisplayName("redirects to login with success flash on valid request")
        void redirectsOnSuccess() throws Exception {
            when(localCredentialService.saveLocal(any())).thenReturn(
                    new UserResponse(1L, UserFixtures.DEFAULT_EMAIL, List.of(UserFixtures.ROLE_USER))
            );

            mockMvc.perform(post("/users/register")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("email", UserFixtures.DEFAULT_EMAIL)
                            .param("password", UserFixtures.DEFAULT_PASSWORD))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/login"))
                    .andExpect(flash().attribute(
                            "successMessage",
                            "Cuenta creada correctamente. Ya puedes iniciar sesión."
                    ));

            verify(localCredentialService).saveLocal(any());
        }

        @Test
        @DisplayName("returns register view when validation fails")
        void returnsRegisterOnValidationErrors() throws Exception {
            mockMvc.perform(post("/users/register")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("email", "not-an-email")
                            .param("password", "123"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("register"))
                    .andExpect(model().attributeHasFieldErrors("localCredentialCreateRequest", "email", "password"));

            verify(localCredentialService, never()).saveLocal(any());
        }

        @Test
        @DisplayName("returns register view with error when email already exists")
        void returnsRegisterWhenEmailExists() throws Exception {
            when(localCredentialService.saveLocal(any()))
                    .thenThrow(new DomainException(UserErrorCodes.USER_ALREADY_EXISTS));

            mockMvc.perform(post("/users/register")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("email", UserFixtures.DEFAULT_EMAIL)
                            .param("password", UserFixtures.DEFAULT_PASSWORD))
                    .andExpect(status().isOk())
                    .andExpect(view().name("register"))
                    .andExpect(model().attribute(
                            "errorMessage",
                            "Ya existe una cuenta con ese correo electrónico."
                    ));
        }

        @Test
        @DisplayName("lets ControllerAdvice handle other DomainExceptions")
        void delegatesOtherDomainExceptionsToAdvice() throws Exception {
            when(localCredentialService.saveLocal(any())).thenThrow(
                    new DomainException(RoleErrorCodes.ROLE_NOT_FOUND, "Role not found")
            );

            mockMvc.perform(post("/users/register")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("email", UserFixtures.DEFAULT_EMAIL)
                            .param("password", UserFixtures.DEFAULT_PASSWORD))
                    .andExpect(status().isOk())
                    .andExpect(view().name("errors/error400"));
        }
    }

    @Nested
    @DisplayName("POST /users/update_locale")
    class UpdateLocale {

        @Test
        @DisplayName("persists the locale on the current user when authenticated")
        void persistsLocaleWhenAuthenticated() throws Exception {
            User currentUser = UserFixtures.user(1L, UserFixtures.DEFAULT_EMAIL);
            when(authFacade.isAuthenticated()).thenReturn(true);
            when(authFacade.getCurrentUser()).thenReturn(currentUser);

            mockMvc.perform(post("/users/update_locale")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("locale", "es"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/home"));

            verify(userService).setLocale(currentUser, "es");
            verify(localeResolver, never()).setLocale(any(), any(), any());
        }

        @Test
        @DisplayName("stores the locale in the session via the resolver when anonymous")
        void storesLocaleWhenAnonymous() throws Exception {
            when(authFacade.isAuthenticated()).thenReturn(false);

            mockMvc.perform(post("/users/update_locale")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("locale", "es"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/home"));

            verify(localeResolver).setLocale(any(), any(), eq(Locale.of("es")));
            verify(userService, never()).setLocale(any(), any());
        }

        @Test
        @DisplayName("rejects an unsupported locale from an anonymous visitor without touching the session")
        void rejectsUnsupportedLocaleWhenAnonymous() throws Exception {
            when(authFacade.isAuthenticated()).thenReturn(false);

            mockMvc.perform(post("/users/update_locale")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("locale", "fr"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("errors/error400"));

            verify(localeResolver, never()).setLocale(any(), any(), any());
            verify(userService, never()).setLocale(any(), any());
        }
    }
}
