package ma.estfbs.pfe_management.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                // Allow authentication endpoints without authentication
                .requestMatchers("/api/auth/login").permitAll()
                .requestMatchers("/api/auth/register").permitAll()
                .requestMatchers("/api/auth/reset-password-request").permitAll()
                .requestMatchers("/api/auth/reset-password-confirm").permitAll()
                
                
                // Require authentication for all Spring Data REST endpoints
                .requestMatchers("/api/binome/**").authenticated()
                .requestMatchers("/api/filiere/**").authenticated()
                .requestMatchers("/api/sujet/**").authenticated()
                .requestMatchers("/api/soutenance/**").authenticated()
                .requestMatchers("/api/note-soutenance/**").authenticated()
                .requestMatchers("/api/rapport/**").authenticated()
                .requestMatchers("/api/demande-binome/**").authenticated()
                .requestMatchers("/api/documents-evaluation/**").authenticated()
                .requestMatchers("/api/pourcentage/**").authenticated()
                .requestMatchers("/api/note-finale/**").authenticated()
                .requestMatchers("/api/salle/**").authenticated()
                .requestMatchers("/api/proposer-sujets/**").authenticated()
                
                // Any other request requires authentication
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
            
        return http.build();
    }
}