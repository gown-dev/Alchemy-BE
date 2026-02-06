package alchemy.model.pets.attributes;

import alchemy.model.pets.Pet;
import jakarta.persistence.Transient;

public enum BaseAttribute implements Attribute {

	STRENGTH,
	CONSTITUTION,
	AGILITY,
	INTELLECT,
	WIILPOWER;

	@Override
	@Transient
	public int getAttributeFromPet(Pet pet) {
		return pet.getAttributeLoadout().getBaseAttribute(this);
	}

}
