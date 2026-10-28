package com.btlon.movie.store.security;

import com.btlon.movie.store.entity.User;
import com.btlon.movie.store.entity.UserRole;
import com.btlon.movie.store.service.AuthService;
import com.btlon.movie.store.web.ForbiddenException;
import com.btlon.movie.store.web.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Resolves the user from "Authorization: Bearer <token>" and
 * enforces @LoginRequired / @AdminOnly.
 * A token that is sent but invalid or expired is rejected even on public
 * endpoints,
 * so the client learns its session ended instead of silently booking as a
 * guest.
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {
    static final String CURRENT_USER = AuthInterceptor.class.getName() + ".user";
    private static final String BEARER = "Bearer ";
    private final AuthService authService;

    public AuthInterceptor(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod method)) return true;
        String token = bearerToken(request);
        User user = null;
        if (token != null) {
            user = authService.authenticate(token)
                    .orElseThrow(() -> new UnauthorizedException("Session expired, please log in again"));
            request.setAttribute(CURRENT_USER, user);
        }
        boolean adminOnly = has(method, AdminOnly.class);
        if ((adminOnly || has(method, LoginRequired.class)) && user == null)
            throw new UnauthorizedException("Login required");
        if (adminOnly && user.getRole() != UserRole.ADMIN)
            throw new ForbiddenException("Admin role required");
        return true;
    }

    static String bearerToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith(BEARER)) return null;
        String token = header.substring(BEARER.length()).trim();
        return token.isEmpty() ? null : token;
    }

    private static boolean has(HandlerMethod method, Class<? extends java.lang.annotation.Annotation> annotation) {
        return method.hasMethodAnnotation(annotation) || method.getBeanType().isAnnotationPresent(annotation);
    }
}
