package com.example.zubzub.security;

import com.example.zubzub.service.CustomUserDetailsService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final CustomUserDetailsService userDetailsService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/members/signup")
                || path.startsWith("/api/members/login")
                || path.startsWith("/api/members/token/refresh");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {


        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            try {
                log.info("1");
                Jws<Claims> claims = JwtUtil.parseToken(token);
                // 🔥 LOGIN 토큰만 인증 처리
                String type = claims.getBody().get("type", String.class);
                if (!"LOGIN".equals(type)) {
                    filterChain.doFilter(request, response);
                    return;
                }
                log.info("2");

                String email = claims.getBody().getSubject();
                UserDetails userDetails =
                        userDetailsService.loadUserByUsername(email);

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                log.info("3");

                SecurityContextHolder.getContext().setAuthentication(auth);
                log.info("4");

            } catch (JwtException e) {
                SecurityContextHolder.clearContext();
                log.warn("[JWT FILTER] 토큰 유효하지 않음: {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}
