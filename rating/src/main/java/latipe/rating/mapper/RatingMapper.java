package latipe.rating.mapper;


import latipe.rating.Entity.Rating;
import latipe.rating.response.RatingResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class RatingMapper {

  public abstract RatingResponse mapToRatingResponse(Rating rating);
}
