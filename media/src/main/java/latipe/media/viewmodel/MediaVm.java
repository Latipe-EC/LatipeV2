package latipe.media.viewmodel;

import java.util.Date;

public record MediaVm(String id, String fileName, String mediaType, String url, Date createdDate,
                      Date lastModifiedDate) {

}
