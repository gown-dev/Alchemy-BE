package alchemy.model.pets.constraints;

import alchemy.model.pets.Pet;
import jakarta.persistence.Entity;

@Entity
public abstract class Requirement extends Constraint {

	@Override
	public boolean apply(Pet pet) {
		return false;
	}

}
