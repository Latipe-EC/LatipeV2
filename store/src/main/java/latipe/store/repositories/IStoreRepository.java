package latipe.store.repositories;


import java.util.List;
import latipe.store.Entity.Store;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface IStoreRepository extends MongoRepository<Store, String> {

  @Query("{'ownerId' : ?0, 'isDeleted' : false}")
  Store findByOwnerId(String ownerId);

  List<Store> findByIdIn(List<String> ids);

  Boolean existsByName(String name);
}
