package com.example.TpDA1.config;

import com.example.TpDA1.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final HandlerExceptionResolver handlerExceptionResolver;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserDetailsService userDetailsService,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver handlerExceptionResolver
    ) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.handlerExceptionResolver = handlerExceptionResolver;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            final String authHeader = request.getHeader("Authorization");
            final String requestPath = request.getRequestURI();

            // Permitir acceso sin token a endpoints públicos
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                logger.debug("No authentication token found for path: {}", requestPath);
                filterChain.doFilter(request, response);
                return;
            }

            final String jwt = authHeader.substring(7);
            
            // Validación básica del token
            if (jwt.isEmpty() || jwt.split("\\.").length != 3) {
                logger.warn("Invalid JWT token format for path: {}", requestPath);
                filterChain.doFilter(request, response);
                return;
            }

            final String username = jwtService.extractUsername(jwt);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (username != null && authentication == null) {
                try {
                    logger.debug("Attempting to load user details for: {}", username);
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    
                    if (jwtService.isTokenValid(jwt, userDetails)) {
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                        
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        logger.debug("Authentication successful for user: {}", username);
                    } else {
                        logger.warn("Invalid JWT token for user: {} on path: {}", username, requestPath);
                    }
                } catch (org.springframework.dao.InvalidDataAccessResourceUsageException dbException) {
                    logger.error("Database error loading user details for: {} - {}", username, dbException.getMessage());
                    // No propagar el error, simplemente continuar sin autenticación
                } catch (Exception userLoadException) {
                    logger.error("Error loading user details for: {} - {}", username, userLoadException.getMessage());
                    // No establecer autenticación si hay error cargando el usuario
                }
            }
            
            filterChain.doFilter(request, response);
            
        } catch (Exception e) {
            logger.error("Critical error in JWT authentication filter: {}", e.getMessage());
            // En lugar de devolver error 401, continuar sin autenticación
            // Esto permite que endpoints públicos funcionen incluso con errores
            filterChain.doFilter(request, response);
        }
    }
}