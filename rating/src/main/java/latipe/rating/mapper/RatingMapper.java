package latipe.rating.mapper;


import latipe.rating.entity.Rating;
import latipe.rating.request.CreateRatingRequest;
import latipe.rating.request.UpdateRatingRequest;
import latipe.rating.response.RatingResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class RatingMapper {

    public abstract RatingResponse mapToRatingResponse(Rating rating);

    public abstract Rating mapToRatingBeforeCreate(CreateRatingRequest rating, String userId,
        String username,
        String avatar);

    public abstract void mapToRatingBeforeUpdate(@MappingTarget Rating rating,
        UpdateRatingRequest request, Boolean isChange);

}
