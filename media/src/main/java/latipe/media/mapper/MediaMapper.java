package latipe.media.mapper;


import latipe.media.entity.Media;
import latipe.media.viewmodel.MediaVm;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MediaMapper {

  MediaVm mapToMediaResponse(Media media);

}
