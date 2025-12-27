package alchemy.services.auth;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import alchemy.annotations.Logged;
import alchemy.config.AuthProperties;
import alchemy.exceptions.ProcessException;
import alchemy.exceptions.process.auth.AuthProcessError;
import alchemy.model.Account;
import alchemy.model.AccountRequestDTO;
import alchemy.model.RefreshRequestDTO;
import alchemy.model.SecurityToken;
import alchemy.repositories.AccountRepository;
import alchemy.repositories.SecurityTokenRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
	
	private final AuthProperties authProperties;

	private final AccountRepository accountRepository;
	private final SecurityTokenRepository tokenRepository;
	private final PasswordEncoder passwordEncoder;
	
	@Transactional
	@Logged("Authentication")
	public SecurityToken authenticate(AccountRequestDTO request) throws ProcessException {
		verifyLoginRequest(request);
		Account account = findAccount(request);
		
		if (!passwordEncoder.matches(request.getPassword(), account.getPassword())) {
			throw new ProcessException(AuthProcessError.INVALID_CREDENTIALS, HttpStatus.FORBIDDEN);
		}
		
		Optional<SecurityToken> token = tokenRepository.findByAccount(account);
		SecurityToken result;

		if (token.isEmpty()) {
			result = generateNewToken(account);
		} else {
			SecurityToken oldToken = token.get();
			if (oldToken.getAccessExpirationTime().isBefore(LocalDateTime.now())) {
				destroyToken(oldToken);
				SecurityToken newToken = generateNewToken(account);
				result = newToken;
			} else {
				result = oldToken;
			}
		}
		
		return result;
	}
	
	@Transactional
	@Logged("Registration")
	public SecurityToken register(AccountRequestDTO request) throws ProcessException {
		if (StringUtils.isBlank(request.getUsername())) {
			throw new ProcessException(AuthProcessError.MISSING_USERNAME, HttpStatus.BAD_REQUEST);
		}

		if (StringUtils.isBlank(request.getPassword())) {
			throw new ProcessException(AuthProcessError.MISSING_PASSWORD, HttpStatus.BAD_REQUEST);
		}
		
		if (!StringUtils.isBlank(authProperties.getUsernameRestriction())) {
			if (!Pattern.matches(authProperties.getUsernameRestriction(), request.getUsername())) {
				throw new ProcessException(AuthProcessError.USERNAME_UNSUITABLE, HttpStatus.BAD_REQUEST);
			}
		}
		
		if (!StringUtils.isBlank(authProperties.getPasswordRestriction())) {
			if (!Pattern.matches(authProperties.getPasswordRestriction(), request.getPassword())) {
				throw new ProcessException(AuthProcessError.PASSWORD_UNSUITABLE, HttpStatus.BAD_REQUEST);
			}
		}
		
		Optional<AuthProcessError> accountAlreadyExists = checkAccount(request);
				
		if (accountAlreadyExists.isPresent()) {
			throw new ProcessException(accountAlreadyExists.get(), HttpStatus.FORBIDDEN);
		}
		
		final Account account = registerAccount(request);
		final SecurityToken token = generateNewToken(account);
		
		return token;
	}
	
	@Transactional
	@Logged("Refresh")
	public SecurityToken refresh(RefreshRequestDTO request) throws ProcessException {
		if (request.getRefreshToken() == null) {
			throw new ProcessException(AuthProcessError.MISSING_OR_INVALID_REFRESH_TOKEN, HttpStatus.FORBIDDEN);
		}
		
		Optional<SecurityToken> token = tokenRepository.findByRefreshToken(UUID.fromString(request.getRefreshToken()));
		
		SecurityToken result = token.orElseThrow(() -> {
			throw new ProcessException(AuthProcessError.MISSING_OR_INVALID_REFRESH_TOKEN, HttpStatus.FORBIDDEN);
		});
		
		if (result.getRefreshExpirationTime().isBefore(LocalDateTime.now())) {
			throw new ProcessException(AuthProcessError.MISSING_OR_INVALID_REFRESH_TOKEN, HttpStatus.FORBIDDEN);
		}
		
		result = generateAccessToken(result);
		
		return result;
	}
	
	public Account getAuthenticatedAccount() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		
		if (authentication instanceof AnonymousAuthenticationToken) {
			throw new ProcessException(AuthProcessError.UNAUTHENTICATED, HttpStatus.FORBIDDEN);
		}

		try {
			return (Account) authentication.getPrincipal();
		} catch (ClassCastException exception) {
			throw new ProcessException(AuthProcessError.MALFORMED_AUTH, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	private Optional<AuthProcessError> checkAccount(AccountRequestDTO request) {
		if (accountRepository.existsByUsername(request.getUsername())) {
			return Optional.of(AuthProcessError.USERNAME_TAKEN);
		} else {
			return Optional.empty();
		}
	}
	
	private Account registerAccount(AccountRequestDTO request) {
		Account account = createAccount(request);
		accountRepository.save(account);
		
		return account;
	}
	
	private Account createAccount(AccountRequestDTO request) {
		return Account.builder()
			.username(request.getUsername())
			.password(passwordEncoder.encode(request.getPassword()))
			.authorities(Arrays.asList(authProperties.getDefaultRoles())
				.stream()
				.collect(Collectors.toList()))
			.build();
	}
	
	private void verifyLoginRequest(AccountRequestDTO request) {
		if (StringUtils.isBlank(request.getUsername())) {
			throw new ProcessException(AuthProcessError.MISSING_USERNAME, HttpStatus.BAD_REQUEST);
		}
	
		if (StringUtils.isBlank(request.getPassword())) {
			throw new ProcessException(AuthProcessError.MISSING_PASSWORD, HttpStatus.BAD_REQUEST);
		}
	}
	
	private Account findAccount(AccountRequestDTO request) {
		return accountRepository.findByUsername(request.getUsername()).orElseThrow(() -> {
			return new ProcessException(AuthProcessError.INVALID_CREDENTIALS, HttpStatus.FORBIDDEN);
		});
	}
	
	private SecurityToken createToken(Account account) {
		return SecurityToken.builder()
				.account(account)
				.build();
	}
	
	private SecurityToken generateNewToken(Account account) {
		SecurityToken token = createToken(account);
		token = generateAccessToken(token);
		token = generateRefreshToken(token);
		
		tokenRepository.save(token);
		
		return token;
	}
	
	private SecurityToken generateAccessToken(SecurityToken token) {
		token.setAccessToken(UUID.randomUUID());
		token.setAccessExpirationTime(LocalDateTime.now().plusHours(1));
		return token;
	}
	
	private SecurityToken generateRefreshToken(SecurityToken token) {
		token.setRefreshToken(UUID.randomUUID());
		token.setRefreshExpirationTime(LocalDateTime.now().plusHours(72));
		return token;
	}
	
	private void destroyToken(SecurityToken token) {
		tokenRepository.delete(token);
	}
	
}
