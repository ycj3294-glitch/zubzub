package com.example.zubzub.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;

public class JwtUtil {

    private static final String SECRET_KEY = "your-512-bit-secret-your-512-bit-secret-your-512-bit-secret-your-512-bit-secret"; // 512bit
    private static final long EXPIRATION = 1000 * 60 * 5; // 5분

    private static final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    public static String generateToken(String email, String code) {
        return Jwts.builder()
                .setSubject(email)
                .claim("code", code)
                .claim("type", JwtType.SIGNUP.name())   // ✅ 추가
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(key)
                .compact();
    }

    public static String generateTokenForLogin(String email, long memberId, boolean isAdmin) {
        return Jwts.builder()
                .setSubject(email)
                .claim("memberId", memberId)
                .claim("role", isAdmin ? "ADMIN" : "USER")
                .claim("type", JwtType.LOGIN.name())    // ✅ 추가
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 30 * 1000))
                .signWith(key)
                .compact();
    }

    public static String generateRefreshToken(String email, long memberId, boolean isAdmin) {
        return Jwts.builder()
                .setSubject(email)
                .claim("memberId", memberId)
                .claim("role", isAdmin ? "ADMIN" : "USER")
                .claim("type", JwtType.REFRESH.name())  // ✅ 추가
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000))
                .signWith(key)
                .compact();
    }

    public static Jws<Claims> parseToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
    }

    public static boolean validateToken(String token, String code) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
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
