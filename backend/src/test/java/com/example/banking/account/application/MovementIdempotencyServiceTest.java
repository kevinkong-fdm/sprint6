package com.example.banking.account.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.banking.account.domain.AccountMovementEntity;
import com.example.banking.account.domain.MovementDirection;
import com.example.banking.account.domain.MovementType;
import com.example.banking.account.infrastructure.AccountMovementRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MovementIdempotencyServiceTest {

    @Mock
    private AccountMovementRepository accountMovementRepository;

    private MovementIdempotencyService service;

    @BeforeEach
    void setUp() {
        service = new MovementIdempotencyService(accountMovementRepository);
    }

    @Test
    void shouldReturnEmptyForBlankIdempotencyKey() {
        Optional<AccountMovementEntity> result = service.findExisting("acc-1", MovementType.DEPOSIT, " ");

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldQueryRepositoryForTrimmedIdempotencyKey() {
        AccountMovementEntity movement = AccountMovementEntity.posted(
                "acc-1",
                MovementType.DEPOSIT,
                new BigDecimal("1.0000"),
                MovementDirection.CREDIT,
                new BigDecimal("0.0000"),
                new BigDecimal("1.0000"),
                "idem-1",
                "corr-1",
                null);

        when(accountMovementRepository.findFirstByAccountIdAndMovementTypeAndIdempotencyKeyOrderByCreatedAtDesc(
                "acc-1",
                MovementType.DEPOSIT,
                "idem-1")).thenReturn(Optional.of(movement));

        Optional<AccountMovementEntity> result = service.findExisting("acc-1", MovementType.DEPOSIT, " idem-1 ");

        assertTrue(result.isPresent());
        assertEquals(movement.getMovementId(), result.get().getMovementId());
        verify(accountMovementRepository).findFirstByAccountIdAndMovementTypeAndIdempotencyKeyOrderByCreatedAtDesc(
                "acc-1",
                MovementType.DEPOSIT,
                "idem-1");
    }
}
