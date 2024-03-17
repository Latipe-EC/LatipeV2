package latipe.media.services;

import jakarta.servlet.http.HttpServletRequest;
import java.util.concurrent.CompletableFuture;
import latipe.media.viewmodel.MediaVm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface IMediaService {

  CompletableFuture<MediaVm> saveMedia(MultipartFile file, HttpServletRequest request);

  CompletableFuture<Void> remove(String id, HttpServletRequest request);

  CompletableFuture<Page<MediaVm>> findAllPaginate(String fileName, Pageable pageable,
      HttpServletRequest request);

  CompletableFuture<MediaVm> saveMediaToCloud(MultipartFile file, HttpServletRequest request);
}
