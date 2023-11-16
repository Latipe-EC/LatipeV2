package latipe.product.controllers;

import jakarta.validation.Valid;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import latipe.product.annotations.ApiPrefixController;
import latipe.product.annotations.Authenticate;
import latipe.product.annotations.RequiresAuthorization;
import latipe.product.dtos.PagedResultDto;
import latipe.product.request.CreateCategoryRequest;
import latipe.product.request.UpdateCategoryRequest;
import latipe.product.response.CategoryResponse;
import latipe.product.services.category.ICategoryService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ApiPrefixController("categories")
@AllArgsConstructor
public class CategoryController {

  private final ICategoryService categoryService;

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/paginate", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<PagedResultDto<CategoryResponse>> getPaginateCategory(
      @RequestParam(value = "skip", defaultValue = "0") long skip,
      @RequestParam(value = "limit", defaultValue = "10") int limit,
      @RequestParam(value = "name", defaultValue = "") String name) {
    return categoryService.getPaginateCategory(skip, limit, name);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<List<CategoryResponse>> searchNameCate(
      @RequestParam(value = "name") String name) {
    return categoryService.searchNameCate(name);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/children-categories/{parentId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<List<CategoryResponse>> getListChildrenCategory(
      @PathVariable String parentId) {
    return categoryService.getListChildrenCategory(parentId);
  }

  @Authenticate
  @RequiresAuthorization("ADMIN")
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<CategoryResponse> createStore(
      @Valid @RequestBody CreateCategoryRequest input) {
    return categoryService.create(input);
  }

  @Authenticate
  @RequiresAuthorization("ADMIN")
  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<CategoryResponse> get(@PathVariable("id") String id) {
    return categoryService.get(id);
  }

  @Authenticate
  @RequiresAuthorization("ADMIN")
  @ResponseStatus(HttpStatus.OK)
  @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<CategoryResponse> updateStore(@PathVariable("id") String id,
      @Valid @RequestBody UpdateCategoryRequest input) {
    return categoryService.update(id, input);
  }

  @Authenticate
  @RequiresAuthorization("ADMIN")
  @ResponseStatus(HttpStatus.OK)
  @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<Void> deleteCategory(@PathVariable("id") String id) {
    return categoryService.remove(id);
  }
}
