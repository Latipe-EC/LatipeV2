package latipe.product.services.category;


import latipe.product.dtos.PagedResultDto;
import latipe.product.services.IService;
import latipe.product.services.category.Dtos.CategoryCreateDto;
import latipe.product.services.category.Dtos.CategoryDto;
import latipe.product.services.category.Dtos.CategoryUpdateDto;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ICategoryService extends IService<CategoryDto, CategoryCreateDto, CategoryUpdateDto> {
    public CompletableFuture<List<CategoryDto>> getListChildrenCategory(String parentId);
    public CompletableFuture<PagedResultDto<CategoryDto>> getPaginateCategory(long skip, int limit, String name);
}

