package latipe.search.controllers;

import jakarta.validation.Valid;
import latipe.search.annotations.ApiPrefixController;
import latipe.search.annotations.Authenticate;
import latipe.search.annotations.RequiresAuthorization;
import latipe.search.dtos.PagedResultDto;
import latipe.search.services.store.Dtos.SearchCreateDto;
import latipe.search.services.store.Dtos.SearchDto;
import latipe.search.services.store.Dtos.SearchUpdateDto;
import latipe.search.services.store.ISearchService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@ApiPrefixController("categories")
public class SearchController {
    private final ISearchService searchService;

    public SearchController(ISearchService searchService) {
        this.searchService = searchService;
    }

    public CompletableFuture<PagedResultDto<SearchDto>> getPaginateSearch(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "name", defaultValue = "") String content
    ){
        return searchService.getPaginateSearch((long) page * size, size, content);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/children-categories/{parentID}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<List<SearchDto>> getListChildrenSearch(@PathVariable("parentID") String parentID) {
        return searchService.getListChildrenSearch(parentID);
    }
    @Authenticate
    @RequiresAuthorization("ADMIN")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<SearchDto> createStore(
            @Valid @RequestBody SearchCreateDto input) {
        return searchService.create(input);
    }

    @Authenticate
    @RequiresAuthorization("ADMIN")
    @ResponseStatus(HttpStatus.OK)
    @PatchMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<SearchDto> updateStore(
            @PathVariable("id") String id, @Valid @RequestBody SearchUpdateDto input) throws InvocationTargetException, IllegalAccessException {
        return searchService.update(id, input);
    }

    @Authenticate
    @RequiresAuthorization("ADMIN")
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<Void> deleteSearch(
            @PathVariable("id") String id) {
        return searchService.remove(id);
    }
}
