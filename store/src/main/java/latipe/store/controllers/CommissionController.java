package latipe.store.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.concurrent.CompletableFuture;
import latipe.store.annotations.ApiPrefixController;
import latipe.store.annotations.RequiresAuthorization;
import latipe.store.dtos.PagedResultDto;
import latipe.store.request.CreateCommissionRequest;
import latipe.store.request.UpdateCommissionRequest;
import latipe.store.response.CommissionResponse;
import latipe.store.services.commission.ICommissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
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
@ApiPrefixController("commissions")
@Validated
@RequiredArgsConstructor
public class CommissionController {

    private final ICommissionService commissionService;

    @RequiresAuthorization("ADMIN")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<CommissionResponse> createCommission(
        @RequestBody @Valid
        CreateCommissionRequest input, HttpServletRequest request

    ) {
        return commissionService.create(input, request);
    }

    @RequiresAuthorization("ADMIN")
    @ResponseStatus(HttpStatus.OK)
    @PutMapping(value = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<CommissionResponse> updateCommission(
        @PathVariable String id,
        @RequestBody @Valid
        UpdateCommissionRequest input, HttpServletRequest request

    ) {
        return commissionService.update(id, input, request);
    }

    @RequiresAuthorization("ADMIN")
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping(value = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<Void> deleteCommission(
        @PathVariable String id, HttpServletRequest request

    ) {
        return commissionService.delete(id, request);
    }

    @RequiresAuthorization("ADMIN")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/paginate", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<PagedResultDto<CommissionResponse>> getPaginate(
        @RequestParam(defaultValue = "") String keyword,
        @RequestParam(defaultValue = "0") Long skip,
        @RequestParam(defaultValue = "12") Integer size, HttpServletRequest request

    ) {
        return commissionService.getPaginate(keyword, skip, size, request);
    }
}
