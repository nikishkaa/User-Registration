package org.userregistrationspringsecurity.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SpringSecurity {
    @Autowired
    private UserDetailsService userDetailsService;

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests((authorize) ->
                                authorize.requestMatchers("/register/**").permitAll()
                                        .requestMatchers("/verify-email").permitAll()
                                        .requestMatchers("/home-page").permitAll()
                                        .requestMatchers("/login").permitAll()
                                        .requestMatchers("/").permitAll()
                                        .requestMatchers("/css/**").permitAll()
                                        .requestMatchers("/js/**").permitAll()
                                        .requestMatchers("/img/**").permitAll()
                                        .requestMatchers("/users").hasRole("admin-user")
                                        .anyRequest().authenticated()
//                                      .anyRequest().permitAll() // Доступ ко всем неуказанным ресурсам
                ).formLogin(
                        form -> form
                                .loginPage("/login")
                                .loginProcessingUrl("/perform_login")
                                .defaultSuccessUrl("/home-page")
                                .failureHandler(authenticationFailureHandler())
                                .permitAll()
                ).logout(
                        logout -> logout
                                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                                .logoutSuccessUrl("/home-page")
                                .permitAll()
                );
        return http.build();
    }

    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return (request, response, exception) -> {
            boolean isDisabled = exception instanceof DisabledException;

            Throwable cause = exception.getCause();
            while (cause != null && !isDisabled) {
                if (cause instanceof DisabledException) {
                    isDisabled = true;
                    break;
                }
                cause = cause.getCause();
            }

            if (!isDisabled) {
                String msg = exception.getMessage();
                if (msg != null && msg.toLowerCase().contains("not verified")) {
                    isDisabled = true;
                }
            }

            if (isDisabled) {
                response.sendRedirect("/verify-email");
            } else {
                response.sendRedirect("/login?error");
            }
        };
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
    }
}