package com.energymeter.apigateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;

@Component
public class JwtUtil {

    private static final Key SECRET_KEY = Keys.hmacShaKeyFor(Decoders.BASE64.decode("sampleAppSecretKeyWhichMayNotBeSecureEnough"));

    public static boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(SECRET_KEY)
                    .build().parseClaimsJws(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isTokenExpired(String jwt) {
        return extractExpiration(jwt).before(new Date());
    }

    public static Date extractExpiration(String jwt) {
        return extractClaims(jwt, Claims::getExpiration);
    }

    private static <T> T extractClaims(String jwt, Function<Claims, T> claimResolver) {
        var claims = Jwts.parserBuilder().setSigningKey(SECRET_KEY).build().parseClaimsJws(jwt).getBody();
        return claimResolver.apply(claims);
    }

    public static String extractRole(String jwt) {
        var claims = Jwts.parserBuilder().setSigningKey(SECRET_KEY).build().parseClaimsJws(jwt).getBody();
        var roles = (List<LinkedHashMap<String, String>>) claims.get("roles");
        return roles.get(0).get("authority");
    }

    public static String extractUserId(String jwt) {
        return (extractClaims(jwt, claims -> claims.get("userId"))).toString();
    }

    public static String extractUsername(String jwt) {
        return extractClaims(jwt, Claims::getSubject);
    }
}
