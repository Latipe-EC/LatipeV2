package latipe.media.dtos;

import lombok.Data;

@Data
public class CreateMediaDto {

    private String fileName;
    private String mediaType;
    private String url;
    private double fileSize;
}
