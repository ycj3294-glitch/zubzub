package com.example.zubzub.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Key;
import java.util.Date;

public class JwtUtil {

    private static final String SECRET_KEY = "nZr4u7x!A%C*F-JaNdRgUkXp2s5v8y/B?E(G+KbPeShVmYq3t6w9z$C&F)J@McQf"; // 512bit
    private static final long SIGNUP_EXPIRATION = 1000 * 60 * 5; // 5분
    private static final long LOGIN_EXPIRATION = 1000 * 60 * 30;//1분, 30분
    private static final long REFRESH_EXPIRATION = 1000 * 60 * 60 * 24 * 7; // 7일
    private static final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    public static String generateSignupToken(String email, String code) {
        return Jwts.builder()
                .setSubject(email)
                .claim("code", code)
                .claim("type", JwtType.SIGNUP.name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + SIGNUP_EXPIRATION))
                .signWith(key)
                .compact();
    }

    public static String generateLoginToken(String email, long memberId, boolean isAdmin) {
        return Jwts.builder()
                .setSubject(email)
                .claim("memberId", memberId)
                .claim("role", isAdmin ? "ADMIN" : "USER")
                .claim("type", JwtType.LOGIN.name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + LOGIN_EXPIRATION))
                .signWith(key)
                .compact();
    }

    public static String generateRefreshToken(String email, long memberId, boolean isAdmin) {
        return Jwts.builder()
                .setSubject(email)
                .claim("memberId", memberId)
                .claim("role", isAdmin ? "ADMIN" : "USER")
                .claim("type", JwtType.REFRESH.name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_EXPIRATION))
                .signWith(key)
                .compact();
    }

    public static Jws<Claims> parseToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
    }

    public static boolean validateToken(String token, String code) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            System.out.println(claims.getBody().get("code", String.class));
            System.out.println(code);
            return claims.getBody().get("code", String.class).equals(code);
        } catch (JwtException e) {
            return false;
        }
    }

    public static String getEmail(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();
    }
    public static JwtType getTokenType(String token) {
        Claims claims = parseToken(token).getBody();
        String type = claims.get("type", String.class);

        if (type == null) {
            throw new JwtException("JWT type claim 없음");
        }

        return JwtType.valueOf(type);
    }



}
