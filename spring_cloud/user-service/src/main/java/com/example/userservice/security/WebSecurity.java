package com.example.userservice.security;

import com.example.userservice.service.UserService;
import jakarta.servlet.Filter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.server.header.XFrameOptionsServerHttpHeadersWriter;
import org.springframework.security.web.util.matcher.IpAddressMatcher;

import java.net.InetSocketAddress;
import java.util.function.Supplier;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
@Slf4j
public class WebSecurity /*  Spring Security 6.1 이상에서는 상속 받지 않음  */ {
    private final UserService userService;
    private final Environment env;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        /*
        http.csrf(AbstractHttpConfigurer::disable)
            .headers(headers -> headers
                    .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
            )
            .authorizeHttpRequests(auth -> auth
                    // 특정 경로 허용
                    .requestMatchers("/h2-console/**").permitAll()
                    // 그 외에는 인증 필요
                    .anyRequest().authenticated()
            )
            // BASIC 인증 추가
            .httpBasic(Customizer.withDefaults());
        */

        http.csrf(AbstractHttpConfigurer::disable);

        http.authorizeHttpRequests(
                authorize -> authorize
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/**")
                        .access((authSupllier, context) -> {
                            String remoteAddress = context.getRequest().getRemoteAddr();
                            String remoteHost = context.getRequest().getRemoteHost();

                            log.info("remote address: {}", remoteAddress);
                            log.info("remote host: {}", remoteHost);

//                            return new AuthorizationDecision(
//                                    new IpAddressMatcher("127.0.0.1").matches(context.getRequest())
//                                            ||  new IpAddressMatcher("::1").matches(context.getRequest())
//                            );

                            // 쿠버네티스 pod로 실행
                            return new AuthorizationDecision(true);
                        })
        );

        http.addFilterBefore(
                authenticationFilter(
                        authenticationManager( http.getSharedObject( AuthenticationConfiguration.class ) )
                ),
                UsernamePasswordAuthenticationFilter.class);

        http.headers(AbstractHttpConfigurer::disable);

        return http.build();
    }

    /**
     * 사용자 인증 매니저 설정
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * 커스텀 인증 필터 등록
     */
    private AuthenticationFilter authenticationFilter(AuthenticationManager authenticationManager) {
        AuthenticationFilter filter = new AuthenticationFilter(authenticationManager, userService, env);
        filter.setAuthenticationManager(authenticationManager);
        filter.setFilterProcessesUrl("/login"); // 필요에 따라 로그인 경로 설정
        return filter;
    }

//    @Bean
//    public UserDetailsService userDetailsService() {
//        UserDetails userDetails = User.withDefaultPasswordEncoder()
//                .username("user")
//                .password("password")
//                .roles("USER")
//                .build();
//
//        return new InMemoryUserDetailsManager(userDetails);
//    }
}
