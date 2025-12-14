package alchemy.controllers;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import alchemy.model.AccountRequest;
import alchemy.model.RefreshRequest;
import alchemy.model.SecurityToken;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {

	@PostMapping("/register")
    public ResponseEntity<SecurityToken> register(@RequestBody AccountRequest request) {
		SecurityToken response = null;
        return ResponseEntity.ok(response);
    }
	
	@PostMapping("/login")
    public ResponseEntity<SecurityToken> login(@RequestBody AccountRequest request) {
		SecurityToken response = null;
        return ResponseEntity.ok(response);
    }
	
	@GetMapping("/refresh")
    public ResponseEntity<SecurityToken> refresh(@RequestBody RefreshRequest request) {
		SecurityToken response = null;
        return ResponseEntity.ok(response);
    }
	
}