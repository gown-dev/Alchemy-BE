package alchemy.model.pets.constraints;

import alchemy.model.pets.Pet;
import alchemy.model.pets.attributes.Attribute;
import alchemy.utils.BaseAttributeConverter;
import jakarta.persistence.Convert;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@Entity
@DiscriminatorValue("ATTRIBUTE_RESTRICTION")
@AllArgsConstructor
@NoArgsConstructor
public class AttributeRestriction extends Restriction {

	@Convert(converter = BaseAttributeConverter.class)
	private Attribute attribute;

	private int threshold;

	@Override
	public boolean apply(Pet pet) {
		return attribute.getAttributeFromPet(pet) < threshold;
	}

	@Override
	public String getSignature() {
		return "<_" + attribute.toString() + "_" + threshold;
	}

}
