package com.example.zubzub.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Slf4j
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {

        // 헤더에서 Authorization 추출
        String authHeader = request.getHeaders().getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("[WS] Authorization 헤더 없음");
            return false; //
        }

        String token = authHeader.substring(7);

        try {
            Jws<Claims> claims = JwtUtil.parseToken(token);
            String email = claims.getBody().getSubject();

            // WS 세션 attributes 저장
            attributes.put("email", email);
            attributes.put("principal", email); //이름표 같은 느낌

            log.info("[WS] 인증 성공 email={}", email);
            return true;

        } catch (JwtException e) {
            log.warn("[WS] 토큰 검증 실패: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
    }
}

