package com.example.apigatewayservice.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class AuthorizationHeaderFilter extends AbstractGatewayFilterFactory<AuthorizationHeaderFilter.Config> {
	private final Environment env;

	public AuthorizationHeaderFilter(Environment env) {
		super(Config.class);
		this.env = env;
	}

	public static class Config {}

	@Override
	public GatewayFilter apply(Config config) {
		return (exchange, chain) -> {
			ServerHttpRequest request = exchange.getRequest();

			if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
				return onError(exchange, "No Authorization Header.", HttpStatus.UNAUTHORIZED);
			}

			String authorizationHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
			String jwt = authorizationHeader.replace("Bearer ", "");

			if (!isJwtValid(jwt)) {

				return onError(exchange, "JWT token is not valid.", HttpStatus.UNAUTHORIZED);
			}

			return chain.filter(exchange);
		};
	}

	private boolean isJwtValid(String jwt) {
		boolean returnValue = false;

		String subject = null;
		//


		try {
			String secret = env.getProperty("token.secret");
			SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

			Claims claims = Jwts.parser()
					.verifyWith(key) // 서명 검증 키 설정
					.build()
					.parseSignedClaims(jwt)
					.getPayload();

			subject = claims.getSubject();

			returnValue = null != subject && subject.length() > 0;
		} catch (Exception ex) {
			returnValue = false;
		}

		return returnValue;
	}

	// Mono, Flux -> Spring WebFlux
	private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
		ServerHttpResponse response = exchange.getResponse();
		response.setStatusCode(httpStatus);

		log.error("Authorization onError message: {}", err);
		return response.setComplete();
	}
}
