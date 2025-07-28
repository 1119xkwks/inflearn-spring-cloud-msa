package com.example.userservice.client;

import feign.RequestInterceptor;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignTracingConfig {

	@Bean
	public RequestInterceptor traceRequestInterceptor(Tracer tracer, Propagator propagator) {
		return template -> {
			Propagator.Setter<feign.RequestTemplate> setter =
					(carrier, key, value) -> carrier.header(key, value);

			propagator.inject(tracer.currentTraceContext().context(), template, setter);
		};
	}
}
