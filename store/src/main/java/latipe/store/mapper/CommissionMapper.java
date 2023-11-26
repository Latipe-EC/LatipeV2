package latipe.store.mapper;


import latipe.store.Entity.Commission;
import latipe.store.request.CreateCommissionRequest;
import latipe.store.request.UpdateCommissionRequest;
import latipe.store.response.CommissionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CommissionMapper {

  @Mapping(target = "id", ignore = true)
  Commission mapToStoreBeforeCreate(CreateCommissionRequest request);

  CommissionResponse mapToResponse(Commission input);

  void mapToStoreBeforeUpdate(@MappingTarget Commission commission,
      UpdateCommissionRequest request);
}
