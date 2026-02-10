package alchemy.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import alchemy.model.Account;
import alchemy.model.wardrobe.WardrobeItem;

public interface WardrobeRepository extends JpaRepository<WardrobeItem, UUID> {

	boolean existsByName(String name);
	List<WardrobeItem> findByAuthor(Account author);

}