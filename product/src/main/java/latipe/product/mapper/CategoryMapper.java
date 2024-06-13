package latipe.product.mapper;

import latipe.product.entity.Category;
import latipe.product.request.CreateCategoryRequest;
import latipe.product.request.UpdateCategoryRequest;
import latipe.product.response.CategoryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoryMapper {

    @Mapping(target = "id", ignore = true)
    Category mapToCategoryBeforeCreate(CreateCategoryRequest user);

    void mapToCategoryBeforeUpdate(@MappingTarget Category category, UpdateCategoryRequest input);

    //  @Mappings({
//      @Mapping(target = "id", source = "id"),
//      @Mapping(target = "name", source = "name"),
//      @Mapping(target = "isDeleted", source = "isDeleted"),
//      @Mapping(target = "parentCategoryId", source = "parentCategoryId"),
//      @Mapping(target = "image", source = "image"),
//      @Mapping(target = "attributes", source = "attributes"),
//  })
    CategoryResponse mapToCategoryResponse(Category category);

}
