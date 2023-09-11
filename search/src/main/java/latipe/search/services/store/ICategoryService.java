package latipe.search.services.store;


import latipe.search.dtos.PagedResultDto;
import latipe.search.services.IService;
import latipe.search.services.store.Dtos.SearchCreateDto;
import latipe.search.services.store.Dtos.SearchDto;
import latipe.search.services.store.Dtos.SearchUpdateDto;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ISearchService extends IService<SearchDto, SearchCreateDto, SearchUpdateDto> {
    public CompletableFuture<List<SearchDto>> getListChildrenSearch(String parentId);
    public CompletableFuture<PagedResultDto<SearchDto>> getPaginateSearch(long skip, int limit, String name);
}

