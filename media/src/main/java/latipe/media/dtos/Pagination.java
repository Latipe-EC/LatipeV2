package latipe.media.dtos;

import lombok.Data;

@Data
public class Pagination {

    private long total;
    private long skip;
    private int limit;

    public Pagination(long total, long skip, int limit) {
        this.total = total;
        this.skip = skip;
        this.limit = limit;
    }

    public static Pagination create(long total, long skip, int limit) {
        return new Pagination(total, skip, limit);
    }

}


