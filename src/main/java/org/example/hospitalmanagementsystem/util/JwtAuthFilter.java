package org.example.hospitalmanagementsystem.util;//import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.hospitalmanagementsystem.dto.response.ApiResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;
import java.io.IOException;

import java.util.List;
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                if (!jwtUtil.isValidToken(token)) {
                    sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid Token");
                    return;
                }
                if (!jwtUtil.isAccessToken(token)) {
                    sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Access Token Required");
                    return;
                }
                Long userId = jwtUtil.extractUserId(token);

                String role = jwtUtil.extractRole(token);


                List<SimpleGrantedAuthority> authorities = List.of(
                            new SimpleGrantedAuthority(role)
                    );

                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(userId, null, authorities);

                    SecurityContextHolder.getContext().setAuthentication(auth);
                }

            catch (ExpiredJwtException e) {
                SecurityContextHolder.clearContext();
                sendError(response, 401, "Token Expired");
                return;

            } catch (Exception e) {
                SecurityContextHolder.clearContext();
                sendError(response, 401, "Invalid Token");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void sendError(HttpServletResponse response, int status, String message) throws IOException {
        response.setContentType("application/json");
        response.setStatus(status);
        ApiResponse<Void> apiResponse = ApiResponse.error(status, message);
        objectMapper.writeValue(response.getOutputStream(), apiResponse);
    }
}
