package alchemy.controllers;

import java.time.ZoneOffset;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import alchemy.api.AuthApi;
import alchemy.model.AccountRequestDTO;
import alchemy.model.RefreshRequestDTO;
import alchemy.model.SecurityToken;
import alchemy.model.TokenResponseDTO;
import alchemy.services.auth.AuthService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController implements AuthApi {

	private AuthService authService;
	
	@PostMapping("/register")
    public ResponseEntity<TokenResponseDTO> register(@RequestBody AccountRequestDTO request) {
		SecurityToken token = authService.register(request);
		
		TokenResponseDTO response = TokenResponseDTO.builder()
				.accessToken(token.getAccessToken())
				.accessExpirationTime(token.getAccessExpirationTime().atOffset(ZoneOffset.UTC))
				.refreshToken(token.getRefreshToken())
				.refreshExpirationTime(token.getRefreshExpirationTime().atOffset(ZoneOffset.UTC))
				.build();
		
        return ResponseEntity.ok(response);
    }
	
	@PostMapping("/login")
    public ResponseEntity<TokenResponseDTO> login(@RequestBody AccountRequestDTO request) {
		SecurityToken token = authService.authenticate(request);
		
		TokenResponseDTO response = TokenResponseDTO.builder()
				.accessToken(token.getAccessToken())
				.accessExpirationTime(token.getAccessExpirationTime().atOffset(ZoneOffset.UTC))
				.refreshToken(token.getRefreshToken())
				.refreshExpirationTime(token.getRefreshExpirationTime().atOffset(ZoneOffset.UTC))
				.build();
		
        return ResponseEntity.ok(response);
    }
	
	@GetMapping("/refresh")
    public ResponseEntity<TokenResponseDTO> refresh(@RequestBody RefreshRequestDTO request) {
		SecurityToken token = authService.refresh(request);
		
		TokenResponseDTO response = TokenResponseDTO.builder()
				.accessToken(token.getAccessToken())
				.accessExpirationTime(token.getAccessExpirationTime().atOffset(ZoneOffset.UTC))
				.refreshToken(token.getRefreshToken())
				.refreshExpirationTime(token.getRefreshExpirationTime().atOffset(ZoneOffset.UTC))
				.build();
		
        return ResponseEntity.ok(response);
    }
	
}