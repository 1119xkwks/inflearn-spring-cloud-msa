package com.example.userservice.controller;

import com.example.userservice.dto.UserDto;
import com.example.userservice.jpa.UserEntity;
import com.example.userservice.service.UserService;
import com.example.userservice.vo.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.core.env.Environment;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/")
public class UserController {
	private final Environment env;
	private final Greeting greeting;
	private final UserService userService;
//	private final UserService userService;

	@GetMapping("/health-check")
//	@Timed(value="users.status", longTask = true)
	public String status() {
		return String.format("It's Working in User Service"
				+ ", port(local.server.port)=" + env.getProperty("local.server.port")
				+ ", port(server.port)=" + env.getProperty("server.port")
				+ ", gateway ip(env)=" + env.getProperty("gateway.ip")
//				+ ", gateway ip(value)=" + greeting.getIp()
				+ ", message=" + env.getProperty("greeting.message")
//                + ", token secret=" + env.getProperty("token.secret")
//				+ ", token secret=" + greeting.getSecret()
				+ ", token expiration time=" + env.getProperty("token.expiration_time"));
	}

	@GetMapping("/welcome")
	public String welcome(HttpServletRequest request) {
		log.info("Welcome to User Service, ip: {}, {}, {}, {}"
			, request.getRemoteAddr(), request.getRemoteHost(), request.getRequestURI(), request.getRequestURL()
		);

		return greeting.getGreeting();
	}

	@PostMapping("/users")
	public ResponseEntity<ResponseUser> createUsers(@RequestBody RequestUser user) {
		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

		UserDto userDto = mapper.map(user, UserDto.class);
		userService.createUser(userDto);

		ResponseUser responseUser = mapper.map(userDto, ResponseUser.class);

		return ResponseEntity.status(HttpStatus.CREATED).body(responseUser);
	}

	@GetMapping("/users")
	public ResponseEntity<List<ResponseUser>> getUsers() {
		Iterable<UserEntity> userList = userService.getUserByAll();

		List<ResponseUser> result = new ArrayList<>();
		userList.forEach(v -> {
			result.add(new ModelMapper().map(v, ResponseUser.class));
		});

		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	@GetMapping("/users/{userId}")
	public ResponseEntity getUser(@PathVariable("userId") String userId) {
		UserDto userDto = userService.getUserByUserId(userId);

		ResponseUser returnValue = new ModelMapper().map(userDto, ResponseUser.class);

		EntityModel entityModel = EntityModel.of(returnValue);
		WebMvcLinkBuilder linkTo = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(this.getClass()).getUsers());
		entityModel.add(linkTo.withRel("all-users"));

		try {
			return ResponseEntity.status(HttpStatus.OK).body(entityModel);
		} catch (Exception ex) {
			throw new RuntimeException();
		}
	}

}
