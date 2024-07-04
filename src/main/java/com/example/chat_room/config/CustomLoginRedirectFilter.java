package com.example.chat_room.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Custom filter to redirect authenticated users away from the login, home, and register pages.
 */
@Component
public class CustomLoginRedirectFilter extends OncePerRequestFilter {

    /**
     * Filters requests to redirect authenticated users.
     *
     * @param request     the HTTP request
     * @param response    the HTTP response
     * @param filterChain the filter chain
     * @throws ServletException if a servlet error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if ("/login".equals(request.getRequestURI()) || "/".equals(request.getRequestURI()) || "/register".equals(request.getRequestURI())) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                response.sendRedirect("/chatroom");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}