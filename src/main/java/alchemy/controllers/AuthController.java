package alchemy.controllers;

import java.time.ZoneOffset;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import alchemy.api.AuthApi;
import alchemy.model.AccountRequest;
import alchemy.model.RefreshRequest;
import alchemy.model.SecurityToken;
import alchemy.model.TokenResponse;
import alchemy.services.AuthService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController implements AuthApi {

	private AuthService authService;
	
	@PostMapping("/register")
    public ResponseEntity<TokenResponse> register(@RequestBody AccountRequest request) {
		SecurityToken token = authService.register(request);
		
		TokenResponse response = TokenResponse.builder()
				.accessToken(token.getAccessToken().toString())
				.accessExpirationTime(token.getAccessExpirationTime().atOffset(ZoneOffset.UTC))
				.refreshToken(token.getRefreshToken().toString())
				.refreshExpirationTime(token.getRefreshExpirationTime().atOffset(ZoneOffset.UTC))
				.build();
		
        return ResponseEntity.ok(response);
    }
	
	@PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody AccountRequest request) {
		SecurityToken token = authService.authenticate(request);
		
		TokenResponse response = TokenResponse.builder()
				.accessToken(token.getAccessToken().toString())
				.accessExpirationTime(token.getAccessExpirationTime().atOffset(ZoneOffset.UTC))
				.refreshToken(token.getRefreshToken().toString())
				.refreshExpirationTime(token.getRefreshExpirationTime().atOffset(ZoneOffset.UTC))
				.build();
		
        return ResponseEntity.ok(response);
    }
	
	@GetMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestBody RefreshRequest request) {
		SecurityToken token = authService.refresh(request);
		
		TokenResponse response = TokenResponse.builder()
				.accessToken(token.getAccessToken().toString())
				.accessExpirationTime(token.getAccessExpirationTime().atOffset(ZoneOffset.UTC))
				.refreshToken(token.getRefreshToken().toString())
				.refreshExpirationTime(token.getRefreshExpirationTime().atOffset(ZoneOffset.UTC))
				.build();
		
        return ResponseEntity.ok(response);
    }
	
}