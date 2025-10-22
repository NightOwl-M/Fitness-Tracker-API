package app.security.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

public final class JwtUtil {
    private JwtUtil(){}

    private static final String DEFAULT_SECRET = "change-me-dev-secret-change-me-dev-secret";
    private static final Key KEY = Keys.hmacShaKeyFor(
            (System.getenv("JWT_SECRET") != null ? System.getenv("JWT_SECRET") : DEFAULT_SECRET).getBytes()
    );

    private static Jws<Claims> parse(String token) {
        return Jwts.parserBuilder().setSigningKey(KEY).build().parseClaimsJws(token);
    }

    public static Integer userIdFrom(String token){
        Claims c = parse(token).getBody();
        Object uid = c.get("uid");
        if (uid instanceof Number n) return n.intValue();
        if (c.getSubject() != null) return Integer.valueOf(c.getSubject());
        return null;
    }

    public static String roleFrom(String token){
        return parse(token).getBody().get("role", String.class);
    }

    public static String issue(Integer userId, String role, long ttlSeconds){
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .addClaims(Map.of("uid", userId, "role", role))
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(ttlSeconds)))
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }
}
