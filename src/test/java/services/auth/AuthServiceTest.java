package services.auth;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.function.ThrowingSupplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import alchemy.config.AuthProperties;
import alchemy.config.Autoconfiguration;
import alchemy.exceptions.ProcessException;
import alchemy.exceptions.process.auth.AuthProcessError;
import alchemy.model.Account;
import alchemy.model.AccountRequestDTO;
import alchemy.model.RefreshRequestDTO;
import alchemy.model.SecurityToken;
import alchemy.repositories.AccountRepository;
import alchemy.repositories.SecurityTokenRepository;
import alchemy.services.auth.AuthService;
import config.AbstractTest;

@DataJpaTest(showSql = false)
@ActiveProfiles("test")
@ContextConfiguration(classes = Autoconfiguration.class)
public class AuthServiceTest extends AbstractTest {
    
	@Autowired
	private AuthProperties authProperties;
	
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private SecurityTokenRepository tokenRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private AuthService authService;
    
    private Account createAccount(String username, String password) {
    	Account account = Account.builder()
			.username(username)
			.password(passwordEncoder.encode(password))
			.active(true)
			.expired(false)
			.locked(false)
			.expiredCredentials(false)
			.authorities(List.of("ADMIN", "USER"))
			.build();
    	
    	return entityManager.persistAndFlush(account);
    }

    private SecurityToken createToken(Account account, UUID accessToken, UUID refreshToken) {    	
    	SecurityToken token = SecurityToken.builder()
    		.account(account)
    		.accessToken(accessToken)
    		.accessExpirationTime(LocalDateTime.now().plusDays(1))
    		.refreshToken(refreshToken)
    		.refreshExpirationTime(LocalDateTime.now().plusDays(3))
			.build();
    	
    	return entityManager.persistAndFlush(token);
    }
    
    private void assertValidToken(ThrowingSupplier<SecurityToken> tokenSupplier) {
    	SecurityToken token = assertDoesNotThrow(tokenSupplier);
    	assertNotNull(token.getAccessToken());
    	assertTrue(token.getAccessExpirationTime().isAfter(LocalDateTime.now()));
    	assertNotNull(token.getRefreshToken());
    	assertTrue(token.getRefreshExpirationTime().isAfter(LocalDateTime.now()));
    }
    
    private void assertAuthException(AuthProcessError expectedError, Executable call) {
    	ProcessException exception = assertThrows(ProcessException.class, call);
    	assertTrue(expectedError.code.equals(exception.getError().getCode()));
    	assertTrue(expectedError.description.equals(exception.getError().getDescription()));
    	assertTrue(expectedError.message.equals(exception.getError().getMessage()));
    }
    
    @BeforeEach
    void setUp() {
    	tokenRepository.deleteAll();
    	accountRepository.deleteAll();
    }
    
    @AfterEach
    void cleanUp() {
    	tokenRepository.deleteAll();
    	accountRepository.deleteAll();
    }
    
    @Test
    void authenticateSuccessTest() {
    	String username = "Gown";
    	String password = "p455w0rd";
    	
    	createAccount(username, password);
    	
    	assertTrue(accountRepository.count() == 1);
    	
    	AccountRequestDTO request = AccountRequestDTO.builder()
    		.username(username)
    		.password(password)
    		.build();
    	
    	assertValidToken(() -> authService.authenticate(request));
    }
    
    @Test
    void authenticateFailureUsernameTest() {
    	String username = "Gown";
    	String password = "p455w0rd";
    	
    	createAccount(username, password);
    	
    	AccountRequestDTO request = AccountRequestDTO.builder()
    		.username("Nwog")
    		.password(password)
    		.build();
    	    
    	assertAuthException(AuthProcessError.INVALID_CREDENTIALS, () -> authService.authenticate(request));
    }
    
    @Test
    void registerSuccessTest() {
    	String username = "Gown";
    	String password = "p455w0rd";
    	
    	AccountRequestDTO request = AccountRequestDTO.builder()
        		.username(username)
        		.password(password)
        		.build();
    	
    	assertValidToken(() -> authService.register(request));
    }
    
    @Test
    void registerFailureUsernameMissingTest() {
    	String username = "Gown";
    	String password = "p455w0rd";
    	
    	createAccount(username, password);
    	
    	AccountRequestDTO request = AccountRequestDTO.builder()
        		.password("5tr0ng_p455w0rd")
        		.build();
    	
    	assertAuthException(AuthProcessError.MISSING_USERNAME, () -> authService.register(request));
    }
    
