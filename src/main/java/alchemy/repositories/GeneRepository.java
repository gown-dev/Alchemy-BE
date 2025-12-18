package alchemy.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import alchemy.model.Gene;

public interface GeneRepository extends JpaRepository<Gene, String> {
	
	boolean existsByIdIn(List<String> images);
	
}
