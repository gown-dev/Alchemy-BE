package alchemy.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import alchemy.model.pets.genes.Gene;

public interface GeneRepository extends JpaRepository<Gene, String> {

	boolean existsByImageIn(List<String> ids);

}
