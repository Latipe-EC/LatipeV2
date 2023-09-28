package latipe.product.mapper;

import latipe.product.Entity.Product;
import latipe.product.request.CreateProductRequest;
import latipe.product.response.ProductResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {

  @Mapping(target = "id", ignore = true)
  public abstract Product mapToProductBeforeCreate(CreateProductRequest user);


  public abstract ProductResponse mapToProductToResponse(Product product);

}
