package com.example.zubzub.security;

import com.example.zubzub.service.CustomUserDetailsService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

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
                log.info("1: 토큰 확인 중");
                Jws<Claims> claims = JwtUtil.parseToken(token);

                String type = claims.getBody().get("type", String.class);
                if (!"LOGIN".equals(type)) {
                    filterChain.doFilter(request, response);
                    return;
                }
                log.info("2: 토큰 타입 LOGIN 확인");

                String email = claims.getBody().getSubject();

                // 🔥 [수정] 토큰에서 role 꺼내기 (ADMIN 또는 USER)
                String role = claims.getBody().get("role", String.class);
                log.info("추출된 role: {}", role);

                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                // 🔥 [수정] 토큰의 role을 기반으로 권한 생성 ("ROLE_" 접두사 추가)
                // 시큐리티의 hasRole("ADMIN")은 "ROLE_ADMIN"을 찾습니다.
                List<SimpleGrantedAuthority> authorities =
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                authorities // 👈 DB 권한 대신 토큰 권한 주입
                        );

                log.info("3: 시큐리티 권한 주입 완료 -> {}", authorities);

                SecurityContextHolder.getContext().setAuthentication(auth);
                log.info("4: 인증 컨텍스트 설정 완료");

            } catch (JwtException e) {
                SecurityContextHolder.clearContext();
                log.warn("[JWT FILTER] 토큰 유효하지 않음: {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}