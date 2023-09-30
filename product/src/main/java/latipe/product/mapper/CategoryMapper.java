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

  CategoryResponse mapToCategoryResponse(Category category);

}
