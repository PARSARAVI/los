
package com.narvee.util;

import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
/**
 * Gateway-local JwtTokenUtil.
 * Does NOT generate tokens — only validates and reads claims.
 * All tokens (PRODUCT_OWNER + TENANT_USER) are signed with the
 * same secret, so this single util handles both types.
 */
@Component
public class JwtTokenUtil {

    @Value("${jwt.secret}")
    private String jwtSecret;
  

    @PostConstruct
    public void printSecret() {
        System.out.println("PLATFORM JWT SECRET = " + jwtSecret);
        System.out.println("PLATFORM SECRET LENGTH = " + jwtSecret.length());
    }
    private Key key() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    // ── Parse all claims ──
    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // ── Validate token — throws if invalid or expired ──
    public void validateToken(String token) {
        try {
            getClaims(token);
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("Token has expired", e);
        } catch (JwtException e) {
            throw new RuntimeException("Invalid token", e);
        }
    }

    // ── Read "type" claim: PRODUCT_OWNER or TENANT_USER ──
    public String getType(String token) {
        return getClaims(token).get("type", String.class);
    }

    // ── Read tenant identifier (only present for TENANT_USER tokens) ──
    public String getTenant(String token) {
        return getClaims(token).get("tenant", String.class);
    }

    // ── Read subject (userId for PRODUCT_OWNER, email/mobile for TENANT_USER) ──
    public String getSubject(String token) {
        return getClaims(token).getSubject();
    }

    // ── Read role (SUPER_ADMIN, EMPLOYEE, CUSTOMER etc — only for TENANT_USER) ──
    public String getRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    // ── Read email claim (only present for PRODUCT_OWNER tokens) ──
    public String getEmail(String token) {
        return getClaims(token).get("email", String.class);
    }

    // ── Check expiry without throwing ──
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getClaims(token).getExpiration();
            return expiration.before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }
}