package alchemy.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import alchemy.model.technical.SupervisionAlert;

public interface SupervisionRepository extends JpaRepository<SupervisionAlert, UUID> {
	
}
