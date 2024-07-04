package com.example.chat_room.config;

import com.example.chat_room.service.UserStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuration class for web security.
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    private final CustomLoginRedirectFilter customLoginRedirectFilter;

    @Autowired
    private UserStatusService userStatusService;

    public WebSecurityConfig(CustomLoginRedirectFilter customLoginRedirectFilter) {
        this.customLoginRedirectFilter = customLoginRedirectFilter;
    }

    /**
     * Bean for password encoding.
     *
     * @return BCryptPasswordEncoder
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures the security filter chain.
     *
     * @param http the HttpSecurity configuration
     * @return SecurityFilterChain
     * @throws Exception if an error occurs
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/css/**", "/images/**", "/js/**").permitAll() // Allow access to static resources
                        .requestMatchers("/", "/login", "/register").permitAll() // Allow access to log in and register pages
                        .anyRequest().authenticated()
                )
                .formLogin((form) -> form
                        .loginPage("/login")
                        .failureUrl("/login?error=true")
                        .defaultSuccessUrl("/chatroom", true)
                        .permitAll()
                        .successHandler((request, response, authentication) -> {
                            userStatusService.setUserOnline(authentication.getName());
                            response.sendRedirect("/chatroom");
                        })
                )
                .logout((logout) -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login")
                        .permitAll()
                        .addLogoutHandler((request, response, authentication) -> {
                            if (authentication != null) {
                                userStatusService.setUserOffline(authentication.getName());
                            }
                        })
                )
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(customLoginRedirectFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}