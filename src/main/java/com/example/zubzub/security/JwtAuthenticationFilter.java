package com.example.zubzub.security;

import com.example.zubzub.service.CustomUserDetailsService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
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

    /*@Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {


        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            try {
                Jws<Claims> claims = JwtUtil.parseToken(token);
                // 🔥 LOGIN 토큰만 인증 처리
                String type = claims.getBody().get("type", String.class);
                if (!"LOGIN".equals(type)) {
                    filterChain.doFilter(request, response);
                    return;
                }

                String email = claims.getBody().getSubject();
                UserDetails userDetails =
                        userDetailsService.loadUserByUsername(email);

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                SecurityContextHolder.getContext().setAuthentication(auth);

            } catch (JwtException e) {
                SecurityContextHolder.clearContext();
                log.warn("[JWT FILTER] 토큰 유효하지 않음: {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }*/

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authorizationHeader = request.getHeader("Authorization");
        String token = null;

        // "Bearer <token>" 형태라면 앞의 "Bearer " 제거
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);
        }

        log.info("token : {}", token);


        // 2. 토큰이 있으면 검증
        if (token != null) {
            try {
                log.info("1");
                Jws<Claims> claims = JwtUtil.parseToken(token);
                log.info("2");

                // 🔥 LOGIN 토큰만 인증 처리
                String type = claims.getBody().get("type", String.class);
                if (!"LOGIN".equals(type)) {
                    filterChain.doFilter(request, response);
                    return;
                }
                log.info("3");

                String email = claims.getBody().getSubject();
                log.info("4");
                UserDetails userDetails =
                        userDetailsService.loadUserByUsername(email);
                log.info("5");

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                log.info("6");

                SecurityContextHolder.getContext().setAuthentication(auth);
                log.info("7");

            } catch (JwtException e) {
                SecurityContextHolder.clearContext();
                log.warn("[JWT FILTER] 토큰 유효하지 않음: {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}
