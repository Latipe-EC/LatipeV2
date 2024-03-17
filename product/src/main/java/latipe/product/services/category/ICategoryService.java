package latipe.product.services.category;

import jakarta.servlet.http.HttpServletRequest;
import java.util.concurrent.CompletableFuture;
import latipe.product.request.CreateCategoryRequest;
import latipe.product.request.UpdateCategoryRequest;

public interface ICategoryService {

  CompletableFuture<?> getListChildrenCategory(String parentId, HttpServletRequest request);

  CompletableFuture<?> searchNameCate(String name, HttpServletRequest request);

  CompletableFuture<?> getPaginateCategory(long skip,
      int limit, String name, HttpServletRequest request);

  CompletableFuture<?> update(String id, UpdateCategoryRequest input, HttpServletRequest request);

  CompletableFuture<?> get(String id, HttpServletRequest request);

  CompletableFuture<?> remove(String id, HttpServletRequest request);

  CompletableFuture<?> create(CreateCategoryRequest input, HttpServletRequest request);

}
