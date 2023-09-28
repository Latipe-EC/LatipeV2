package latipe.media.mapper;



import latipe.media.Entity.Media;
import latipe.media.viewmodel.MediaVm;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MediaMapper {

  MediaVm mapToMediaResponse(Media media);

}
