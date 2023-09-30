package latipe.product.entity;

import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariant {

  @Id
  String id = new ObjectId().toString();
  String name;
  String image;
  List<String> options;

  @CreatedBy
  @Field("created_by")
  private String createdBy;

  @LastModifiedDate
  @Field("last_modified_date")
  private Date lastModifiedDate;

}
