package com.example.userservice.config;

import com.example.userservice.filter.JwtFilterToken;
import com.example.userservice.service.AuthenticationService;
import com.example.userservice.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final AuthenticationService authenticationService;
    private final JwtService jwtService;
    private final String[] permitAll = {"/swagger-ui/**", "/v3/api-docs/**", "/user/auth/**"};

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(Customizer.withDefaults())
                .csrf().disable()
                .authorizeHttpRequests((authorize) -> {
                    authorize
                            .requestMatchers(permitAll).permitAll()
                            .anyRequest().authenticated();
                })
                .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(new JwtFilterToken(authenticationService, jwtService), UsernamePasswordAuthenticationFilter.class)
                .build();
    }
//    @Bean
//    CorsConfigurationSource corsConfigurationSource(){
//        CorsConfiguration corsConfiguration = new CorsConfiguration();
//        corsConfiguration.setAllowedOrigins(List.of("http://localhost:3000"));
//        corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
//        corsConfiguration.setAllowedHeaders(List.of("*"));
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", corsConfiguration);
//        return source;
//    }

}
