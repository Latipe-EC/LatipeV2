package latipe.media.services;

import static latipe.media.constants.CONSTANTS.IMAGE;
import static latipe.media.constants.CONSTANTS.VIDEO;

import jakarta.servlet.http.HttpServletRequest;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import javax.imageio.ImageIO;
import latipe.media.entity.Media;
import latipe.media.exceptions.BadRequestException;
import latipe.media.exceptions.NotFoundException;
import latipe.media.mapper.MediaMapper;
import latipe.media.repositories.IMediaRepository;
import latipe.media.utils.FileCategorizeUtils;
import latipe.media.viewmodel.MediaVm;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

@Service
@AllArgsConstructor
public class MediaService implements IMediaService {

  private final IMediaRepository mediaRepository;
  private final MediaMapper mediaMapper;

  @Override
  @Async
  public CompletableFuture<MediaVm> saveMedia(MultipartFile file, String userId) {
    return CompletableFuture.supplyAsync(
        () -> {
          String type = FileCategorizeUtils.categorizeFile(file.getOriginalFilename());
          Path uploadPath = Paths.get(System.getProperty("user.dir"), "uploads");
          if (!Files.exists(uploadPath)) {
            try {
              Files.createDirectories(uploadPath);
            } catch (IOException e) {
              throw new RuntimeException(e.getMessage());
            }
          }
          String fileName = System.currentTimeMillis() + "-" + UUID.randomUUID() + "."
              + FileCategorizeUtils.getFileExtension(file.getOriginalFilename());
          HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

          Path path = Paths.get(uploadPath.toString(), fileName);
          try {
            if (type.equals(IMAGE)) {
              byte[] fileData = new byte[0];

              fileData = file.getBytes();

              BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(fileData));
              int newWidth = 1920;
              int newHeight = 1080;
              boolean isLargeImage = originalImage != null && originalImage.getWidth() > newWidth
                  && originalImage.getHeight() > newHeight;
              if (isLargeImage) {
                BufferedImage resizedImage = new BufferedImage(newWidth, newHeight,
                    originalImage.getType());
                Graphics2D g2d = resizedImage.createGraphics();
                g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
                g2d.dispose();
                ByteArrayOutputStream newImageBytes = new ByteArrayOutputStream();
                ImageIO.write(resizedImage, "jpg", newImageBytes);
                fileData = newImageBytes.toByteArray();
              }
              Files.write(path, fileData);
              String url =
                  (request.getRemoteAddr().equalsIgnoreCase("0:0:0:0:0:0:0:1") ? "http://localhost"
                      : "localhost") + ":" + request.getLocalPort() + "/uploads/" + fileName;
              var media = mediaRepository.save(new Media(fileName, type, url, file.getSize()));
              return mediaMapper.mapToMediaResponse(media);
            } else if (type.equals(VIDEO)) {
              Files.write(path, file.getBytes());
              String url =
                  (request.getRemoteAddr().equalsIgnoreCase("0:0:0:0:0:0:0:1") ? "http://localhost"
                      : "localhost") + ":" + request.getLocalPort() + "/uploads/" + fileName;
              var media = mediaRepository.save(new Media(fileName, type, url, file.getSize()));
              return mediaMapper.mapToMediaResponse(media);
            } else {
              throw new BadRequestException("Some thing went wrong!");
            }
          } catch (IOException e) {
            throw new BadRequestException(e.getMessage());
          }
        }
    );
  }

  @Override
  @Async
  public CompletableFuture<Page<MediaVm>> findAllPaginate(String fileName, Pageable pageable) {
    return CompletableFuture.supplyAsync(
        () -> mediaRepository.findAllPaginate(fileName, pageable)
            .map(mediaMapper::mapToMediaResponse)
    );
  }

  @Override
  @Async
  public CompletableFuture<Void> remove(String id) {
    return CompletableFuture.supplyAsync(
        () -> {
          Media noFileMediaVm = mediaRepository.findById(id)
              .orElseThrow(() -> new NotFoundException(String.format("Media %s is not found", id)));
          mediaRepository.deleteById(id);
          return null;
        }
    );
//        Path uploadPath = Paths.get(System.getProperty("user.dir"), "uploads");
//        if (!Files.exists(uploadPath)) {
//            try {
//                Files.createDirectories(uploadPath);
//            } catch (IOException e) {
//                throw new NotFoundException("Not found file name");
//            }
//        }
//        try {
//            if (fileName == null || fileName.trim() == "")
//                throw new NotFoundException("Not found file name");
//            java.io.File file = new java.io.File(uploadPath + "/" + fileName);
//            if (file.delete()) {
//                fileRepository.delete(fileRepository.findByName(fileName).orElse(null));
//                return CompletableFuture.completedFuture(null);
//            } else {
//                throw new RuntimeException("Some thing went wrong!");
//            }
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//        return CompletableFuture.completedFuture(null);
  }


}
