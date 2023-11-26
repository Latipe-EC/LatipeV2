package latipe.store.services.commission;


import java.util.concurrent.CompletableFuture;
import latipe.store.exceptions.BadRequestException;
import latipe.store.exceptions.NotFoundException;
import latipe.store.mapper.CommissionMapper;
import latipe.store.repositories.ICommissionRepository;
import latipe.store.repositories.IStoreRepository;
import latipe.store.request.CreateCommissionRequest;
import latipe.store.request.UpdateCommissionRequest;
import latipe.store.response.CommissionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommissionService implements ICommissionService {

  private final IStoreRepository storeRepository;
  private final ICommissionRepository commissionRepository;
  private final CommissionMapper commissionMapper;

  @Override
  public CompletableFuture<CommissionResponse> create(CreateCommissionRequest request) {
    return CompletableFuture.supplyAsync(() -> {
      var check = commissionRepository.existsByFeeOrder(request.feeOrder());
      if (check) {
        throw new BadRequestException("Fee order existed");
      }

      var commission = commissionMapper.mapToStoreBeforeCreate(request);
      commissionRepository.save(commission);
      return commissionMapper.mapToResponse(commission);
    });
  }

  @Override
  public CompletableFuture<Void> delete(String commissionId) {
    return CompletableFuture.supplyAsync(() -> {
      var commission = commissionRepository.findById(commissionId).orElseThrow(
          () -> new NotFoundException("Not found commission"));
      commissionRepository.delete(commission);
      return null;
    });
  }

  @Override
  public CompletableFuture<CommissionResponse> update(String commissionId,
      UpdateCommissionRequest request) {
    return CompletableFuture.supplyAsync(() -> {
      var commission = commissionRepository.findById(commissionId).orElseThrow(
          () -> new NotFoundException("Not found commission"));

      commissionMapper.mapToStoreBeforeUpdate(commission, request);
      commissionRepository.save(commission);
      return commissionMapper.mapToResponse(commission);
    });
  }

  @Override
  public Double calcPercentStore(String storeId) {

    var store = storeRepository.findById(storeId).orElseThrow(
        () -> new NotFoundException("Not found store")
    );

    var listCommission = commissionRepository.findAll();

    if (listCommission.isEmpty()) {
      throw new BadRequestException("Please add commission");
    }

    for (var commission : listCommission) {
      if (commission.getMinPoint() > store.getPoint()) {
        return commission.getFeeOrder();
      }
    }
    return listCommission.get(listCommission.size() - 1).getFeeOrder();

  }
}
