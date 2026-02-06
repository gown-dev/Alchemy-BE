package alchemy.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import alchemy.model.Account;
import alchemy.model.SecurityToken;

public interface SecurityTokenRepository extends JpaRepository<SecurityToken, UUID> {

	Optional<SecurityToken> findByAccessToken(UUID token);
	Optional<SecurityToken> findByRefreshToken(UUID token);
	Optional<SecurityToken> findByAccount(Account account);

}
