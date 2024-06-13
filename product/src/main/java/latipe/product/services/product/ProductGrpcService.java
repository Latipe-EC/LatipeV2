package latipe.product.services.product;

import static latipe.product.utils.GenTokenInternal.generateHash;
import static latipe.product.utils.GenTokenInternal.getPrivateKey;

import feign.Feign;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.okhttp.OkHttpClient;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import latipe.product.configs.SecureInternalProperties;
import latipe.product.feign.StoreClient;
import latipe.product.grpc.GetPurchaseItemRequest;
import latipe.product.grpc.GetPurchaseItemResponse;
import latipe.product.grpc.GetPurchaseProductRequest;
import latipe.product.grpc.ItemResponse;
import latipe.product.grpc.ProductServiceGrpc;
import latipe.product.repositories.IProductRepository;
import latipe.product.response.ProvinceCodeResponse;
import latipe.product.utils.GetInstanceServer;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;

//@GRpcService
@RequiredArgsConstructor
public class ProductGrpcService extends ProductServiceGrpc.ProductServiceImplBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductGrpcService.class);
    private final IProductRepository productRepository;
    private final SecureInternalProperties secureInternalProperties;
    private final LoadBalancerClient loadBalancer;
    private final GsonDecoder gsonDecoder;
    private final GsonEncoder gsonEncoder;
    private final OkHttpClient okHttpClient;
    private final String storeService;
    private final boolean useEureka;

    @Override
    public void checkInStock(GetPurchaseProductRequest request,
        StreamObserver<GetPurchaseItemResponse> responseObserver) {

        LOGGER.info("Received request check in stock");
        var prods = productRepository.findAllByIdsAndStoreId(request.getItemsList().stream().map(
            GetPurchaseItemRequest::getProductId
        ).toList(), request.getStoreId());

        var classifications = prods.stream()
            .flatMap(prod -> prod.getProductClassifications().stream()
                .map(classification -> ItemResponse.newBuilder()
                    .setProductId(prod.getId())
                    .setQuantity(classification.getQuantity())
                    .setImage(prod.getImages().get(0))
                    .setName(prod.getName())
                    .setNameOption(classification.getName())
                    .setPromotionalPrice(classification.getPromotionalPrice().longValue())
                    .setPrice(classification.getPrice().longValue())
                    .setStoreId(prod.getStoreId())
                    .setOptionId(classification.getId())
                    .build()))
            .toList();

        long total = 0L;

        var classificationMap = classifications.stream()
            .collect(Collectors.toMap(
                c -> c.getProductId() + "-" + c.getOptionId(),
                Function.identity()
            ));

        List<ItemResponse> listItem = new ArrayList<>();
        for (var item : request.getItemsList()) {
            String key = item.getProductId() + "-" + item.getOptionId();
            var classificationOpt = Optional.ofNullable(classificationMap.get(key));
            if (classificationOpt.isPresent()) {
                var classification = classificationOpt.get();
                if (classification.getQuantity() >= item.getQuantity()) {
                    listItem.add(classification);
                    total += item.getQuantity() * (classification.getPromotionalPrice() > 0
                        ? classification.getPromotionalPrice() : classification.getPrice());
                } else {
                    responseObserver.onError(
                        Status.OUT_OF_RANGE.withDescription("Product out of stock")
                            .asRuntimeException());
                    return;
                }
            } else {
                responseObserver.onError(
                    Status.NOT_FOUND.withDescription("Product not found").asRuntimeException());
                return;
            }
        }

        // get store province code
        String hash;
        ProvinceCodeResponse storeProvinceCode;
        try {
            var storeClient = Feign.builder().client(okHttpClient).encoder(gsonEncoder)
                .decoder(gsonDecoder).target(StoreClient.class,
                    useEureka ? String.format("%s/api/v1", GetInstanceServer.get(
                        loadBalancer, storeService
                    )) : storeService);

            hash = generateHash("store-service",
                getPrivateKey(secureInternalProperties.getPrivateKey()));
            storeProvinceCode = storeClient.getProvinceCode(request.getStoreId(), hash);
        } catch (Exception e) {
            responseObserver.onError(
                Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
            return;
        }
        LOGGER.info("Check in stock successfully");
        responseObserver.onNext(
            GetPurchaseItemResponse.newBuilder()
                .setTotalPrice(total)
                .setStoreId(request.getStoreId())
                .setProvinceCode(storeProvinceCode.code())
                .addAllItems(listItem).build());
        responseObserver.onCompleted();
    }

//  @Override
//  public void updateQuantity(UpdateProductQuantityRequest request,
//      StreamObserver<UpdateProductQuantityResponse> responseObserver) {
//    var prods = productRepository.findAllByIdsAndStoreId(request.getItemsList().stream().map(
//        GetPurchaseItemRequest::getProductId
//    ).toList(), request.getStoreId());
//
//    // TODO if error send message queue
//    // TODO after confirm success send message commit to queue
//    for (var item : request.getItemsList()) {
//      var prod = prods.stream().filter(p -> p.getId().equals(item.getProductId())
//      ).findFirst();
//      if (prod.isPresent()) {
//        var classification = prod.get().getProductClassifications().stream().filter(
//            c -> c.getId().equals(item.getOptionId())
//        ).findFirst();
//        if (classification.isPresent()) {
//          if (classification.get().getQuantity() >= item.getQuantity()) {
//            classification.get()
//                .setQuantity(classification.get().getQuantity() - item.getQuantity());
//          } else {
//            responseObserver.onError(
//                Status.OUT_OF_RANGE.withDescription("Product out of stock").asRuntimeException());
//            return;
//          }
//        } else {
//          responseObserver.onError(
//              Status.NOT_FOUND.withDescription("Option not found").asRuntimeException());
//          return;
//        }
//      } else {
//        responseObserver.onError(
//            Status.NOT_FOUND.withDescription("Product not found").asRuntimeException());
//        return;
//      }
//    }
//
//    productRepository.saveAll(prods);
//    responseObserver.onNext(
//        UpdateProductQuantityResponse.newBuilder()
//            .setIsSuccess(true).build());
//  }
}
