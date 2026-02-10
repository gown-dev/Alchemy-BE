package alchemy.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import alchemy.model.GeneDTO;
import alchemy.model.GeneIdentificationDTO;
import alchemy.model.GeneTypeDTO;
import alchemy.model.pets.genes.Gene;
import alchemy.model.pets.genes.GeneType;

@Mapper(componentModel = "spring", uses = { ConstraintMapper.class })
public interface GeneMapper {

	GeneTypeDTO toTypeDTO(GeneType type);

	GeneType toTypeEntity(GeneTypeDTO type);

	@Mapping(source = "image", target = "image")
	@Mapping(source = "name", target = "name")
	GeneIdentificationDTO toGeneIdentificationDTO(Gene gene);

	@Mapping(source = "image", target = "identification.image")
	@Mapping(source = "name", target = "identification.name")
	@Mapping(source = "type", target = "type")
	@Mapping(source = "tags", target = "tags")
	GeneDTO toGeneDTO(Gene gene);

	@Mapping(source = "identification.image", target = "image")
	@Mapping(source = "identification.name", target = "name")
	@Mapping(source = "type", target = "type")
	@Mapping(source = "tags", target = "tags")
	@Mapping(source = "constraints", target = "constraints")
	Gene toGeneEntity(GeneDTO gene);

	@Mapping(source = "image", target = "image")
	@Mapping(source = "name", target = "name")
	@Mapping(target = "type", ignore = true)
	@Mapping(target = "tags", ignore = true)
	@Mapping(target = "constraints", ignore = true)
	Gene toGeneEntity(GeneIdentificationDTO gene);

}
