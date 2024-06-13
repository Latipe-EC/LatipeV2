package latipe.product.controllers;

import jakarta.validation.Valid;
import java.util.concurrent.CompletableFuture;
import latipe.product.annotations.ApiPrefixController;
import latipe.product.annotations.Authenticate;
import latipe.product.annotations.RequiresAuthorization;
import latipe.product.dtos.PagedResultDto;
import latipe.product.request.CreateAttributeCategoryRequest;
import latipe.product.request.UpdateAttributeCategoryRequest;
import latipe.product.response.AttributeCategoryResponse;
import latipe.product.services.AttributeCategory.IAttributeCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ApiPrefixController("attributes")
@RequiredArgsConstructor
public class AttributeCategoryController {

    private final IAttributeCategoryService attributeCategoryService;

    /**
     * @param id
     * @return AttributeCategoryResponse
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<AttributeCategoryResponse> getAttributeCategories(
        @PathVariable String id) {

        return attributeCategoryService.getDetail(id);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/cate/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<AttributeCategoryResponse> getByCate(@PathVariable String id) {
        return attributeCategoryService.getDetailByCateId(id);
    }

    /**
     * Get all attribute categories
     *
     * @param skip  The number of items to skip
     * @param limit The number of items to return
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/paginate", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<PagedResultDto<AttributeCategoryResponse>> getAttributeCategories(
        @RequestParam(value = "skip", defaultValue = "0") long skip,
        @RequestParam(value = "limit", defaultValue = "10") int limit) {

        return attributeCategoryService.getAttributeCategories(skip, limit);
    }

    /**
     * Create a new attribute category
     *
     * @param input interceptor call get new token from auth service when token call between service
     *              failThe request body { "name": "string", "description": "string" } name: The
     *              name of the attribute category description: The description of the attribute
     *              category The response body { "id": "string", "name": "string", "description":
     *              "string" } id: The ID of the attribute category name: The name of the attribute
     *              category description: The description of the attribute category The response
     *              status 201: The attribute category was created successfully 400: The request
     *              body is invalid 401: The user is not authenticated 403: The user is not
     *              authorized 500: An error occurred
     */
    @Authenticate
    @RequiresAuthorization("ADMIN")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<AttributeCategoryResponse> createStore(
        @Valid @RequestBody CreateAttributeCategoryRequest input) {
        return attributeCategoryService.create(input);
    }

    @Authenticate
    @RequiresAuthorization("ADMIN")
    @ResponseStatus(HttpStatus.OK)
    @PatchMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<AttributeCategoryResponse> updateStore(@PathVariable("id") String id,
        @Valid @RequestBody UpdateAttributeCategoryRequest input) {
        return attributeCategoryService.update(id, input);
    }

    @Authenticate
    @RequiresAuthorization("ADMIN")
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<Void> deleteCategory(@PathVariable("id") String id) {
        return attributeCategoryService.remove(id);
    }
}
