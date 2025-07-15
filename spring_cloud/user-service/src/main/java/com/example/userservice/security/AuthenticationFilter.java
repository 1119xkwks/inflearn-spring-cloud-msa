package com.example.userservice.security;

import com.example.userservice.dto.UserDto;
import com.example.userservice.service.UserService;
import com.example.userservice.vo.RequestLogin;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.ArrayList;
import java.util.Date;

@AllArgsConstructor
@Slf4j
public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {
	private final AuthenticationManager authenticationManager;
	private final UserService userService;
	private final Environment env;

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
		try {
			RequestLogin login = new ObjectMapper().readValue(request.getInputStream(), RequestLogin.class);

			return getAuthenticationManager()
					.authenticate(
							new UsernamePasswordAuthenticationToken(login.getEmail(), login.getPassword(), new ArrayList<>())
					);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	// 로그인 성공시
	// token을 response 해더에 담음.
	// JJWT 0.12.6
	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
		String username = ((User)authResult.getPrincipal()).getUsername();
		UserDto userDetails = userService.getUserDetailsByEmail(username);

		// ⚠️ token.secret는 base64로 인코딩된 문자열이어야 함
		// ✅ Base64 디코딩된 시크릿 키를 Key 객체로 변환
		String secret = env.getProperty("token.secret");
		byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
		Key key = Keys.hmacShaKeyFor(secretBytes); // secretBytes 길이는 32바이트 이상 필요

		String token = Jwts.builder()
				.subject(userDetails.getUserId())
				.signWith(key)
				.expiration(new Date(System.currentTimeMillis() + Long.parseLong(env.getProperty("token.expiration_time")))) // ✅ setExpiration → expiration
				.compact();

//		response.addHeader("Authorization", "Bearer " + token)
		response.addHeader("token", token);
		response.addHeader("userId", userDetails.getUserId());
	}
}
