package alchemy.mappers;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import alchemy.model.WardrobeItemCategoryDTO;
import alchemy.model.WardrobeItemDTO;
import alchemy.model.wardrobe.WardrobeItem;
import alchemy.model.wardrobe.WardrobeItemCategory;
import alchemy.utils.WardrobeUtils;

@Mapper(componentModel = "spring")
public interface WardrobeMapper {

	@Mapping(source = "name", target = "name")
	@Mapping(source = "price", target = "price")
	@Mapping(source = "category", target = "category")
	@Mapping(target = "frontImage", expression = "java(wardrobeUtils.getPublicImageUrl(item.getFrontImage()))")
	@Mapping(target = "backImage", expression = "java(wardrobeUtils.getPublicImageUrl(item.getBackImage()))")
	WardrobeItemDTO toWardrobeItemDTO(WardrobeItem item, @Context WardrobeUtils wardrobeUtils);
	
	WardrobeItemCategoryDTO toDamageTypeDTO(WardrobeItemCategory source);

}
