package com.wrongweather.moipzy.domain.jwt;

import com.wrongweather.moipzy.domain.users.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
public class JwtTokenFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String requestPath = request.getRequestURI();
        if(requestPath.equals("/moipzy/auth/login") || requestPath.equals("/moipzy/users/register")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authorizationHeader = request.getHeader("Authorization");
        if(authorizationHeader != null || authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);

            try {
                Map<String, Object> claims = JwtTokenUtil.extractClaims(token);

                request.setAttribute("userId", claims.get("userId"));
                request.setAttribute("email", claims.get("email"));
                request.setAttribute("name", claims.get("name"));

                filterChain.doFilter(request, response);
                return;
            } catch (Exception e) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired JWT token");
                return;
            }
        }
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT token required");
    }

}
