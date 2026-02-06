package alchemy.model.pets.attributes;

import alchemy.model.pets.Pet;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DerivedAttribute implements Attribute {

	TOUGHNESS(BaseAttribute.STRENGTH, BaseAttribute.CONSTITUTION),
	PRECISION(BaseAttribute.STRENGTH, BaseAttribute.AGILITY),
	FOCUS(BaseAttribute.STRENGTH, BaseAttribute.INTELLECT),
	MOMENTUM(BaseAttribute.STRENGTH, BaseAttribute.WIILPOWER),
	DEFENCE(BaseAttribute.CONSTITUTION, BaseAttribute.AGILITY),
	ADAPTATION(BaseAttribute.CONSTITUTION, BaseAttribute.INTELLECT),
	RESOLVE(BaseAttribute.CONSTITUTION, BaseAttribute.WIILPOWER),
	CLARITY(BaseAttribute.AGILITY, BaseAttribute.INTELLECT),
	INSTINCT(BaseAttribute.AGILITY, BaseAttribute.WIILPOWER),
	MASTERY(BaseAttribute.INTELLECT, BaseAttribute.WIILPOWER);

	private BaseAttribute baseAttribute1;
	private BaseAttribute baseAttribute2;

	@Override
	@Transient
	public int getAttributeFromPet(Pet pet) {
		return pet.getAttributeLoadout().getDerivedAttribute(this);
	}

}
