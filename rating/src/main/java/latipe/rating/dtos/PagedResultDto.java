package latipe.rating.dtos;

import java.util.List;
import lombok.Data;

@Data
public class PagedResultDto<TDto> {

    private Pagination pagination;
    private List<TDto> data;

    public PagedResultDto(Pagination pagination, List<TDto> data) {
        this.data = data;
        this.pagination = pagination;
    }

    public static <TDto> PagedResultDto<TDto> create(Pagination pagination, List<TDto> data) {
        return new PagedResultDto<TDto>(pagination, data);
    }
}
