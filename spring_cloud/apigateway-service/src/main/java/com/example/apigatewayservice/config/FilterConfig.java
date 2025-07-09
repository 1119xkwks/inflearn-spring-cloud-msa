package com.example.apigatewayservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

//@Configuration ==> yml 파일에 실습
@RequiredArgsConstructor
public class FilterConfig {
	private final Environment env;

	@Bean
	public RouteLocator getRouteLocator(RouteLocatorBuilder builder) {
		return builder.routes()
				.route(r -> r.path("/first-service/**")
						.filters(f -> f.addRequestHeader("first-request", "1st-request-header-by-java")
								.addResponseHeader("first-response", "first-response-header-from-java"))
						.uri("http://localhost:8081")
				)
				.route(r -> r.path("/second-service/**")
						.filters(f -> f.addRequestHeader("second-request", "2nd-request-header-by-java")
								.addResponseHeader("second-response", "2nd-response-header-by-java"))
						.uri("http://localhost:8082")
				)
				.build();
	}
}
