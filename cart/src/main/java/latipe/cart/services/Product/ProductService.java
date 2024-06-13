package latipe.cart.services.Product;

import static latipe.cart.constants.CONSTANTS.PRODUCT_SERVICE;
import static latipe.cart.utils.GenTokenInternal.generateHash;
import static latipe.cart.utils.GenTokenInternal.getPrivateKey;

import feign.Feign;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.okhttp.OkHttpClient;
import java.util.List;
import latipe.cart.configs.SecureInternalProperties;
import latipe.cart.feign.ProductClient;
import latipe.cart.request.ProductFeatureRequest;
import latipe.cart.response.ProductThumbnailResponse;
import latipe.cart.utils.GetInstanceServer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final SecureInternalProperties secureInternalProperties;
    private final LoadBalancerClient loadBalancer;
    private final GsonDecoder gsonDecoder;
    private final GsonEncoder gsonEncoder;
    private final OkHttpClient okHttpClient;

    @Value("${service.product}")
    private String productService;

    @Value("${eureka.client.enabled}")
    private boolean useEureka;

    public List<ProductThumbnailResponse> getProducts(
        List<ProductFeatureRequest> ids) {

        String hash;
        var productClient = Feign.builder().client(okHttpClient).encoder(gsonEncoder)
            .decoder(gsonDecoder).target(ProductClient.class,
                useEureka ? String.format("%s/api/v1", GetInstanceServer.get(
                    loadBalancer, productService
                )) : productService);
        try {
            hash = generateHash(PRODUCT_SERVICE,
                getPrivateKey(secureInternalProperties.getPrivateKey()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return productClient.getProducts(hash, ids);
    }
}
