package latipe.store.mapper;


import latipe.store.Entity.Store;
import latipe.store.request.CreateStoreRequest;
import latipe.store.request.UpdateStoreRequest;
import latipe.store.response.StoreResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StoreMapper {

  @Mapping(target = "id", ignore = true)
  Store mapToStoreBeforeCreate(CreateStoreRequest user, String ownerId);

  void mapToStoreBeforeUpdate(@MappingTarget Store category, UpdateStoreRequest input);

  StoreResponse mapToStoreResponse(Store category);

}
