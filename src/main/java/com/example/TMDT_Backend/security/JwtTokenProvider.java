package com.example.TMDT_Backend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenProvider {

    //  Tạo secret key an toàn cho HS512
    private final SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);

    // Token sống 1 ngày (hoặc thay đổi giá trị tuỳ bạn)
    private final long jwtExpirationMs = 86400000;

    //  Tạo token JWT
    public String generateToken(Authentication authentication) {
        String username = authentication.getName();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();
    }

    //  Lấy username từ token
    public String getUsernameFromJWT(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    //  Kiểm tra token hợp lệ
    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // log lỗi nếu cần
        }
        return false;
    }
}
