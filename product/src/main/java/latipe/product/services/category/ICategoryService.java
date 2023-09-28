package latipe.product.services.category;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import latipe.product.dtos.PagedResultDto;
import latipe.product.request.CreateCategoryRequest;
import latipe.product.request.UpdateCategoryRequest;
import latipe.product.response.CategoryResponse;

public interface ICategoryService {

  public CompletableFuture<List<CategoryResponse>> getListChildrenCategory(String parentId);

  public CompletableFuture<PagedResultDto<CategoryResponse>> getPaginateCategory(long skip,
      int limit, String name);

  public CompletableFuture<CategoryResponse> update(String id, UpdateCategoryRequest input);

  public CompletableFuture<Void> remove(String id);

  public CompletableFuture<CategoryResponse> create(CreateCategoryRequest input);

}
