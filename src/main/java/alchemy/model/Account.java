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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuppressWarnings("serial")
@Entity 
@Table(name = "Account")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Account implements UserDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid")
	protected UUID id;
	
	@Column(name = "username", nullable = false)
    protected String username;
	
	@Column(name = "tag", nullable = true)
	protected String tag;
	
	@Column(name = "password", nullable = false)
    protected String password;
	
	@Builder.Default
    protected boolean active = true;
	
	@Builder.Default
    protected boolean expired = false;
	
	@Builder.Default
    protected boolean locked = false;
	
	@Builder.Default
    protected boolean expiredCredentials = false;
	
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(
	    name = "Role",
	    joinColumns = @JoinColumn(name = "account_id")
	)
	@Column(name = "role")
	protected List<String> authorities;

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
	
}
