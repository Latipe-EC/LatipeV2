package latipe.search.services.store;


import latipe.search.Entity.Search;
import latipe.search.dtos.PagedResultDto;
import latipe.search.dtos.Pagination;
import latipe.search.exceptions.BadRequestException;
import latipe.search.repositories.ISearchRepository;
import latipe.search.services.store.Dtos.SearchCreateDto;
import latipe.search.services.store.Dtos.SearchDto;
import latipe.search.services.store.Dtos.SearchUpdateDto;
import latipe.search.utils.NullAwareBeanUtilsBean;
import org.modelmapper.ModelMapper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class SearchService implements ISearchService {
    private final ISearchRepository cateRepository;
    private final ModelMapper toDto;

    public SearchService(ISearchRepository cateRepository, ModelMapper toDto) {
        this.cateRepository = cateRepository;
        this.toDto = toDto;
    }


    @Override
    public CompletableFuture<List<SearchDto>> getAll() {
        return null;
    }

    @Override
    public CompletableFuture<SearchDto> getOne(String id) {
        return null;
    }

    @Override
    @Async
    public CompletableFuture<List<SearchDto>> getListChildrenSearch(String parentId) {
        return CompletableFuture.supplyAsync(() -> {
            List<Search> categories = cateRepository.findChildrenCate(parentId);
            return categories.stream().map(cate -> toDto.map(cate, SearchDto.class)).toList();
        });
    }

    @Override
    @Async
    public CompletableFuture<PagedResultDto<SearchDto>> getPaginateSearch(long skip, int limit, String name) {
        return CompletableFuture.supplyAsync(() -> {
            List<Search> categories = cateRepository.findSearchWithPaginationAndSearch(skip, limit, name);
            return PagedResultDto.create(
                    Pagination.create(cateRepository.countByName(name), skip, limit),
                    categories.stream().map(cate -> toDto.map(cate, SearchDto.class)).toList());
        });
    }

    @Override
    @Async
    public CompletableFuture<SearchDto> create(SearchCreateDto input) {
        return CompletableFuture.supplyAsync(() -> {
            Search cate = cateRepository.findByName(input.getName());
            if (cate != null) {
                throw new BadRequestException("Name search should be unique");
            }
            cate = toDto.map(input, Search.class);
            cateRepository.save(cate);
            return toDto.map(cate, SearchDto.class);
        });
    }

    @Override
    @Async
    public CompletableFuture<SearchDto> update(String id, SearchUpdateDto input) throws InvocationTargetException, IllegalAccessException {
        return CompletableFuture.supplyAsync(() -> {
            Search cate = cateRepository.findByName(input.getName());
            if (cate != null) {
                throw new BadRequestException("Name search should be unique");
            }
            cate = cateRepository.findById(id)
                    .orElseThrow(() -> new BadRequestException("Search not found"));
            if (cate.getIsDeleted())
                throw new BadRequestException("Search is deleted");
            NullAwareBeanUtilsBean nullAwareBeanUtilsBean = new NullAwareBeanUtilsBean();
            try {
                nullAwareBeanUtilsBean.copyProperties(cate, input);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
            cateRepository.save(cate);
            return toDto.map(cate, SearchDto.class);
        });
    }

    @Override
    @Async
    public CompletableFuture<Void> remove(String id) {
        return CompletableFuture.supplyAsync(() -> {
            Search cate = cateRepository.findById(id)
                    .orElseThrow(() -> new BadRequestException("Search not found"));
            cate.setIsDeleted(true);
            cateRepository.save(cate);
            return null;
        });
    }
}
