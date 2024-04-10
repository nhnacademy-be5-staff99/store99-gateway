package com.nhnacademy.store99.gateway.filter;

import com.nhnacademy.store99.gateway.util.JwtUtil;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 인가 필터
 *
 * @author Ahyeon Song
 */
@Component
public class AuthorizationFilter extends AbstractGatewayFilterFactory<AuthorizationFilter.Config> {

    public AuthorizationFilter() {
        super(Config.class);
    }

    /**
     * Redis 에서 UUID 에 맞는 userId 가져오는 메소드
     *
     * @param config
     * @param uuid
     * @return userId
     */
    private static String getUserId(Config config, String uuid) {
        String userId = (String) config.redisTemplate.opsForValue().get(uuid);

        if (Objects.isNull(userId)) {
            return null;
        }
        return userId;
    }

    /**
     * header 에 userId 를 담는 메소드
     *
     * @param exchange
     * @param userId
     */
    private static void setAuthorizationHeader(ServerWebExchange exchange, String userId) {
        exchange.getRequest()
                .mutate()
                .header("X-USER-ID", userId)
                .build();
    }

    /**
     * header 에 있는 accessToken 을 통해 userId 를 찾아 api server 에 전달
     *
     * <ol>
     *  <li>토큰 유무 확인
     *  <li>토큰 만료 여부 확인
     *  <li>Redis 에서 UUID 에 따른 userId 확인
     *  <li>header 에 userId 넣어서 전달
     * </ol>
     *
     * @param config
     * @return GatewayFilter
     */
    @Override
    public GatewayFilter apply(Config config) {
        return (((exchange, chain) -> {

            HttpCookie tokenCookie = exchange
                    .getRequest()
                    .getCookies()
                    .getFirst("X-USER-TOKEN");

            // X-USER-TOKEN 이라는 이름의 Cookie 가 존재하는 지 확인
            if (Objects.isNull(tokenCookie)) {
                return handleUnAuthorize(exchange);
            }

            String token = tokenCookie.getValue();

            // 사용 기간이 만료 되었는 지 확인
            boolean isValid = config.jwtUtil.isValidToken(token);

            if (!isValid) {
                return handleUnAuthorize(exchange);
            }

            // uuid 를 parsing
            String uuid = config.jwtUtil.getUUID(token);

            // Redis 에서 UUID 로 userId 조회
            String userId = getUserId(config, uuid);

            if (Objects.isNull(userId)) {
                return handleUnAuthorize(exchange);
            }

            // header 에 X-USER-ID : userId 설정
            setAuthorizationHeader(exchange, userId);

            return chain.filter(exchange);
        }));
    }

    /**
     * 인증 실패 시 401 에러 처리
     *
     * @return 401_UNAUTHORIZED
     */
    private Mono<Void> handleUnAuthorize(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);

        return response.setComplete();
    }

    @RequiredArgsConstructor
    public static class Config {
        private final RedisTemplate<String, Object> redisTemplate;
        private final JwtUtil jwtUtil;
    }

}
