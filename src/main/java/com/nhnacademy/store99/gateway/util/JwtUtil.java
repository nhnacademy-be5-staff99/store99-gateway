package com.nhnacademy.store99.gateway.util;

import com.nhnacademy.store99.gateway.property.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import java.security.Key;
import java.time.Duration;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 토큰 파싱, 만료 기간 검사
 *
 * @author Ahyeon Song
 */
@Slf4j
@Component
public class JwtUtil {

    public static final String ACCESS_TOKEN = "access-token";
    public static final long ACCESS_TOKEN_EXPIRED_TIME = Duration.ofDays(30).toMillis();
    private static final String BEARER_PREFIX = "Bearer";
    private final String secretKey;

    public JwtUtil(JwtProperties jwtProperties) {
        this.secretKey = jwtProperties.getSecret();
    }


    private Key key() {
        byte[] decodeKey = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(decodeKey);
    }


    /**
     * Token 에서 Claim 부분 파싱하여 제공하는 메소드
     *
     * @param token
     * @return Claims claims
     */
    public Claims getClaims(String token) {
        token = removeBearerPrefix(token);
        return Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Token 에서 UUID 부분 파싱하여 제공하는 메소드
     *
     * @param token
     * @return String UUID
     */
    public String getUUID(String token) {
        token = removeBearerPrefix(token);
        return Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("UUID", String.class);
    }


    /**
     * Token 에서 만료기간 파싱하여 제공하는 메소드
     *
     * @param token
     * @return Date expiredTime
     */
    public Date getExpiredTime(String token) {
        token = removeBearerPrefix(token);
        return Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
    }


    /**
     * 토큰이 만료 기간 전인지 확인
     *
     * @param token
     * @return true, false
     */
    public boolean isValidToken(String token) {
        token = removeBearerPrefix(token);
        try {
            Jws<Claims> claimsJws = Jwts.parserBuilder()
                    .setSigningKey(key())
                    .build()
                    .parseClaimsJws(token);
            return claimsJws.getBody().getExpiration().after(new Date());

        } catch (SignatureException | MalformedJwtException | ExpiredJwtException e) {
            return false;
        }
    }

    /**
     * 접두사 Bearer 삭제
     *
     * @param token
     * @return Bearer 를 제외한 token
     */
    private String removeBearerPrefix(String token) {
        if (token.startsWith(BEARER_PREFIX)) {
            return token.substring(BEARER_PREFIX.length()).trim();
        }
        return token;
    }


}
