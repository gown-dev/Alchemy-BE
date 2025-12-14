package alchemy.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import alchemy.model.Account;

public interface AccountRepository extends JpaRepository<Account, UUID> {

	Optional<Account> findByUsername(String username);
	boolean existsByUsernameAndPassword(String username, String password);
	boolean existsByUsername(String username);

}
