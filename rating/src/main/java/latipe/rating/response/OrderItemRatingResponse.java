package latipe.rating.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemRatingResponse {

  Integer code;
  String message;
  String error_code;
  DataOrderItemRating data;

  @Getter
  @Setter
  public static class DataOrderItemRating {

    String rating_id;
  }
}
