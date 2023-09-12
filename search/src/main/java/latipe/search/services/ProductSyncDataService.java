package latipe.search.services;


import latipe.search.constants.MessageCode;
import latipe.search.controllers.APIClient;
import latipe.search.document.Product;
import latipe.search.exceptions.NotFoundException;
import latipe.search.repositories.ProductRepository;
import latipe.search.viewmodel.ProductESDetailVm;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Service
public class ProductSyncDataService {
    private final APIClient apiClient;
    private final ProductRepository productRepository;

    public ProductSyncDataService(APIClient apiClient, ProductRepository productRepository) {
        this.apiClient = apiClient;
        this.productRepository = productRepository;
    }

    public ProductESDetailVm getProductESDetailById(String id) {
        return apiClient.getProductESDetailById(id);
    }

    public void updateProduct(String id) {
        ProductESDetailVm productESDetailVm = getProductESDetailById(id);
        Product product = productRepository.findById(id).orElseThrow(()
                -> new NotFoundException(MessageCode.PRODUCT_NOT_FOUND, id));

        product.setName(productESDetailVm.name());
        product.setSlug(productESDetailVm.slug());
        product.setPrice(productESDetailVm.price());
        product.setIsPublished(productESDetailVm.isPublished());
        product.setCategories(productESDetailVm.categories());
        product.setClassifications(productESDetailVm.classifications());
        product.setProductClassifications(productESDetailVm.productClassifications());
        product.setImages(productESDetailVm.images());
        product.setCreatedDate(productESDetailVm.createdOn());
        product.setDescription(productESDetailVm.description());
        product.setBanned(productESDetailVm.isBanned());
        product.setDeleted(productESDetailVm.isDeleted());

        productRepository.save(product);
    }

    public void createProduct(String id) {
        ProductESDetailVm productESDetailVm = getProductESDetailById(id);

        Product product = Product.builder()
                .id(id)
                .name(productESDetailVm.name())
                .slug(productESDetailVm.slug())
                .price(productESDetailVm.price())
                .isPublished(productESDetailVm.isPublished())
                .categories(productESDetailVm.categories())
                .classifications(productESDetailVm.classifications())
                .productClassifications(productESDetailVm.productClassifications())
                .images(productESDetailVm.images())
                .createdDate(productESDetailVm.createdOn())
                .description(productESDetailVm.description())
                .isBanned(productESDetailVm.isBanned())
                .isDeleted(productESDetailVm.isDeleted())
                .build();
        Product savedProduct = productRepository.save(product);
    }

    public void deleteProduct(String id) {
        final boolean isProductExisted = productRepository.existsById(id);
        if (!isProductExisted) {
            throw new NotFoundException(MessageCode.PRODUCT_NOT_FOUND, id);
        }

        productRepository.deleteById(id);
    }
}
