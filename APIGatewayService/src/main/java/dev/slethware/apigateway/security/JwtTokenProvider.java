package dev.slethware.apigateway.security;

import dev.slethware.apigateway.exception.UnauthorizedException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.UUID;

@Slf4j
@Component
public class JwtTokenProvider {

    private final Key jwtSigningKey;

    public JwtTokenProvider(@Value("${jwt.secret}") String jwtSecret) {
        if (jwtSecret.length() < 44) {
            log.warn("JWT Secret is less than 44 characters (Base64-encoded 256-bit key). This may be insecure.");
        }
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        this.jwtSigningKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(jwtSigningKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SignatureException ex) {
            log.error("Invalid JWT signature: {}", ex.getMessage());
            throw new UnauthorizedException("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token: {}", ex.getMessage());
            throw new UnauthorizedException("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token: {}", ex.getMessage());
            throw new UnauthorizedException("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token: {}", ex.getMessage());
            throw new UnauthorizedException("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty: {}", ex.getMessage());
            throw new UnauthorizedException("JWT claims string is empty");
        }
    }

    public UUID getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(jwtSigningKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        String userId = claims.get("user_id", String.class);
        if (userId == null) {
            userId = claims.getSubject();
        }

        if (userId == null) {
            throw new UnauthorizedException("user_id not found in JWT");
        }
        return UUID.fromString(userId);
    }

    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(jwtSigningKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        String email = claims.get("email", String.class);
        if (email == null) {
            throw new UnauthorizedException("email not found in JWT");
        }
        return email;
    }
}