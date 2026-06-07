package com.example.historical_figures.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SpringSecurityConfig {

    @Value("${security.require-ssl:false}")
    private boolean requireSsl;

    @Bean
    public SecurityFilterChain authorizationServerSecurityFilterChain(
            HttpSecurity http,
            FilteredRequestCache requestCache) throws Exception {
        OAuth2AuthorizationServerConfigurer asc = OAuth2AuthorizationServerConfigurer.authorizationServer();

        http.authorizeHttpRequests(requests -> requests
                .requestMatchers(
                    "/",
                    "/home",
                    "/login",
                    "/users/register",
                    "/css/**",
                    "/error/**",
                    "/.well-known/**"
                ).permitAll()
                .anyRequest().authenticated());

        

        if (requireSsl) {
            http.requiresChannel(channel -> channel.anyRequest().requiresSecure());
        }

        http.csrf(withDefaults());

        http.formLogin(flc -> flc
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/home")
                .failureUrl("/login?error=true"));

        http.requestCache(cache -> cache.requestCache(requestCache));

        http.sessionManagement(sm -> sm
                .maximumSessions(1)
                .maxSessionsPreventsLogin(true)
                .expiredUrl("/login?expired=true"));

        http.logout(loc -> loc
                .logoutSuccessUrl("/home")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID"));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
