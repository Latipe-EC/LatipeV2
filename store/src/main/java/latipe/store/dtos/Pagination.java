package latipe.store.dtos;

import lombok.Data;

@Data
public class Pagination {

    private long total;
    private int skip;
    private int limit;

    public Pagination(long total, int skip, int limit) {
        this.total = total;
        this.skip = skip;
        this.limit = limit;
    }

    public static Pagination create(long total, int skip, int limit) {
        return new Pagination(total, skip, limit);
    }

}


