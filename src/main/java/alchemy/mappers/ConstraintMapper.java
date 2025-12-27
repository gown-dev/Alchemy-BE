package alchemy.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.SubclassExhaustiveStrategy;
import org.mapstruct.SubclassMapping;

import alchemy.model.AttributeDTO;
import alchemy.model.AttributeRequirementDTO;
import alchemy.model.AttributeRestrictionDTO;
import alchemy.model.ConstraintDTO;
import alchemy.model.pets.attributes.Attribute;
import alchemy.model.pets.constraints.AttributeRequirement;
import alchemy.model.pets.constraints.AttributeRestriction;
import alchemy.model.pets.constraints.Constraint;
import alchemy.utils.AttributeUtils;

@Mapper(componentModel = "spring", subclassExhaustiveStrategy = SubclassExhaustiveStrategy.RUNTIME_EXCEPTION)
public interface ConstraintMapper {
	
	default Attribute mapAttribute(AttributeDTO attribute) {
	    if (attribute == null) return null;
	    
	    return AttributeUtils.findAttributeByValue(attribute.getValue());
	}
	
	default AttributeDTO toAttributeDTO(Attribute attribute) {
        if (attribute == null) return null;

        return AttributeUtils.findAttributeDTOByValue(attribute.toString());
    }
	
	@Mapping(target = "type", constant = "ATTRIBUTE_REQUIREMENT")
	@Mapping(source = "threshold", target = "threshold")
	AttributeRequirementDTO toAttributeRequirementDTO(AttributeRequirement constraint);
	
	@Mapping(target = "type", constant = "ATTRIBUTE_RESTRICTION")
	@Mapping(source = "threshold", target = "threshold")
	AttributeRestrictionDTO toAttributeRestrictionDTO(AttributeRestriction constraint);

	@Mapping(source = "threshold", target = "threshold")
	AttributeRequirement toAttributeRequirementEntity(AttributeRequirementDTO dto);

	@Mapping(source = "threshold", target = "threshold")
	AttributeRestriction toAttributeRestrictionEntity(AttributeRestrictionDTO dto);

	@SubclassMapping(source = AttributeRequirementDTO.class, target = AttributeRequirement.class)
	@SubclassMapping(source = AttributeRestrictionDTO.class, target = AttributeRestriction.class)
	Constraint toConstraintDTO(ConstraintDTO dto);

	@SubclassMapping(source = AttributeRequirement.class, target = AttributeRequirementDTO.class)
	@SubclassMapping(source = AttributeRestriction.class, target = AttributeRestrictionDTO.class)
	ConstraintDTO toConstraintEntity(Constraint dto);

}
