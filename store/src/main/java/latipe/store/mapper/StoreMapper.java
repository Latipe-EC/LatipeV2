package latipe.store.mapper;


import latipe.store.entity.Store;
import latipe.store.request.CreateStoreRequest;
import latipe.store.request.UpdateStoreRequest;
import latipe.store.response.StoreDetailResponse;
import latipe.store.response.StoreResponse;
import latipe.store.response.StoreSimplifyResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StoreMapper {

    @Mapping(target = "id", ignore = true)
    Store mapToStoreBeforeCreate(CreateStoreRequest user, String ownerId);

    void mapToStoreBeforeUpdate(@MappingTarget Store category, UpdateStoreRequest input);

    @Mapping(target = "feePerOrder", source = "percent")
    StoreResponse mapToStoreResponse(Store store, Double percent);


    @Mappings({
        @Mapping(target = "feePerOrder", source = "percent"),
    })
    StoreDetailResponse mapToStoreDetailResponse(Store store, Double percent, Double eWallet);

    @Mapping(target = "cityOrProvinceId", source = "store.address.cityOrProvinceId")
    StoreSimplifyResponse mapToStoreSimplifyResponse(Store store);

}
