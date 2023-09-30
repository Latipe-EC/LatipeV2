package latipe.product.services.category;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import latipe.product.entity.Category;
import latipe.product.dtos.PagedResultDto;
import latipe.product.dtos.Pagination;
import latipe.product.exceptions.BadRequestException;
import latipe.product.mapper.CategoryMapper;
import latipe.product.repositories.ICategoryRepository;
import latipe.product.request.CreateCategoryRequest;
import latipe.product.request.UpdateCategoryRequest;
import latipe.product.response.CategoryResponse;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CategoryService implements ICategoryService {

  private final ICategoryRepository cateRepository;
  private final CategoryMapper categoryMapper;
  @Override
  @Async
  public CompletableFuture<List<CategoryResponse>> getListChildrenCategory(String parentId) {
    return CompletableFuture.supplyAsync(() -> {
      List<Category> categories = cateRepository.findChildrenCate(parentId);
      return categories.stream().map(categoryMapper::mapToCategoryResponse).toList();
    });
  }

  @Override
  @Async
  public CompletableFuture<PagedResultDto<CategoryResponse>> getPaginateCategory(long skip,
      int limit, String name) {
    return CompletableFuture.supplyAsync(() -> {
      List<Category> categories = cateRepository.findCategoryWithPaginationAndSearch(skip, limit,
          name);
      return PagedResultDto.create(
          Pagination.create(cateRepository.countByName(name), skip, limit),
          categories.stream().map(categoryMapper::mapToCategoryResponse).toList());
    });
  }

  @Override
  @Async
  public CompletableFuture<CategoryResponse> create(CreateCategoryRequest input) {
    return CompletableFuture.supplyAsync(() -> {
      Category cate = cateRepository.findByName(input.name());
      if (cate != null) {
        throw new BadRequestException("Name product should be unique");
      }
      cate = categoryMapper.mapToCategoryBeforeCreate(input);
      cateRepository.save(cate);
      return categoryMapper.mapToCategoryResponse(cate);
    });
  }

  @Override
  @Async
  public CompletableFuture<CategoryResponse> update(String id, UpdateCategoryRequest input) {
    return CompletableFuture.supplyAsync(() -> {

      Category cate = cateRepository.findByName(input.name());
      if (cate != null) {
        throw new BadRequestException("Name product should be unique");
      }
      cate = cateRepository.findById(id)
          .orElseThrow(() -> new BadRequestException("Category not found"));
      if (cate.getIsDeleted()) {
        throw new BadRequestException("Category is deleted");
      }

      categoryMapper.mapToCategoryBeforeUpdate(cate, input);
      cateRepository.save(cate);

      return categoryMapper.mapToCategoryResponse(cate);
    });
  }

  @Override
  @Async
  public CompletableFuture<Void> remove(String id) {
    return CompletableFuture.supplyAsync(() -> {
      Category cate = cateRepository.findById(id)
          .orElseThrow(() -> new BadRequestException("Category not found"));
      cate.setIsDeleted(true);
      cateRepository.save(cate);
      return null;
    });
  }

}
