package alchemy.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.SubclassExhaustiveStrategy;
import org.mapstruct.SubclassMapping;

import alchemy.model.DamageComponentDTO;
import alchemy.model.DamageTypeDTO;
import alchemy.model.MoveComponentDTO;
import alchemy.model.MoveDTO;
import alchemy.model.pets.moves.Move;
import alchemy.model.pets.moves.components.DamageComponent;
import alchemy.model.pets.moves.components.DamageType;
import alchemy.model.pets.moves.components.MoveComponent;

@Mapper(componentModel = "spring", uses = { ConstraintMapper.class }, subclassExhaustiveStrategy = SubclassExhaustiveStrategy.RUNTIME_EXCEPTION)
public interface MoveMapper {

	@Mapping(target = "identification.name", source = "name")
	@Mapping(target = "identification.tags", source = "tags")
	@Mapping(target = "constraints", source = "constraints")
	@Mapping(target = "cooldown", source = "cooldown")
	@Mapping(target = "components", source = "components")
	MoveDTO toDTO(Move source);

	@Mapping(target = "name", source = "identification.name")
	@Mapping(target = "tags", source = "identification.tags")
	@Mapping(target = "constraints", source = "constraints")
	@Mapping(target = "cooldown", source = "cooldown")
	@Mapping(target = "components", source = "components")
	Move toEntity(MoveDTO source);

	@Mapping(target = "type", constant = "DAMAGE_COMPONENT")
	@Mapping(target = "baseDamage", source = "baseDamage")
	@Mapping(target = "baseBypass", source = "baseBypass")
	@Mapping(target = "damageType", source = "damageType")
	DamageComponentDTO toDamageComponentDTO(DamageComponent constraint);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "baseDamage", source = "baseDamage")
	@Mapping(target = "baseBypass", source = "baseBypass")
	@Mapping(target = "damageType", source = "damageType")
	DamageComponent toDamageComponentEntity(DamageComponentDTO dto);

	@SubclassMapping(source = DamageComponentDTO.class, target = DamageComponent.class)
	MoveComponent toMoveComponentEntity(MoveComponentDTO dto);

	@SubclassMapping(source = DamageComponent.class, target = DamageComponentDTO.class)
	MoveComponentDTO toMoveComponentDTO(MoveComponent dto);

	DamageTypeDTO toDamageTypeDTO(DamageType source);
	DamageType toDamageTypeEntity(DamageTypeDTO source);

}
