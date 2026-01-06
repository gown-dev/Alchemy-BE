package alchemy.filters;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import alchemy.config.Autoconfiguration;
import alchemy.model.Account;
import alchemy.model.SecurityToken;
import alchemy.repositories.SecurityTokenRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BearerTokenFilter extends OncePerRequestFilter {

	private final SecurityTokenRepository tokenRepository;
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		if (Stream.of(Autoconfiguration.WHITELISTED_PATHS).anyMatch(path -> path.equals(request.getServletPath()))) {
			filterChain.doFilter(request, response);
		}
		
		String authHeader = request.getHeader("Authorization");

        if(authHeader != null && authHeader.startsWith("Bearer ") && !authHeader.substring(7).isBlank()) {
            String accessToken = authHeader.substring(7);
            Account account = isTokenValid(accessToken);

            if(account == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            } else {
                Authentication authenticationToken = new UsernamePasswordAuthenticationToken(account, account.getPassword(), account.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }

        filterChain.doFilter(request, response);
	}
	
	private Account isTokenValid(String token) {
		SecurityToken securityToken = tokenRepository.findByAccessToken(UUID.fromString(token)).orElse(null);
	    
	    if (securityToken == null || securityToken.getAccessExpirationTime().isBefore(LocalDateTime.now())) {
	        return null;
	    }
	    
	    return securityToken.getAccount();
	}

}
