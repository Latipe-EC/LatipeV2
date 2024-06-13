package latipe.product.services.category;

import static latipe.product.utils.AuthenticationUtils.getMethodName;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
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
import latipe.product.viewmodel.LogMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class CategoryService implements ICategoryService {

    private final ICategoryRepository cateRepository;
    private final CategoryMapper categoryMapper;
    private final CacheManager cacheManager;
    private final Gson gson;

    @Override
    @Async
    @Cacheable(value = "child_category_cache", key = "#parentId")
    public CompletableFuture<List<?>> getListChildrenCategory(String parentId,
        HttpServletRequest request) {

        log.info(
            gson.toJson(LogMessage.create("Get list children category", request, getMethodName())));

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

            log.info("Get list children category successfully");
            return categories.stream().map(categoryMapper::mapToCategoryResponse).toList();
        });
    }

    @Override
    @Async
    @Cacheable(value = "search_name_category_cache", key = "#name")
    public CompletableFuture<List<?>> searchNameCate(String name, HttpServletRequest request) {

        log.info(
            gson.toJson(LogMessage.create("Search category by name", request, getMethodName())));
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
            log.info("Search category by name successfully");
            return categories.stream().map(categoryMapper::mapToCategoryResponse).toList();
        });
    }

    @Override
    @Async
    @Cacheable(value = "get_paginate_category", key = "{ #skip, #limit, #name }")
    public CompletableFuture<?> getPaginateCategory(long skip, int limit, String name,
        HttpServletRequest request) {
        log.info(gson.toJson(LogMessage.create("Get paginate category", request, getMethodName())));
        var cache = cacheManager.getCache("get_paginate_category");
        if (cache != null) {
            String cacheKey = "{ #%s, #%s, #%s }".formatted(skip, limit, name);
            var cachedResult = cache.get(cacheKey, PagedResultDto.class);
            if (cachedResult != null) {
                log.info("Get paginate category successfully");
                return CompletableFuture.completedFuture(cachedResult);
            }
        }
        return CompletableFuture.supplyAsync(() -> {
            var categories = cateRepository.findCategoryWithPaginationAndSearch(skip, limit, name);
            log.info("Get paginate category successfully");
            return PagedResultDto.create(
                Pagination.create(cateRepository.countByName(name), skip, limit),
                categories.stream().map(categoryMapper::mapToCategoryResponse).toList());
        });
    }

    @Override
    @Async
    public CompletableFuture<?> create(CreateCategoryRequest input, HttpServletRequest request) {
        log.info(gson.toJson(LogMessage.create("Create category", request, getMethodName())));
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
            log.info("Create category successfully");
            return categoryMapper.mapToCategoryResponse(cate);
        });
    }

    @Override
    @Async
    public CompletableFuture<?> update(String id, UpdateCategoryRequest input,
        HttpServletRequest request) {
        log.info(gson.toJson(
            LogMessage.create("Update category with id: %s".formatted(id), request,
                getMethodName())));
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

            log.info("Update category successfully");
            return categoryMapper.mapToCategoryResponse(cate);
        });
    }

    @Override
    public CompletableFuture<?> get(String id, HttpServletRequest request) {
        log.info(gson.toJson(
            LogMessage.create("Get category with id: %s".formatted(id), request, getMethodName())));
        return CompletableFuture.supplyAsync(() -> {
            var cate = cateRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found"));
            log.info("Get category successfully");
            return categoryMapper.mapToCategoryResponse(cate);
        });
    }

    @Override
    @Async
    public CompletableFuture<?> remove(String id, HttpServletRequest request) {
        log.info(gson.toJson(
            LogMessage.create("Delete category with id: %s".formatted(id), request,
                getMethodName())));
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
                children = children.stream().peek(child -> child.setParentCategoryId(idAlter))
                    .toList();
                cateRepository.saveAll(children);
            }
            if (cate.getIsDeleted()) {
                throw new BadRequestException("Category is deleted");
            }
            cate.setIsDeleted(true);
            cateRepository.save(cate);
            log.info("Delete category successfully");
            return null;
        });
    }
}
