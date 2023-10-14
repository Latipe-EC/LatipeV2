package latipe.product.mapper;

import latipe.product.entity.AttributeCategory;
import latipe.product.entity.Category;
import latipe.product.request.CreateAttributeCategoryRequest;
import latipe.product.request.CreateCategoryRequest;
import latipe.product.request.UpdateAttributeCategoryRequest;
import latipe.product.request.UpdateCategoryRequest;
import latipe.product.response.AttributeCategoryResponse;
import latipe.product.response.CategoryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AttributeCategoryMapper {

  @Mapping(target = "id", ignore = true)
  AttributeCategory mapToAttributeCategoryBeforeCreate(CreateAttributeCategoryRequest user);

  void mapToAttributeCategoryBeforeUpdate(@MappingTarget AttributeCategory category, UpdateAttributeCategoryRequest input);

  AttributeCategoryResponse mapToResponse(AttributeCategory category);

}
