package com.studentlearn.learnplatform.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenTest {

    private JwtToken jwtToken;

    private final String SECRET = "uhyb45jb53kjkjjg35nkjb35knjbv35knjbv35knb";
    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    @BeforeEach
    void setUp() {
        jwtToken = new JwtToken();
    }

    private String buildExpiredToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date(System.currentTimeMillis() - 1000 * 60 * 20)) // issued 20 min ago
                .setExpiration(new Date(System.currentTimeMillis() - 1000 * 60 * 10)) // expired 10 min ago
                .signWith(key)
                .compact();
    }

    private String buildTamperedToken() {
        // valid structure but signed with a different key
        Key wrongKey = Keys.hmacShaKeyFor("wrongkeywrongkeywrongkeywrongkeywrongkey".getBytes());
        return Jwts.builder()
                .setSubject("hacker@example.com")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 10))
                .signWith(wrongKey)
                .compact();
    }

    @Test
    void generateToken_ShouldReturnNonNullToken() {
        String token = jwtToken.generateToken("alice@example.com");
        assertNotNull(token);
    }

    @Test
    void generateToken_ShouldReturnNonEmptyToken() {
        String token = jwtToken.generateToken("alice@example.com");
        assertFalse(token.isBlank());
    }

    @Test
    void extractEmail_ShouldReturnSameEmailUsedToGenerateToken() {
        String token = jwtToken.generateToken("alice@example.com");
        String extracted = jwtToken.extractEmail(token);
        assertEquals("alice@example.com", extracted);
    }

    @Test
    void extractEmail_DifferentEmails_ShouldExtractCorrectly() {
        String token1 = jwtToken.generateToken("alice@example.com");
        String token2 = jwtToken.generateToken("bob@example.com");

        assertEquals("alice@example.com", jwtToken.extractEmail(token1));
        assertEquals("bob@example.com", jwtToken.extractEmail(token2));
    }

    @Test
    void extractEmail_TamperedToken_ShouldThrowRuntimeException() {
        String tampered = buildTamperedToken();
        assertThrows(RuntimeException.class, () -> jwtToken.extractEmail(tampered));
    }

    @Test
    void extractEmail_RandomString_ShouldThrowRuntimeException() {
        assertThrows(RuntimeException.class, () -> jwtToken.extractEmail("not.a.jwt"));
    }

    @Test
    void extractEmail_EmptyString_ShouldThrowRuntimeException() {
        assertThrows(RuntimeException.class, () -> jwtToken.extractEmail(""));
    }

    @Test
    void validateToken_CorrectEmailNonExpiredToken_ShouldReturnTrue() {
        String token = jwtToken.generateToken("alice@example.com");
        assertTrue(jwtToken.validateToken(token, "alice@example.com"));
    }

    @Test
    void validateToken_WrongEmail_ShouldReturnFalse() {
        String token = jwtToken.generateToken("alice@example.com");
        assertFalse(jwtToken.validateToken(token, "bob@example.com"));
    }

    @Test
    void validateToken_ExpiredToken_ShouldThrowRuntimeException() {
        // expired tokens fail during claims parsing — getClaims throws before validate can return
        String expired = buildExpiredToken("alice@example.com");
        assertThrows(RuntimeException.class, () -> jwtToken.validateToken(expired, "alice@example.com"));
    }

    @Test
    void validateToken_TamperedToken_ShouldThrowRuntimeException() {
        String tampered = buildTamperedToken();
        assertThrows(RuntimeException.class, () -> jwtToken.validateToken(tampered, "hacker@example.com"));
    }

    @Test
    void validateToken_NullEmail_ShouldThrowException() {
        String token = jwtToken.generateToken("alice@example.com");
        assertThrows(Exception.class, () -> jwtToken.validateToken(token, null));
    }

    @Test
    void getClaims_InvalidToken_ShouldThrowRuntimeExceptionWithCorrectMessage() {
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> jwtToken.extractEmail("invalid.token.here"));
        assertEquals("Jwt token is invalid", ex.getMessage());
    }

    @Test
    void getClaims_ExpiredToken_ShouldThrowRuntimeException() {
        String expired = buildExpiredToken("alice@example.com");
        assertThrows(RuntimeException.class, () -> jwtToken.extractEmail(expired));
    }
}
