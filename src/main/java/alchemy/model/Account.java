package alchemy.model;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@SuppressWarnings("serial")
@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(name = "Account")
public class Account implements UserDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid")
	private UUID id;

	@Column(name = "username", nullable = false)
	private String username;

	@Column(name = "tag", nullable = true)
	private String tag;

	@Column(name = "password", nullable = false)
	private String password;

	@Builder.Default
	private boolean active = true;

	@Builder.Default
	private boolean expired = false;

	@Builder.Default
	private boolean locked = false;

	@Builder.Default
	private boolean expiredCredentials = false;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(
	    name = "Role",
	    joinColumns = @JoinColumn(name = "account_id")
	)
	@Column(name = "role")
	private List<String> authorities;

    @Override
    public boolean isAccountNonExpired() {
        return !expired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !locked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return !expiredCredentials;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }

    @Override
    public List<? extends GrantedAuthority> getAuthorities() {
        return authorities.stream()
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());
    }

    public boolean isUsable() {
    	return isEnabled() && isAccountNonExpired() && isAccountNonLocked() && isCredentialsNonExpired();
    }
    
    public boolean isAdmin() {
    	return authorities.contains("ADMIN");
    }

}
