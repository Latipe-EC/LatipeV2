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

/**
 * Service implementation for managing store commissions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CommissionService implements ICommissionService {

    private final ICommissionRepository commissionRepository;
    private final CommissionMapper commissionMapper;
    private final Gson gson;

    /**
     * Creates a new commission rate asynchronously.
     *
     * @param input   The request containing the details for the new commission.
     * @param request The HTTP servlet request (consider extracting needed info earlier).
     * @return A CompletableFuture containing the created commission's response details.
     * @throws BadRequestException if the fee order already exists.
     */
    @Override
    @Async
    public CompletableFuture<CommissionResponse> create(CreateCommissionRequest input,
        HttpServletRequest request) {
        // Potential Improvement: Extract necessary info from request
        // before this async method to avoid potential request scope issues.
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

    /**
     * Deletes a commission rate by its ID asynchronously.
     *
     * @param commissionId The ID of the commission to delete.
     * @param request      The HTTP servlet request (consider extracting needed info earlier).
     * @return A CompletableFuture indicating completion.
     */
    @Override
    @Async
    public CompletableFuture<Void> delete(String commissionId, HttpServletRequest request) {
        // Potential Improvement: Extract necessary info from request
        // before this async method to avoid potential request scope issues.
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

    /**
     * Updates an existing commission rate asynchronously.
     *
     * @param commissionId The ID of the commission to update.
     * @param input        The request containing the updated commission details.
     * @param request      The HTTP servlet request (consider extracting needed info earlier).
     * @return A CompletableFuture containing the updated commission's response details.
     */
    @Override
    @Async
    public CompletableFuture<CommissionResponse> update(String commissionId,
        UpdateCommissionRequest input, HttpServletRequest request) {
        // Potential Improvement: Extract necessary info from request
        // before this async method to avoid potential request scope issues.
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

    /**
     * Calculates the store percentage based on a given point value.
     * Note: This method is synchronous.
     *
     * @param point   The point value to use for calculation.
     * @param request The HTTP servlet request (consider extracting needed info earlier if needed).
     * @return The calculated percentage as a Double.
     */
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

    /**
     * Retrieves commission rates with pagination and keyword filtering asynchronously.
     *
     * @param keyword The keyword to filter commissions (optional).
     * @param skip    The number of records to skip.
     * @param size    The maximum number of records to return.
     * @param request The HTTP servlet request (consider extracting needed info earlier).
     * @return A CompletableFuture containing a paged result of commission responses.
     */
    @Override
    @Async
    public CompletableFuture<PagedResultDto<CommissionResponse>> getPaginate(
        String keyword,
        Long skip,
        Integer size, HttpServletRequest request
    ) {
        // Potential Improvement: Extract necessary info from request
        // before this async method to avoid potential request scope issues.
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
