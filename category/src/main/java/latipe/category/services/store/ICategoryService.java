package latipe.category.services.store;


import latipe.category.dtos.PagedResultDto;
import latipe.category.services.IService;
import latipe.category.services.store.Dtos.CategoryCreateDto;
import latipe.category.services.store.Dtos.CategoryDto;
import latipe.category.services.store.Dtos.CategoryUpdateDto;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ICategoryService extends IService<CategoryDto, CategoryCreateDto, CategoryUpdateDto> {
    public CompletableFuture<List<CategoryDto>> getListChildrenCategory(String parentId);
    public CompletableFuture<PagedResultDto<CategoryDto>> getPaginateCategory(long skip, int limit, String name);
}

