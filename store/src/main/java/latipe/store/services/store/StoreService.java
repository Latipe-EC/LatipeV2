package latipe.store.services.store;


import latipe.store.Entity.Store;
import latipe.store.exceptions.BadRequestException;
import latipe.store.exceptions.NotFoundException;
import latipe.store.repositories.IStoreRepository;
import latipe.store.services.store.Dtos.StoreCreateDto;
import latipe.store.services.store.Dtos.StoreDto;
import latipe.store.services.store.Dtos.StoreUpdateDto;
import latipe.store.utils.NullAwareBeanUtilsBean;
import org.modelmapper.ModelMapper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class StoreService implements IStoreService {
    private final IStoreRepository storeRepository;
    private final ModelMapper toDto;

    public StoreService(IStoreRepository storeRepository, ModelMapper toDto) {
        this.storeRepository = storeRepository;
        this.toDto = toDto;
    }

    @Override
    @Async
    public CompletableFuture<StoreDto> create(String userId, StoreCreateDto input) {
        return CompletableFuture.supplyAsync(() -> {
            Store store = storeRepository.findByOwnerId(userId);
            if (store != null) {
                throw new BadRequestException("One User can only have one store");
            }
            store = toDto.map(input, Store.class);
            store.setOwnerId(userId);
            storeRepository.save(store);
            return toDto.map(store, StoreDto.class);
        });
    }

    @Override
    @Async
    public CompletableFuture<StoreDto> update(String userId, String storeId, StoreUpdateDto input) {
        return CompletableFuture.supplyAsync(() -> {
            Store store = storeRepository.findById(storeId).orElseThrow(
                    () -> new NotFoundException("Store not found")
            );
            NullAwareBeanUtilsBean nullAwareBeanUtilsBean = new NullAwareBeanUtilsBean();
            try {
                nullAwareBeanUtilsBean.copyProperties(store, input);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
            storeRepository.save(store);
            return toDto.map(store, StoreDto.class);
        });
    }

    @Override
    @Async
    public CompletableFuture<String> getStoreByUserId(String userId) {
        return CompletableFuture.supplyAsync(() -> {
            Store store = storeRepository.findByOwnerId(userId);
            if (store == null) {
                throw new BadRequestException("One User can only have one store");
            }
            if (store.getIsDeleted()) {
                throw new BadRequestException("Store is deleted");
            }
            return store.getId();
        });
    }


    @Override
    public CompletableFuture<List<StoreDto>> getAll() {
        return null;
    }

    @Override
    public CompletableFuture<StoreDto> getOne(String id) {
        return null;
    }

    @Override
    public CompletableFuture<StoreDto> create(StoreCreateDto input) {
        return null;
    }

    @Override
    public CompletableFuture<StoreDto> update(String id, StoreUpdateDto input) throws InvocationTargetException, IllegalAccessException {
        return null;
    }


    @Override
    public CompletableFuture<Void> remove(String id) {
        return null;
    }
}
