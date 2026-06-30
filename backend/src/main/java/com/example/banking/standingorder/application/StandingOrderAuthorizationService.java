package com.example.banking.standingorder.application;

import com.example.banking.account.domain.BankAccountEntity;
import com.example.banking.account.infrastructure.BankAccountRepository;
import com.example.banking.standingorder.domain.StandingOrderEntity;
import com.example.banking.standingorder.infrastructure.StandingOrderRepository;
import org.springframework.stereotype.Service;

@Service
public class StandingOrderAuthorizationService {

    private final BankAccountRepository bankAccountRepository;
    private final StandingOrderRepository standingOrderRepository;

    public StandingOrderAuthorizationService(
            BankAccountRepository bankAccountRepository,
            StandingOrderRepository standingOrderRepository
    ) {
        this.bankAccountRepository = bankAccountRepository;
        this.standingOrderRepository = standingOrderRepository;
    }

    public String requireActorId(String actorId) {
        if (actorId == null || actorId.isBlank()) {
            throw new StandingOrderDomainException.AuthenticationRequiredException();
        }
        return actorId.trim();
    }

    public BankAccountEntity requireOwnedSourceAccount(String accountId, String actorId) {
        BankAccountEntity account = bankAccountRepository.findById(accountId)
                .orElseThrow(StandingOrderDomainException.SourceUnauthorizedException::new);

        if (!account.getCustomerId().equals(actorId)) {
            throw new StandingOrderDomainException.SourceUnauthorizedException();
        }

        return account;
    }

    public BankAccountEntity requireOwnedDestinationAccount(String accountId, String actorId) {
        BankAccountEntity account = bankAccountRepository.findById(accountId)
                .orElseThrow(StandingOrderDomainException.DestinationInternalOnlyException::new);

        if (!account.getCustomerId().equals(actorId)) {
            throw new StandingOrderDomainException.DestinationOwnershipMismatchException();
        }

        return account;
    }

    public StandingOrderEntity requireOwnedStandingOrder(String standingOrderId, String actorId) {
        StandingOrderEntity standingOrder = standingOrderRepository.findById(standingOrderId)
                .orElseThrow(StandingOrderDomainException.StandingOrderNotFoundException::new);

        if (!standingOrder.getCustomerId().equals(actorId)) {
            throw new StandingOrderDomainException.AccessForbiddenException();
        }

        return standingOrder;
    }
}
