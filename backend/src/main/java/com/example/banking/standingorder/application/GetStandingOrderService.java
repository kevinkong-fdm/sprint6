package com.example.banking.standingorder.application;

import com.example.banking.standingorder.api.dto.StandingOrderResponseMapper;
import com.example.banking.standingorder.api.dto.StandingOrderSingleResponse;
import com.example.banking.standingorder.domain.StandingOrderEntity;
import org.springframework.stereotype.Service;

@Service
public class GetStandingOrderService {

    private final StandingOrderAuthorizationService standingOrderAuthorizationService;
    private final StandingOrderResponseMapper standingOrderResponseMapper;

    public GetStandingOrderService(
            StandingOrderAuthorizationService standingOrderAuthorizationService,
            StandingOrderResponseMapper standingOrderResponseMapper
    ) {
        this.standingOrderAuthorizationService = standingOrderAuthorizationService;
        this.standingOrderResponseMapper = standingOrderResponseMapper;
    }

    public StandingOrderSingleResponse getById(String standingOrderId, String actorId, String correlationId) {
        String resolvedActorId = standingOrderAuthorizationService.requireActorId(actorId);
        StandingOrderEntity standingOrder = standingOrderAuthorizationService.requireOwnedStandingOrder(standingOrderId, resolvedActorId);
        return standingOrderResponseMapper.toSingleResponse(standingOrder, correlationId);
    }
}
