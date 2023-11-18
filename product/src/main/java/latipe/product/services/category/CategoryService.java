package latipe.product.services.category;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import latipe.product.dtos.PagedResultDto;
import latipe.product.dtos.Pagination;
import latipe.product.entity.Category;
import latipe.product.exceptions.BadRequestException;
import latipe.product.exceptions.NotFoundException;
import latipe.product.mapper.CategoryMapper;
import latipe.product.repositories.ICategoryRepository;
import latipe.product.request.CreateCategoryRequest;
import latipe.product.request.UpdateCategoryRequest;
import lombok.AllArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CategoryService implements ICategoryService {

  private final ICategoryRepository cateRepository;
  private final CategoryMapper categoryMapper;
  private final CacheManager cacheManager;

  @Override
  @Async
  @Cacheable(value = "child_category_cache", key = "#parentId")
  public CompletableFuture<List<?>> getListChildrenCategory(String parentId) {

    var cache = cacheManager.getCache("child_category_cache");
    if (cache != null) {
      List<?> cachedCategories = cache.get(parentId, List.class);
      if (cachedCategories != null && !cachedCategories.isEmpty()) {
        return CompletableFuture.completedFuture(cachedCategories);
      }
    }

    return CompletableFuture.supplyAsync(() -> {
      List<Category> categories;
      if (parentId.equals("null")) {
        categories = cateRepository.findChildrenCate(null);
      } else {
        categories = cateRepository.findChildrenCate(parentId);
      }
      return categories.stream().map(categoryMapper::mapToCategoryResponse).toList();
    });
  }

  @Override
  @Async
  @Cacheable(value = "search_name_category_cache", key = "#name")
  public CompletableFuture<List<?>> searchNameCate(String name) {

    var cache = cacheManager.getCache("search_name_category_cache");
    if (cache != null) {
      List<?> cachedCategories = cache.get(name, List.class);
      if (cachedCategories != null) {
        return CompletableFuture.completedFuture(cachedCategories);
      }
    }

    return CompletableFuture.supplyAsync(() -> {

      var categories = cateRepository.findCateByName(name);
      var ids = categories.stream().map(Category::getFirstParentCategoryId).toList();
      if (ids.isEmpty()) {
        return List.of();
      }
      categories = cateRepository.findAllById(ids);
      return categories.stream().map(categoryMapper::mapToCategoryResponse).toList();
    });
  }

  @Override
  @Async
  @Cacheable(value = "get_paginate_category", key = "{ #skip, #limit, #name }")
  public CompletableFuture<?> getPaginateCategory(long skip, int limit, String name) {
    var cache = cacheManager.getCache("get_paginate_category");
    if (cache != null) {
      String cacheKey = "{ #%s, #%s, #%s }".formatted(skip, limit, name);
      var cachedResult = cache.get(cacheKey, PagedResultDto.class);
      if (cachedResult != null) {
        return CompletableFuture.completedFuture(cachedResult);
      }
    }
    return CompletableFuture.supplyAsync(() -> {

      var categories = cateRepository.findCategoryWithPaginationAndSearch(skip, limit, name);
      return PagedResultDto.create(Pagination.create(cateRepository.countByName(name), skip, limit),
          categories.stream().map(categoryMapper::mapToCategoryResponse).toList());
    });
  }

  @Override
  @Async
  public CompletableFuture<?> create(CreateCategoryRequest input) {
    return CompletableFuture.supplyAsync(() -> {
      var cate = cateRepository.findByName(input.name());
      if (cate != null) {
        throw new BadRequestException("Name category should be unique");
      }
      if (input.idAttributeCategory() != null) {
        cateRepository.findById(input.idAttributeCategory())
            .orElseThrow(() -> new BadRequestException("Parent Category not found"));
      }
      cate = categoryMapper.mapToCategoryBeforeCreate(input);
      cate = cateRepository.save(cate);
      return categoryMapper.mapToCategoryResponse(cate);
    });
  }

  @Override
  @Async
  public CompletableFuture<?> update(String id, UpdateCategoryRequest input) {
    return CompletableFuture.supplyAsync(() -> {

      var cate = cateRepository.findByNameAndExceptId(input.name(), id);
      if (cate != null) {
        throw new BadRequestException("Name category should be unique");
      }
      cate = cateRepository.findById(id)
          .orElseThrow(() -> new BadRequestException("Category not found"));
      if (cate.getIsDeleted()) {
        throw new BadRequestException("Category is deleted");
      }

      if (input.idAttributeCategory() != null) {
        cateRepository.findById(input.idAttributeCategory())
            .orElseThrow(() -> new BadRequestException("Parent Category not found"));
      }
      categoryMapper.mapToCategoryBeforeUpdate(cate, input);
      cateRepository.save(cate);

      return categoryMapper.mapToCategoryResponse(cate);
    });
  }

  @Override
  public CompletableFuture<?> get(String id) {
    return CompletableFuture.supplyAsync(() -> {
      var cate = cateRepository.findById(id)
          .orElseThrow(() -> new NotFoundException("Category not found"));
      return categoryMapper.mapToCategoryResponse(cate);
    });
  }

  @Override
  @Async
  public CompletableFuture<?> remove(String id) {
    return CompletableFuture.supplyAsync(() -> {
      Category cate = cateRepository.findById(id)
          .orElseThrow(() -> new BadRequestException("Category not found"));

      var children = cateRepository.findChildrenCate(id);
      if (!children.isEmpty()) {
        String idAlter;
        if (cate.getParentCategoryId() == null) {
          var firstCate = cateRepository.findFirst().get(0);
          idAlter = firstCate.getId();
        } else {
          idAlter = cate.getParentCategoryId();
        }
        children = children.stream().peek(child -> child.setParentCategoryId(idAlter)).toList();
        cateRepository.saveAll(children);
      }
      if (cate.getIsDeleted()) {
        throw new BadRequestException("Category is deleted");
      }
      cate.setIsDeleted(true);
      cateRepository.save(cate);
      return null;
    });
  }
}
