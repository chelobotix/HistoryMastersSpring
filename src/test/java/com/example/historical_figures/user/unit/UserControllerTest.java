package com.example.historical_figures.user.unit;

import com.example.historical_figures.local_credential.LocalCredentialService;
import com.example.historical_figures.role.RoleErrorCodes;
import com.example.historical_figures.shared.exceptions.DomainException;
import com.example.historical_figures.shared.services.ExceptionMapperService;
import com.example.historical_figures.user.UserController;
import com.example.historical_figures.user.UserErrorCodes;
import com.example.historical_figures.user.UserResponse;
import com.example.historical_figures.user.support.UserFixtures;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
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
}
