package alchemy.config;

import java.util.Arrays;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

import alchemy.exceptions.AuthExceptionHandler;
import alchemy.filters.BearerTokenFilter;
import alchemy.repositories.AccountRepository;
import alchemy.repositories.SecurityTokenRepository;
import lombok.RequiredArgsConstructor;

@AutoConfiguration
@RequiredArgsConstructor
@EnableWebSecurity(debug = false)
@EntityScan(basePackages = "model.entities")
@EnableJpaRepositories(basePackages = "repositories")
@EnableConfigurationProperties(SecurityProperties.class)
public class Autoconfiguration {
    
    @Bean
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
        
        filter.setIncludeClientInfo(true);
        filter.setIncludeQueryString(true);
        filter.setIncludeHeaders(false); 
        filter.setIncludePayload(true);
        filter.setMaxPayloadLength(1000);
        filter.setBeforeMessagePrefix("BEFORE REQUEST: [");
        filter.setAfterMessagePrefix("AFTER REQUEST: [");
        
        return filter;
    }
    
    @Bean
    @ConditionalOnMissingBean(SecurityFilterChain.class)
    public SecurityFilterChain securityFilterChain(HttpSecurity http, BearerTokenFilter bearerTokenFilter) throws Exception {
        return http.authorizeHttpRequests((request) -> {
            request.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
            request.requestMatchers(new String[] { "/auth/register", "/auth/login", "/auth/refresh" }).permitAll();
            request.anyRequest().authenticated();
        })
        .formLogin(login -> login.disable())
        .csrf(AbstractHttpConfigurer::disable)
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .addFilterAfter(bearerTokenFilter, BasicAuthenticationFilter.class)
        .build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList(new String[] { }));
        configuration.setAllowedMethods(Arrays.asList(new String[] { "GET", "POST", "PUT", "DELETE", "OPTIONS" }));
        configuration.setAllowedHeaders(Arrays.asList(new String[] { "*" }));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(Arrays.asList(new String[] { "Authorization" }));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    
    @Bean
    @ConditionalOnMissingBean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public AuthExceptionHandler authExceptionHandler() {
        return new AuthExceptionHandler();
    }
    
    @Bean
    @ConditionalOnProperty(prefix = "sentinel.auth.token", name = "enable-bearer-filter", matchIfMissing = true)
    public BearerTokenFilter bearerTokenFilter(SecurityTokenRepository repository) {
        return new BearerTokenFilter(repository);
    }
    
    @Bean
    @ConditionalOnMissingBean(UserDetailsService.class)
    public UserDetailsService userDetailsService(AccountRepository accountRepository) {
        return username -> accountRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found."));
    }
    
}