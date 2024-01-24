package latipe.search.services;


import latipe.search.constants.MessageCode;
import latipe.search.document.Product;
import latipe.search.exceptions.NotFoundException;
import latipe.search.feign.ProductClient;
import latipe.search.mapper.ProductMapper;
import latipe.search.repositories.ProductRepository;
import latipe.search.viewmodel.ProductESDetailVm;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ProductSyncDataService {

  private final ProductRepository productRepository;
  private final ProductMapper productMapper;
  private final ProductClient productClient;

  public ProductESDetailVm getProductESDetailById(String id) {
    return productClient.getProductESDetailById(id);
  }

  public void updateProduct(String id) {
    ProductESDetailVm productESDetailVm = getProductESDetailById(id);
    Product product = productRepository.findById(id)
        .orElseThrow(() -> new NotFoundException(MessageCode.PRODUCT_NOT_FOUND, id));

    product.setName(productESDetailVm.name());
    product.setSlug(productESDetailVm.slug());
    product.setPrice(productESDetailVm.price());
    product.setIsPublished(productESDetailVm.isPublished());
    product.setCategories(productESDetailVm.categories());
    product.setClassifications(productESDetailVm.classifications());
    product.setProductClassifications(productESDetailVm.productClassifications());
    product.setImages(productESDetailVm.images());
    product.setCreatedDate(productESDetailVm.createdDate());
    product.setDescription(productESDetailVm.description());
    product.setBanned(productESDetailVm.isBanned());
    product.setDeleted(productESDetailVm.isDeleted());
    product.setCountSale(productESDetailVm.countSale());
    product.setRatings(productESDetailVm.ratings());

    productRepository.save(product);
  }

  public void createProduct(String id) {
    var productESDetailVm = getProductESDetailById(id);
    var product = productMapper.mapToProduct(productESDetailVm);
    productRepository.save(product);
  }

  public void deleteProduct(String id) {
    final boolean isProductExisted = productRepository.existsById(id);
    if (!isProductExisted) {
      throw new NotFoundException(MessageCode.PRODUCT_NOT_FOUND, id);
    }

    productRepository.deleteById(id);
  }

  public void banProduct(String id, Boolean isBanned) {
    var productExisted = productRepository.findById(id)
        .orElseThrow(() -> new NotFoundException(MessageCode.PRODUCT_NOT_FOUND, id));
    productExisted.setBanned(isBanned);
    productRepository.save(productExisted);
  }


}
