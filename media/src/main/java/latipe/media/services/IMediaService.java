package latipe.media.services;

import java.util.concurrent.CompletableFuture;
import latipe.media.viewmodel.MediaVm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface IMediaService {

  CompletableFuture<MediaVm> saveMedia(MultipartFile file, String userId);

  CompletableFuture<Void> remove(String id);

  CompletableFuture<Page<MediaVm>> findAllPaginate(String fileName, Pageable pageable);

  CompletableFuture<MediaVm> saveMediaToCloud(MultipartFile file, String userId);
}
