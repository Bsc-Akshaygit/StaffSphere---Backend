package com.studentlearn.learnplatform.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtToken {

    public final String SECRET = "uhyb45jb53kjkjjg35nkjb35knjbv35knjbv35knb";
    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 10))
                .signWith(key)
                .compact();
    }

    public String extractEmail(String token){
        return getClaims(token).getSubject();
    }

    public boolean validateToken(String token, String email){
        return email.equals(extractEmail(token)) && !isExpire(token);
    }

    private boolean isExpire(String token){
        return getClaims(token).getExpiration().before(new Date());
    }

    private Claims getClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

        } catch (Exception e) {

            e.printStackTrace();

            throw new RuntimeException("Jwt token is invalid", e);
        }
    }


}
