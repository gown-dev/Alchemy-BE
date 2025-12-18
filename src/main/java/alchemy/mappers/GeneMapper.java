package alchemy.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import alchemy.model.Gene;
import alchemy.model.GeneDTO;
import alchemy.model.GeneIdentificationDTO;

@Mapper(componentModel = "spring")
public interface GeneMapper {

	@Mapping(source = "id", target = "image")
	GeneIdentificationDTO toGeneIdentificationDTO(Gene gene);
	
	@Mapping(source = "tags", target = "tags")
	GeneDTO toGeneDTO(Gene gene);
	
	@Mapping(source = "identification.image", target = "id")
	@Mapping(source = "tags", target = "tags")
	Gene toGeneEntity(GeneDTO gene);
	
	@Mapping(source = "image", target = "id")
	Gene toGeneEntity(GeneIdentificationDTO gene);
	
}
