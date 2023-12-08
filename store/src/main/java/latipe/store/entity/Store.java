package latipe.store.entity;


import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "Stores")
public class Store {

  @Id
  String id;
  String name;
  String description;
  String logo;
  String ownerId;
  String cover;
  private Double rating = 0.0;
  private int point = 0;
  private Double eWallet = 0.0;
  private Boolean isActive = true;
  private Boolean isDeleted = false;
  private Boolean isBan = false;
  private String reasonBan;
  private StoreAddress address;
}
