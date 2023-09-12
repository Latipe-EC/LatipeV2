package latipe.product.services.category;
import latipe.product.Entity.Category;
import latipe.product.dtos.PagedResultDto;
import latipe.product.dtos.Pagination;
import latipe.product.exceptions.BadRequestException;
import latipe.product.repositories.ICategoryRepository;

import latipe.product.services.category.Dtos.CategoryCreateDto;
import latipe.product.services.category.Dtos.CategoryDto;
import latipe.product.services.category.Dtos.CategoryUpdateDto;
import latipe.product.services.category.ICategoryService;
import latipe.product.utils.NullAwareBeanUtilsBean;
import org.modelmapper.ModelMapper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class CategoryService implements ICategoryService {
    private final ICategoryRepository cateRepository;
    private final ModelMapper toDto;

    public CategoryService(ICategoryRepository cateRepository, ModelMapper toDto) {
        this.cateRepository = cateRepository;
        this.toDto = toDto;
    }


    @Override
    public CompletableFuture<List<CategoryDto>> getAll() {
        return null;
    }

    @Override
    public CompletableFuture<CategoryDto> getOne(String id) {
        return null;
    }

    @Override
    @Async
    public CompletableFuture<List<CategoryDto>> getListChildrenCategory(String parentId) {
        return CompletableFuture.supplyAsync(() -> {
            List<Category> categories = cateRepository.findChildrenCate(parentId);
            return categories.stream().map(cate -> toDto.map(cate, CategoryDto.class)).toList();
        });
    }

    @Override
    @Async
    public CompletableFuture<PagedResultDto<CategoryDto>> getPaginateCategory(long skip, int limit, String name) {
        return CompletableFuture.supplyAsync(() -> {
            List<Category> categories = cateRepository.findCategoryWithPaginationAndSearch(skip, limit, name);
            return PagedResultDto.create(
                    Pagination.create(cateRepository.countByName(name), skip, limit),
                    categories.stream().map(cate -> toDto.map(cate, CategoryDto.class)).toList());
        });
    }

    @Override
    @Async
    public CompletableFuture<CategoryDto> create(CategoryCreateDto input) {
        return CompletableFuture.supplyAsync(() -> {
            Category cate = cateRepository.findByName(input.getName());
            if (cate != null) {
                throw new BadRequestException("Name product should be unique");
            }
            cate = toDto.map(input, Category.class);
            cateRepository.save(cate);
            return toDto.map(cate, CategoryDto.class);
        });
    }

    @Override
    @Async
    public CompletableFuture<CategoryDto> update(String id, CategoryUpdateDto input) throws InvocationTargetException, IllegalAccessException {
        return CompletableFuture.supplyAsync(() -> {
            Category cate = cateRepository.findByName(input.getName());
            if (cate != null) {
                throw new BadRequestException("Name product should be unique");
            }
            cate = cateRepository.findById(id)
                    .orElseThrow(() -> new BadRequestException("Category not found"));
            if (cate.getIsDeleted())
                throw new BadRequestException("Category is deleted");
            NullAwareBeanUtilsBean nullAwareBeanUtilsBean = new NullAwareBeanUtilsBean();
            try {
                nullAwareBeanUtilsBean.copyProperties(cate, input);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
            cateRepository.save(cate);
            return toDto.map(cate, CategoryDto.class);
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
