package ma.estfbs.pfe_management.config;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ma.estfbs.pfe_management.service.JwtService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserDetailsService utilisateurDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;
        
        System.out.println("Request path: " + request.getRequestURI());
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("No valid auth header found for: " + request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }
        
        jwt = authHeader.substring(7);
        userEmail = jwtService.extractUsername(jwt);
        System.out.println("Extracted email: " + userEmail);
        
        // AJOUT: Logs supplémentaires pour déboguer
        System.out.println("Extracted user ID: " + jwtService.extractUserId(jwt));
        System.out.println("Extracted user role: " + jwtService.extractUserRole(jwt));
        
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.utilisateurDetailsService.loadUserByUsername(userEmail);
            
            // Debugging - Print roles and authorities
            System.out.println("User roles from UserDetails: " + userDetails.getAuthorities());
            System.out.println("Role from JWT: " + jwtService.extractUserRole(jwt));
            
            // AJOUT: Vérifier si le token est valide et afficher le résultat
            boolean isValid = jwtService.isTokenValid(jwt, userDetails);
            System.out.println("Is token valid? " + isValid);
            
            if (isValid) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
                );
                authToken.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);
                System.out.println("Authentication set in SecurityContext");
            } else {
                System.out.println("Token validation failed!");
            }
        }
        filterChain.doFilter(request, response);
    }
}