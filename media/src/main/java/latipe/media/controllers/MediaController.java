package latipe.media.controllers;

import jakarta.servlet.http.HttpServletRequest;
import java.util.concurrent.CompletableFuture;
import latipe.media.annotations.ApiPrefixController;
import latipe.media.annotations.Authenticate;
import latipe.media.annotations.RequiresAuthorization;
import latipe.media.services.IMediaService;
import latipe.media.viewmodel.MediaVm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@ApiPrefixController("medias")
public class MediaController {

  private final IMediaService mediaService;

  public MediaController(IMediaService mediaService) {
    this.mediaService = mediaService;
  }

  @RequiresAuthorization("ADMIN")
  @ResponseStatus(HttpStatus.OK)
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<Page<MediaVm>> getPaginateCategory(
      @RequestParam(name = "name", defaultValue = "") String content, Pageable pageable,
      HttpServletRequest request) {
    return mediaService.findAllPaginate(content, pageable, request);
  }

  @Authenticate
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(consumes = "multipart/form-data")
  public CompletableFuture<MediaVm> uploadFile(@RequestPart("file") MultipartFile file,
      HttpServletRequest request) {

    return mediaService.saveMediaToCloud(file, request);
  }

  @RequiresAuthorization("ADMIN")
  @ResponseStatus(HttpStatus.CREATED)
  @DeleteMapping(value = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<Void> delete(@PathVariable String id, HttpServletRequest request) {
    return mediaService.remove(id, request);
  }


}
