package alchemy.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import alchemy.model.pets.moves.Move;

public interface MoveRepository extends JpaRepository<Move, String> {

}
