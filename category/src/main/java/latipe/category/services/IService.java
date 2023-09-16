package latipe.category.services;


import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface IService<T, D, E> {
    CompletableFuture<List<T>> getAll();

    CompletableFuture<T> getOne(String id);

    CompletableFuture<T> create(D input);

    CompletableFuture<T> update(String id, E input) throws InvocationTargetException, IllegalAccessException;

//    CompletableFuture<PagedResultDto<T>> findAllPagination(HttpServletRequest request, Integer limit, Integer skip);
    CompletableFuture<Void> remove(String id);
}
