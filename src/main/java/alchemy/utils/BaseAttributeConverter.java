package alchemy.utils;

import alchemy.model.pets.attributes.Attribute;
import alchemy.model.pets.attributes.BaseAttribute;
import alchemy.model.pets.attributes.DerivedAttribute;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class BaseAttributeConverter implements AttributeConverter<Attribute, String> {

	@Override
	public String convertToDatabaseColumn(Attribute attribute) {
		if (attribute == null) {
			return null;
		}

        return ((Enum<?>) attribute).name();
	}

	@Override
	public Attribute convertToEntityAttribute(String dbData) {
		if (dbData == null) {
			return null;
		}

        try {
            return BaseAttribute.valueOf(dbData);
        } catch (IllegalArgumentException e) {
            return DerivedAttribute.valueOf(dbData);
        }
	}

}
