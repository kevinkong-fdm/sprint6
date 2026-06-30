package com.example.banking.standingorder.application;

import com.example.banking.standingorder.api.dto.StandingOrderListResponse;
import com.example.banking.standingorder.api.dto.StandingOrderResponse;
import com.example.banking.standingorder.api.dto.StandingOrderResponseMapper;
import com.example.banking.standingorder.domain.StandingOrderEntity;
import com.example.banking.standingorder.domain.StandingOrderStatus;
import com.example.banking.standingorder.infrastructure.StandingOrderRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class ListStandingOrdersService {

    private final StandingOrderAuthorizationService standingOrderAuthorizationService;
    private final StandingOrderRepository standingOrderRepository;
    private final StandingOrderResponseMapper standingOrderResponseMapper;

    public ListStandingOrdersService(
            StandingOrderAuthorizationService standingOrderAuthorizationService,
            StandingOrderRepository standingOrderRepository,
            StandingOrderResponseMapper standingOrderResponseMapper
    ) {
        this.standingOrderAuthorizationService = standingOrderAuthorizationService;
        this.standingOrderRepository = standingOrderRepository;
        this.standingOrderResponseMapper = standingOrderResponseMapper;
    }

    public StandingOrderListResponse list(
            String actorId,
            String status,
            int page,
            int size,
            String correlationId
    ) {
        String resolvedActorId = standingOrderAuthorizationService.requireActorId(actorId);

        StandingOrderStatus resolvedStatus = parseStatus(status);
        int resolvedPage = Math.max(1, page);
        int resolvedSize = Math.min(200, Math.max(1, size));

        PageRequest pageRequest = PageRequest.of(
                resolvedPage - 1,
                resolvedSize,
                Sort.by(Sort.Direction.ASC, "nextExecutionAt", "standingOrderId"));

        Page<StandingOrderEntity> result;
        if (resolvedStatus == null) {
            result = standingOrderRepository.findByCustomerId(resolvedActorId, pageRequest);
        } else {
            result = standingOrderRepository.findByCustomerIdAndStatus(resolvedActorId, resolvedStatus, pageRequest);
        }

        List<StandingOrderResponse> items = new ArrayList<>();
        for (StandingOrderEntity entity : result.getContent()) {
            items.add(standingOrderResponseMapper.toStandingOrderResponse(entity));
        }

        return standingOrderResponseMapper.toListResponse(
                items,
                resolvedPage,
                resolvedSize,
                result.getTotalElements(),
                correlationId);
    }

    private StandingOrderStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return StandingOrderStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new StandingOrderDomainException.CreateValidationException("Standing-order setup validation failed.");
        }
    }
}
