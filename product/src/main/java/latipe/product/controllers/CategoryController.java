package latipe.product.controllers;

import jakarta.validation.Valid;
import latipe.product.annotations.ApiPrefixController;
import latipe.product.annotations.Authenticate;
import latipe.product.annotations.RequiresAuthorization;
import latipe.product.dtos.PagedResultDto;
import latipe.product.services.category.Dtos.CategoryCreateDto;
import latipe.product.services.category.Dtos.CategoryDto;
import latipe.product.services.category.Dtos.CategoryUpdateDto;
import latipe.product.services.category.ICategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@ApiPrefixController("categories")
public class CategoryController {
    private final ICategoryService categoryService;

    public CategoryController(ICategoryService categoryService) {
        this.categoryService = categoryService;
    }

    public CompletableFuture<PagedResultDto<CategoryDto>> getPaginateCategory(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "name", defaultValue = "") String content
    ){
        return categoryService.getPaginateCategory((long) page * size, size, content);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/children-categories/{parentID}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<List<CategoryDto>> getListChildrenCategory(@PathVariable("parentID") String parentID) {
        return categoryService.getListChildrenCategory(parentID);
    }
    @Authenticate
    @RequiresAuthorization("ADMIN")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<CategoryDto> createStore(
            @Valid @RequestBody CategoryCreateDto input) {
        return categoryService.create(input);
    }

    @Authenticate
    @RequiresAuthorization("ADMIN")
    @ResponseStatus(HttpStatus.OK)
    @PatchMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<CategoryDto> updateStore(
            @PathVariable("id") String id, @Valid @RequestBody CategoryUpdateDto input) throws InvocationTargetException, IllegalAccessException {
        return categoryService.update(id, input);
    }

    @Authenticate
    @RequiresAuthorization("ADMIN")
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<Void> deleteCategory(
            @PathVariable("id") String id) {
        return categoryService.remove(id);
    }
}
