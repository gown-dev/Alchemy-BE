package alchemy.model.pets.attributes;

import alchemy.model.pets.Pet;
import jakarta.persistence.Transient;

public interface Attribute {

	@Transient
	public int getAttributeFromPet(Pet pet);

}
