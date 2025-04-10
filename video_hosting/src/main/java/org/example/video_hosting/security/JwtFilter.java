package org.example.video_hosting.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.api.ErrorMessage;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);
    private final JwtProvider jwtProvider;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");

        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);

            try {
                authenticateUser(token, request);
            } catch (ExpiredJwtException e) {
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "JWT expired", e);
                return;
            } catch (SignatureException e) {
                sendErrorResponse(response, 435, "JWT invalid", e);
                return;
            } catch (Exception e) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage(), e);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Foydalanuvchini token orqali autentifikatsiya qilish
     */
    private void authenticateUser(String token, HttpServletRequest request) {
        String phoneNumberFromToken = jwtProvider.getEmailFromToken(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(phoneNumberFromToken);

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    /**
     * Xatolikni JSON shaklida qaytarish
     */
    private void sendErrorResponse(HttpServletResponse response, int status, String message, Exception e) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");

        ErrorMessage errorMessage = new ErrorMessage(message);
        logger.error("JWT Error: {} | Exception: {}", errorMessage.getMessage(), e.getMessage());

        new ObjectMapper().writeValue(response.getWriter(), errorMessage);
    }
}