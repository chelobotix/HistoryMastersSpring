package com.example.historical_figures.security;

import com.example.historical_figures.security.config.GoogleOAuth2LoginSuccessHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.DelegatingSecurityContextRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SpringSecurityConfig {

    @Value("${security.require-ssl:false}")
    private boolean requireSsl;

    /**
     * Exposed as a bean so GoogleOAuth2LoginSuccessHandler writes the context it builds
     * into the same store the filter chain reads on the next request.
     */
    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new DelegatingSecurityContextRepository(
                new RequestAttributeSecurityContextRepository(),
                new HttpSessionSecurityContextRepository()
        );
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            FilteredRequestCache requestCache,
            GoogleOAuth2LoginSuccessHandler googleOAuth2LoginSuccessHandler,
            SecurityContextRepository securityContextRepository) throws Exception {
        http.authorizeHttpRequests(requests -> requests
                .requestMatchers(
                    "/",
                        "/css/**",
                        "/error/**",
                    "/home",
                    "/login",
                    "/oauth2/**",
                        "/users/register",
                        "/users/update_locale",
                    "/.well-known/**"
                ).permitAll()
                .anyRequest().authenticated());

        if (requireSsl) {
            http.requiresChannel(channel -> channel.anyRequest().requiresSecure());
        }

        http.csrf(withDefaults());

        http.securityContext(sc -> sc.securityContextRepository(securityContextRepository));

        http.formLogin(flc -> flc
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/home")
                .failureUrl("/login?error=true&type=credentials"));

        http.oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .successHandler(googleOAuth2LoginSuccessHandler)
                .failureUrl("/login?error=true&type=oauth2"));

        http.requestCache(cache -> cache.requestCache(requestCache));

        http.logout(loc -> loc
                .logoutSuccessUrl("/home")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID"));

        return http.build();
    }
}
