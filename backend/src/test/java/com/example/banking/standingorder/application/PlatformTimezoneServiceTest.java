package com.example.banking.standingorder.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.banking.standingorder.domain.StandingOrderEntity;
import com.example.banking.standingorder.domain.StandingOrderFrequency;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import org.junit.jupiter.api.Test;

class PlatformTimezoneServiceTest {

    private final PlatformTimezoneService service = new PlatformTimezoneService();

    @Test
    void shouldExposePlatformTimezone() {
        assertEquals("Australia/Brisbane", service.zoneId().getId());
        assertEquals("AEST", service.timezoneCode());
    }

    @Test
    void shouldComputeMonthBoundariesAndParseMonth() {
        YearMonth month = YearMonth.parse("2026-06");
        Instant start = service.monthStart(month);
        Instant endExclusive = service.monthEndExclusive(month);

        assertTrue(endExclusive.isAfter(start));
        assertEquals(month, service.parseMonth("2026-06"));
        assertNull(service.parseMonth("2026-99"));
    }

    @Test
    void shouldComputeDailyWeeklyAndMonthlyExecutions() {
        Instant reference = Instant.parse("2026-06-10T00:00:00Z");

        Instant daily = service.computeNextExecution(
                StandingOrderFrequency.DAILY,
                LocalDate.parse("2026-06-01"),
                null,
                null,
                null,
                LocalTime.of(9, 0),
                reference);
        assertNotNull(daily);

        Instant weekly = service.computeNextExecution(
                StandingOrderFrequency.WEEKLY,
                LocalDate.parse("2026-06-01"),
                null,
                DayOfWeek.FRIDAY,
                null,
                LocalTime.of(9, 0),
                reference);
        assertNotNull(weekly);

        Instant monthly = service.computeNextExecution(
                StandingOrderFrequency.MONTHLY,
                LocalDate.parse("2026-01-05"),
                null,
                null,
                15,
                LocalTime.of(9, 0),
                reference);
        assertNotNull(monthly);
    }

    @Test
    void shouldParseScheduleHelpers() {
        assertEquals(DayOfWeek.MONDAY, service.parseDayOfWeek("monday"));
        assertNull(service.parseDayOfWeek("nope"));

        assertEquals(LocalTime.of(9, 0), service.parseExecutionTime(null));
        assertEquals(LocalTime.of(9, 0), service.parseExecutionTime("xx"));
        assertEquals(LocalTime.of(8, 30), service.parseExecutionTime("08:30"));
    }

    @Test
    void shouldComputeNextAfterRun() {
        StandingOrderEntity standingOrder = StandingOrderEntity.create(
                "cust-1",
                "src-1",
                "dst-1",
                new BigDecimal("10.0000"),
                StandingOrderFrequency.DAILY,
                LocalDate.parse("2026-06-01"),
                null,
                null,
                null,
                "09:00",
                Instant.parse("2026-06-02T00:00:00Z"),
                "AEST",
                "idem-1");

        Instant next = service.computeNextExecutionAfterRun(standingOrder, Instant.parse("2026-06-10T00:00:00Z"));
        assertNotNull(next);
    }
}