    @Test
    void registerFailurePasswordMissingTest() {
    	String username = "Gown";
    	
    	AccountRequestDTO request = AccountRequestDTO.builder()
    			.username(username)
        		.build();
    	
    	assertAuthException(AuthProcessError.MISSING_PASSWORD, () -> authService.register(request));
    }
    
    @Test
    void registerFailureUsernameTaken() {
    	String username = "Gown";
    	String password = "p455w0rd";
    	
    	createAccount(username, password);
    	
    	AccountRequestDTO request = AccountRequestDTO.builder()
    			.username(username)
        		.password("5tr0ng_p455w0rd")
        		.build();
    	
    	assertAuthException(AuthProcessError.USERNAME_TAKEN, () -> authService.register(request));
    }
    
    @Test
    @DirtiesContext
    void registerFailureUsernameUnsuitableTest() {
    	authProperties.setUsernameRestriction("^[A-Z]+$");
    	
    	String username = "Gown";
    	String password = "p455w0rd";
    	
    	AccountRequestDTO request = AccountRequestDTO.builder()
    			.username(username)
        		.password(password)
        		.build();
    	
    	assertAuthException(AuthProcessError.USERNAME_UNSUITABLE, () -> authService.register(request));
    }
    
    @Test
    @DirtiesContext
    void registerSuccessUsernameSuitableTest() {
    	authProperties.setUsernameRestriction("^[A-Z]+$");
    	
    	String username = "GOWN";
    	String password = "p455w0rd";
    	
    	AccountRequestDTO request = AccountRequestDTO.builder()
    			.username(username)
        		.password(password)
        		.build();
    	
    	assertValidToken(() -> authService.register(request));
    }
    
    @Test
    @DirtiesContext
    void registerFailurePasswordUnsuitableTest() {
    	/* Minimum eight characters, at least one uppercase letter, one lowercase letter and one number. */
    	authProperties.setPasswordRestriction("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,}$");
    	
    	String username = "Gown";
    	String password = "password";
    	
    	AccountRequestDTO request = AccountRequestDTO.builder()
    			.username(username)
        		.password(password)
        		.build();
    	
    	assertAuthException(AuthProcessError.PASSWORD_UNSUITABLE, () -> authService.register(request));
    }
    
    @Test
    @DirtiesContext
    void registerSuccessPasswordSuitableTest() {
    	/* Minimum eight characters, at least one uppercase letter, one lowercase letter and one number. */
    	authProperties.setPasswordRestriction("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,}$");
    	
    	String username = "GOWN";
    	String password = "P455w0rd";
    	
    	AccountRequestDTO request = AccountRequestDTO.builder()
    			.username(username)
        		.password(password)
        		.build();
    	
    	assertValidToken(() -> authService.register(request));
    }
    
    @Test
    void refreshSuccessTest() {
    	String username = "Gown";
    	String password = "p455w0rd";
    	
    	Account account = createAccount(username, password);
    	createToken(account, 
    			UUID.fromString("00000000-0000-0000-0000-000000000000"),
    			UUID.fromString("11111111-1111-1111-1111-111111111111"));
    	
    	assertTrue(accountRepository.count() == 1);
    	
    	RefreshRequestDTO request = RefreshRequestDTO.builder()
    		.refreshToken("11111111-1111-1111-1111-111111111111")
    		.build();
    	
    	assertValidToken(() -> authService.refresh(request));
    }
    
    @Test
    void refreshFailureTest() {
    	String username = "Gown";
    	String password = "p455w0rd";
    	
    	Account account = createAccount(username, password);
    	createToken(account, 
    			UUID.fromString("00000000-0000-0000-0000-000000000000"),
    			UUID.fromString("11111111-1111-1111-1111-111111111111"));
    	
    	assertTrue(accountRepository.count() == 1);
    	
    	RefreshRequestDTO request = RefreshRequestDTO.builder()
    		.refreshToken("22222222-2222-2222-2222-222222222222")
    		.build();
    	
    	assertAuthException(AuthProcessError.MISSING_OR_INVALID_REFRESH_TOKEN, () -> authService.refresh(request));
    }
	
}
