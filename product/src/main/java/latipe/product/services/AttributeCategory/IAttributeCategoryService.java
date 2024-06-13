package latipe.product.services.AttributeCategory;

import java.util.concurrent.CompletableFuture;
import latipe.product.dtos.PagedResultDto;
import latipe.product.request.CreateAttributeCategoryRequest;
import latipe.product.request.UpdateAttributeCategoryRequest;
import latipe.product.response.AttributeCategoryResponse;

public interface IAttributeCategoryService {

    CompletableFuture<AttributeCategoryResponse> getDetail(String id);

    CompletableFuture<AttributeCategoryResponse> getDetailByCateId(String cateId);

    CompletableFuture<PagedResultDto<AttributeCategoryResponse>> getAttributeCategories(long skip,
        int limit);

    CompletableFuture<AttributeCategoryResponse> update(String id,
        UpdateAttributeCategoryRequest input);

    CompletableFuture<Void> remove(String id);

    CompletableFuture<AttributeCategoryResponse> create(CreateAttributeCategoryRequest input);

}
