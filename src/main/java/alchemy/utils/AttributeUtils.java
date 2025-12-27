package alchemy.utils;

import org.apache.commons.lang3.StringUtils;

import alchemy.model.AttributeDTO;
import alchemy.model.pets.attributes.Attribute;
import alchemy.model.pets.attributes.BaseAttribute;
import alchemy.model.pets.attributes.DerivedAttribute;

public class AttributeUtils {

	public static Attribute findAttributeByValue(String value) {
		if (StringUtils.isBlank(value)) return null;
	    
	    return switch (value) {
	        case "STRENGTH", "CONSTITUTION", "AGILITY", "INTELLECT", "WILLPOWER" -> BaseAttribute.valueOf(value);
	        case "TOUGHNESS", "PRECISION", "FOCUS", "MOMENTUM", "DEFENCE", "ADAPTATION", "RESOLVE", "CLARITY", 
	        	"INSTINCT", "MASTERY" -> DerivedAttribute.valueOf(value);
	        default -> throw new IllegalArgumentException("Attribut inconnu : " + value);
	    };
	}
	
	public static AttributeDTO findAttributeDTOByValue(String value) {
		if (StringUtils.isBlank(value)) return null;
	    
	    return switch (value) {
	        case "STRENGTH", "CONSTITUTION", "AGILITY", "INTELLECT", "WILLPOWER", "TOUGHNESS", 
	        	"PRECISION", "FOCUS", "MOMENTUM", "DEFENCE", "ADAPTATION", "RESOLVE", "CLARITY", 
	        	"INSTINCT", "MASTERY" -> AttributeDTO.fromValue(value);
	        default -> throw new IllegalArgumentException("Attribut inconnu : " + value);
	    };
	}
	
}