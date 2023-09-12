package latipe.search.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Document(indexName = "product")
@Setting(settingPath = "esconfig/elastic-analyzer.json")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    private String id;
    @Field(type = FieldType.Text, analyzer = "autocomplete_index", searchAnalyzer = "autocomplete_search")
    private String name;
    private String description;
    private String slug;
    @Field(type = FieldType.Double)
    private Double price;
    private Boolean isPublished;
    private List<String> images = new ArrayList<>();
    @Field(type = FieldType.Keyword)
    private List<String> categories;
    @Field(type = FieldType.Keyword)
    private List<String> classifications;
    @Field(type = FieldType.Date)
    private Date createdDate;
    private List<ProductClassification> productClassifications = new ArrayList<>();
    private String reasonBan;
    public boolean isBanned = false;
    private boolean isDeleted = false;
}
