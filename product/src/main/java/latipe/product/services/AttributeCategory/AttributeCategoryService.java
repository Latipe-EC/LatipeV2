package latipe.product.services.AttributeCategory;

import java.util.concurrent.CompletableFuture;
import latipe.product.dtos.PagedResultDto;
import latipe.product.dtos.Pagination;
import latipe.product.exceptions.BadRequestException;
import latipe.product.mapper.AttributeCategoryMapper;
import latipe.product.repositories.IAttributeCategoryRepository;
import latipe.product.repositories.ICategoryRepository;
import latipe.product.request.CreateAttributeCategoryRequest;
import latipe.product.request.UpdateAttributeCategoryRequest;
import latipe.product.response.AttributeCategoryResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AttributeCategoryService implements IAttributeCategoryService {

    private final ICategoryRepository cateRepository;
    private final AttributeCategoryMapper attributeCategoryMapper;
    private final IAttributeCategoryRepository attributeCategoryRepository;

    @Override
    public CompletableFuture<AttributeCategoryResponse> getDetail(String id) {
        return CompletableFuture.supplyAsync(() -> {
            var attributeCategory = attributeCategoryRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Attribute Category is not existed"));
            return attributeCategoryMapper.mapToResponse(attributeCategory);
        });
    }

    @Override
    public CompletableFuture<AttributeCategoryResponse> getDetailByCateId(String id) {
        return CompletableFuture.supplyAsync(() -> {
            var attributeCategory = attributeCategoryRepository.findByCategoryId(id)
                .orElseThrow(() -> new BadRequestException("Attribute Category is not existed"));
            return attributeCategoryMapper.mapToResponse(attributeCategory);
        });
    }

    @Override
    public CompletableFuture<PagedResultDto<AttributeCategoryResponse>> getAttributeCategories(
        long skip, int limit) {
        return CompletableFuture.supplyAsync(() -> {
            var attributeCategories = attributeCategoryRepository.findAll(skip, limit);
            var count = attributeCategoryRepository.count();

            var list = attributeCategories.stream().map(attributeCategoryMapper::mapToResponse)
                .toList();
            return PagedResultDto.create(new Pagination(count, skip, limit), list);
        });
    }

    @Override
    public CompletableFuture<AttributeCategoryResponse> update(String id,
        UpdateAttributeCategoryRequest input) {
        return CompletableFuture.supplyAsync(() -> {
            var attributeCategory = attributeCategoryRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Attribute Category is not existed"));
            attributeCategoryMapper.mapToAttributeCategoryBeforeUpdate(attributeCategory, input);
            var res = attributeCategoryRepository.save(attributeCategory);
            return attributeCategoryMapper.mapToResponse(res);
        });
    }

    @Override
    public CompletableFuture<Void> remove(String id) {
        var attributeCategory = attributeCategoryRepository.findById(id)
            .orElseThrow(() -> new BadRequestException("Attribute Category is not existed"));
        attributeCategoryRepository.delete(attributeCategory);
        return null;
    }

    @Override
    public CompletableFuture<AttributeCategoryResponse> create(
        CreateAttributeCategoryRequest input) {
        return CompletableFuture.supplyAsync(() -> {
            cateRepository.findById(input.categoryId()).ifPresent((category) -> {
                throw new BadRequestException("Category is Existed");
            });
            var attributeCategory = attributeCategoryMapper.mapToAttributeCategoryBeforeCreate(
                input);
            var res = attributeCategoryRepository.save(attributeCategory);
            return attributeCategoryMapper.mapToResponse(res);
        });

    }
}
