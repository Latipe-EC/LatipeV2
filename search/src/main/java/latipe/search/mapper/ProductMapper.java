package latipe.search.mapper;


import latipe.search.document.Product;
import latipe.search.viewmodel.ProductESDetailVm;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class ProductMapper {

  public abstract Product mapToProduct(ProductESDetailVm input);

}
