package latipe.media.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "Medias")
@AllArgsConstructor
public class Media {

    @Id
    private String id;
    private String fileName;
    private String mediaType;
    private String url;
    private double fileSize;

    public Media(String fileName,
        String mediaType,
        String url,
        double fileSize) {
        this.fileName = fileName;
        this.mediaType = mediaType;
        this.url = url;
        this.fileSize = fileSize;
    }
}
