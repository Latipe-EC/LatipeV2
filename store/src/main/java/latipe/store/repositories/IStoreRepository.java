package latipe.store.repositories;


import java.util.List;
import java.util.Optional;
import latipe.store.entity.Store;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface IStoreRepository extends MongoRepository<Store, String> {

  @Query("{'ownerId' : ?0, 'isDeleted' : false}")
  Optional<Store> findByOwnerId(String ownerId);

  List<Store> findByIdIn(List<String> ids);

  Boolean existsByName(String name);

  @Query(value = "{ 'isBan': { $in: ?0 }, 'name': { $regex: ?1, $options: 'i' }}", count = true)
  long countStoreAdmin(List<Boolean> isBanned, String keyword);
}
