package latipe.product.mapper;

import latipe.product.entity.Product;
import latipe.product.request.CreateProductRequest;
import latipe.product.response.ProductResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {

  @Mappings({
      @Mapping(target = "id", ignore = true),
      @Mapping(target = "product.productVariants.id", ignore = true),
      @Mapping(target = "product.productClassifications.id", ignore = true),
      @Mapping(target = "categories", source = "product.categories"),
      @Mapping(target = "storeId", source = "storeId"),
  })
  public abstract Product mapToProductBeforeCreate(CreateProductRequest product, String storeId);

  @Mappings({
      @Mapping(target = "productVariants", source = "product.productVariants"),
      @Mapping(target = "productClassifications", source = "product.productClassifications"),

  })
  public abstract ProductResponse mapToProductToResponse(Product product);

}
