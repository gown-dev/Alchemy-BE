package alchemy.filters;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import alchemy.config.Autoconfiguration;
import alchemy.model.Account;
import alchemy.services.auth.AuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BearerTokenFilter extends OncePerRequestFilter {

	private final AuthService authService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		if (Stream.of(Autoconfiguration.WHITELISTED_PATHS).anyMatch(path -> path.equals(request.getServletPath()))) {
			filterChain.doFilter(request, response);
			return;
		}

		String authHeader = request.getHeader("Authorization");

        if(authHeader != null && authHeader.startsWith("Bearer ") && !authHeader.substring(7).isBlank()) {
            String accessToken = authHeader.substring(7);

            isTokenValid(accessToken).ifPresentOrElse((account) -> {
                Authentication authenticationToken = new UsernamePasswordAuthenticationToken(account, account.getPassword(), account.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }, () -> {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            });
        }

        filterChain.doFilter(request, response);
	}

	private Optional<Account> isTokenValid(String token) {
		return authService.authenticateToken(token);
	}

}
