package latipe.product.services.category;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import latipe.product.dtos.PagedResultDto;
import latipe.product.request.CreateCategoryRequest;
import latipe.product.request.UpdateCategoryRequest;
import latipe.product.response.CategoryResponse;

public interface ICategoryService {

  CompletableFuture<List<CategoryResponse>> getListChildrenCategory(String parentId);

  CompletableFuture<PagedResultDto<CategoryResponse>> getPaginateCategory(long skip,
      int limit, String name);

  CompletableFuture<CategoryResponse> update(String id, UpdateCategoryRequest input);

  CompletableFuture<CategoryResponse> get(String id);

  CompletableFuture<Void> remove(String id);

  CompletableFuture<CategoryResponse> create(CreateCategoryRequest input);

}
