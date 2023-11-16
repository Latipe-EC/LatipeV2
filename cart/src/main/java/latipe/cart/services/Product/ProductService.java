package latipe.cart.services.Product;

import static latipe.cart.constants.CONSTANTS.URL;
import static latipe.cart.utils.GenTokenInternal.generateHash;
import static latipe.cart.utils.GenTokenInternal.getPrivateKey;

import feign.Feign;
import feign.Logger;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.okhttp.OkHttpClient;
import java.util.List;
import latipe.cart.configs.SecureInternalProperties;
import latipe.cart.feign.ProductClient;
import latipe.cart.request.ProductFeatureRequest;
import latipe.cart.response.ProductThumbnailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {

  private final SecureInternalProperties secureInternalProperties;

  public List<ProductThumbnailResponse> getProducts(
      List<ProductFeatureRequest> ids) {
    var productClient = Feign.builder().client(new OkHttpClient()).encoder(new GsonEncoder())
        .decoder(new GsonDecoder()).logLevel(Logger.Level.FULL).target(ProductClient.class, URL);
    String hash;
    try {
      hash = generateHash("product-service",
          getPrivateKey(secureInternalProperties.getPrivateKey()));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return productClient.getProducts(hash, ids);
  }
}
