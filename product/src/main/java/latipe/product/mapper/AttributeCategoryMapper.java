package latipe.product.mapper;

import latipe.product.entity.AttributeCategory;
import latipe.product.request.CreateAttributeCategoryRequest;
import latipe.product.request.UpdateAttributeCategoryRequest;
import latipe.product.response.AttributeCategoryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AttributeCategoryMapper {

    @Mapping(target = "id", ignore = true)
    AttributeCategory mapToAttributeCategoryBeforeCreate(CreateAttributeCategoryRequest user);

    void mapToAttributeCategoryBeforeUpdate(@MappingTarget AttributeCategory category,
        UpdateAttributeCategoryRequest input);

    AttributeCategoryResponse mapToResponse(AttributeCategory category);

}
