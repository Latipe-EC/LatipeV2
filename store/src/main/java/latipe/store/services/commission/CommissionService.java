package latipe.store.services.commission;


import static latipe.store.utils.AuthenticationUtils.getMethodName;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import java.util.concurrent.CompletableFuture;
import latipe.store.dtos.PagedResultDto;
import latipe.store.dtos.Pagination;
import latipe.store.exceptions.BadRequestException;
import latipe.store.exceptions.NotFoundException;
import latipe.store.mapper.CommissionMapper;
import latipe.store.repositories.ICommissionRepository;
import latipe.store.request.CreateCommissionRequest;
import latipe.store.request.UpdateCommissionRequest;
import latipe.store.response.CommissionResponse;
import latipe.store.viewmodel.LogMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommissionService implements ICommissionService {

  private final ICommissionRepository commissionRepository;
  private final CommissionMapper commissionMapper;
  private final Gson gson;

  @Override
  @Async
  public CompletableFuture<CommissionResponse> create(CreateCommissionRequest input,
      HttpServletRequest request) {
    log.info(gson.toJson(
        LogMessage.create("create commission", request, getMethodName())
    ));
    return CompletableFuture.supplyAsync(() -> {
      var check = commissionRepository.existsByFeeOrder(input.feeOrder());
      if (check) {
        throw new BadRequestException("Fee order existed");
      }

      var commission = commissionMapper.mapToStoreBeforeCreate(input);
      commission = commissionRepository.save(commission);
      log.info("Create commission successfully [id={}]", commission.getId());
      return commissionMapper.mapToResponse(commission);
    });
  }

  @Override
  @Async
  public CompletableFuture<Void> delete(String commissionId, HttpServletRequest request) {
    log.info(gson.toJson(
        LogMessage.create("Delete commission with [id: %s]".formatted(
            commissionId
        ), request, getMethodName())
    ));
    return CompletableFuture.supplyAsync(() -> {
      var commission = commissionRepository.findById(commissionId).orElseThrow(
          () -> new NotFoundException("Not found commission"));
      commissionRepository.delete(commission);
      log.info("Delete commission successfully [id={}]", commission.getId());
      return null;
    });
  }

  @Override
  @Async
  public CompletableFuture<CommissionResponse> update(String commissionId,
      UpdateCommissionRequest input, HttpServletRequest request) {
    log.info(gson.toJson(
        LogMessage.create("Update commission with [id: %s]".formatted(
            commissionId
        ), request, getMethodName())));

    return CompletableFuture.supplyAsync(() -> {
      var commission = commissionRepository.findById(commissionId).orElseThrow(
          () -> new NotFoundException("Not found commission"));

      commissionMapper.mapToStoreBeforeUpdate(commission, input);
      commission = commissionRepository.save(commission);
      log.info("Update commission successfully [id={}]", commission.getId());
      return commissionMapper.mapToResponse(commission);
    });
  }

  @Override
  public Double calcPercentStore(Integer point, HttpServletRequest request) {
    log.info(gson.toJson(
        LogMessage.create("Calculate percent store with [point: %s]".formatted(
            point
        ), request, getMethodName())));

    var listCommission = commissionRepository.findAll();

    if (listCommission.isEmpty()) {
      throw new BadRequestException("Please add commission");
    }

    for (var commission : listCommission) {
      if (commission.getMinPoint() > point) {
        return commission.getFeeOrder();
      }
    }
    log.info("Calculate percent store successfully [point={}]", point);
    return listCommission.get(0).getFeeOrder();
  }

  @Override
  @Async
  public CompletableFuture<PagedResultDto<CommissionResponse>> getPaginate(
      String keyword,
      Long skip,
      Integer size, HttpServletRequest request
  ) {
    log.info(gson.toJson(
        LogMessage.create(
            "Get paginate commission with [keyword: %s, skip: %s, size: %s]".formatted(
                keyword, skip, size
            ), request, getMethodName())
    ));
    return CompletableFuture.supplyAsync(() -> {
      var res = commissionRepository.findPaginate(keyword, skip, size);
      var total = commissionRepository.countCommission(keyword);
      log.info("Get paginate commission successfully [total={}, skip={}, size={}]",
          total, skip, size);
      return new PagedResultDto<>(
          new Pagination(total, skip, size),
          res.stream().map(commissionMapper::mapToResponse).toList());
    });
  }
}
