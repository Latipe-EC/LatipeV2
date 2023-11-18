package latipe.product.services.category;

import java.util.concurrent.CompletableFuture;
import latipe.product.request.CreateCategoryRequest;
import latipe.product.request.UpdateCategoryRequest;

public interface ICategoryService {

  CompletableFuture<?> getListChildrenCategory(String parentId);

  CompletableFuture<?> searchNameCate(String name);

  CompletableFuture<?> getPaginateCategory(long skip,
      int limit, String name);

  CompletableFuture<?> update(String id, UpdateCategoryRequest input);

  CompletableFuture<?> get(String id);

  CompletableFuture<?> remove(String id);

  CompletableFuture<?> create(CreateCategoryRequest input);

}
