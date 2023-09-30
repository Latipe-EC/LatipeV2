package latipe.media.repositories;

import latipe.media.entity.Media;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface IMediaRepository extends MongoRepository<Media, String> {

  @Query("{ 'fileName' : { $regex: ?0, $options: 'i' } }")
  Page<Media> findAllPaginate(String fileName, Pageable pageable);
}
