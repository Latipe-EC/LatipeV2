package latipe.product.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.concurrent.CompletableFuture;
import latipe.product.annotations.ApiPrefixController;
import latipe.product.annotations.Authenticate;
import latipe.product.annotations.RequiresAuthorization;
import latipe.product.request.CreateCategoryRequest;
import latipe.product.request.UpdateCategoryRequest;
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
  public CompletableFuture<?> getPaginateCategory(
      @RequestParam(value = "skip", defaultValue = "0") long skip,
      @RequestParam(value = "limit", defaultValue = "10") int limit,
      @RequestParam(value = "name", defaultValue = "") String name, HttpServletRequest request) {
    return categoryService.getPaginateCategory(skip, limit, name, request);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<?> searchNameCate(
      @RequestParam(value = "name") String name, HttpServletRequest request) {
    return categoryService.searchNameCate(name, request);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/children-categories/{parentId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<?> getListChildrenCategory(
      @PathVariable String parentId, HttpServletRequest request) {
    return categoryService.getListChildrenCategory(parentId, request);
  }

  @Authenticate
  @RequiresAuthorization("ADMIN")
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<?> createStore(
      @Valid @RequestBody CreateCategoryRequest input, HttpServletRequest request) {
    return categoryService.create(input, request);
  }

  @Authenticate
  @RequiresAuthorization("ADMIN")
  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<?> get(@PathVariable("id") String id, HttpServletRequest request) {
    return categoryService.get(id, request);
  }

  @Authenticate
  @RequiresAuthorization("ADMIN")
  @ResponseStatus(HttpStatus.OK)
  @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<?> updateStore(@PathVariable("id") String id,
      @Valid @RequestBody UpdateCategoryRequest input, HttpServletRequest request) {
    return categoryService.update(id, input, request);
  }

  @Authenticate
  @RequiresAuthorization("ADMIN")
  @ResponseStatus(HttpStatus.OK)
  @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<?> deleteCategory(@PathVariable("id") String id,
      HttpServletRequest request) {
    return categoryService.remove(id, request);
  }
}
