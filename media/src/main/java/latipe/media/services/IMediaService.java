package latipe.media.services;

import latipe.media.Entity.Media;
import latipe.media.viewmodel.MediaVm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.CompletableFuture;

public interface IMediaService {
    CompletableFuture<MediaVm> saveMedia(MultipartFile file, String userId);
    public CompletableFuture<Void> remove(String id);
    public CompletableFuture<Page<MediaVm>> findAllPaginate(String fileName, Pageable pageable);
}
