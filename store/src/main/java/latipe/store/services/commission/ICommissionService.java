package latipe.store.services.commission;


import jakarta.servlet.http.HttpServletRequest;
import java.util.concurrent.CompletableFuture;
import latipe.store.dtos.PagedResultDto;
import latipe.store.request.CreateCommissionRequest;
import latipe.store.request.UpdateCommissionRequest;
import latipe.store.response.CommissionResponse;

public interface ICommissionService {

    CompletableFuture<CommissionResponse> create(CreateCommissionRequest input,
        HttpServletRequest request);

    CompletableFuture<Void> delete(String commissionId, HttpServletRequest request);

    CompletableFuture<CommissionResponse> update(String commissionId,
        UpdateCommissionRequest input, HttpServletRequest request);

    Double calcPercentStore(Integer point, HttpServletRequest request);

    CompletableFuture<PagedResultDto<CommissionResponse>> getPaginate(
        String keyword,
        Long skip,
        Integer size, HttpServletRequest request
    );
}
