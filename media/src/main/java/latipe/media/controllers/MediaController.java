package latipe.media.controllers;

import java.util.concurrent.CompletableFuture;
import latipe.media.annotations.ApiPrefixController;
import latipe.media.annotations.Authenticate;
import latipe.media.annotations.RequiresAuthorization;
import latipe.media.dtos.UserCredentialDto;
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
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@ApiPrefixController("categories")
public class MediaController {

  private final IMediaService mediaService;

  public MediaController(IMediaService mediaService) {
    this.mediaService = mediaService;
  }

  @Authenticate
  @ResponseStatus(HttpStatus.OK)
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<Page<MediaVm>> getPaginateCategory(
      @RequestParam(name = "name", defaultValue = "") String content,
      Pageable pageable) {
    return mediaService.findAllPaginate(content, pageable);
  }

  @Authenticate
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<MediaVm> createStore(@RequestPart("file") MultipartFile file,
      @RequestAttribute(value = "user") UserCredentialDto userCredential) {

    return mediaService.saveMedia(file, userCredential.getId());
  }

  @RequiresAuthorization("ADMIN")
  @ResponseStatus(HttpStatus.CREATED)
  @DeleteMapping(value = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<Void> delete(@PathVariable String id) {
    return mediaService.remove(id);
  }


}
