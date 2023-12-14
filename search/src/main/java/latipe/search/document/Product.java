package latipe.search.document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

@Document(indexName = "product")
@Setting(settingPath = "esconfig/elastic-analyzer.json")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product {

  @Id
  private String id;
  @Field(type = FieldType.Text, analyzer = "vietnamese_analyzer", searchAnalyzer = "autocomplete_search")
  private String name;
  private String description;
  private String slug;
  @Field(type = FieldType.Double)
  private Double price;
  private Boolean isPublished;
  private int countSale = 0;
  private List<String> images = new ArrayList<>();
  @Field(type = FieldType.Keyword)
  private List<String> categories;
  @Field(type = FieldType.Keyword)
  private List<String> classifications;
  @Field(type = FieldType.Date)
  private Date createdDate;
  private List<ProductClassification> productClassifications = new ArrayList<>();
  private String reasonBan;
  private boolean isDeleted = false;
  private Date lastModifiedDate;
  private double ratings = 0.0;
  public boolean isBanned = false;

}
