package com.example.banking.standingorder.application;

import com.example.banking.standingorder.domain.StandingOrderEntity;
import com.example.banking.standingorder.domain.StandingOrderFrequency;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import org.springframework.stereotype.Service;

@Service
public class PlatformTimezoneService {

    private static final ZoneId PLATFORM_ZONE = ZoneId.of("Australia/Brisbane");
    private static final String PLATFORM_TIMEZONE_CODE = "AEST";

    public ZoneId zoneId() {
        return PLATFORM_ZONE;
    }

    public String timezoneCode() {
        return PLATFORM_TIMEZONE_CODE;
    }

    public Instant monthStart(YearMonth month) {
        return month.atDay(1)
                .atStartOfDay(PLATFORM_ZONE)
                .toInstant();
    }

    public Instant monthEndExclusive(YearMonth month) {
        return month.plusMonths(1)
                .atDay(1)
                .atStartOfDay(PLATFORM_ZONE)
                .toInstant();
    }

    public YearMonth parseMonth(String month) {
        try {
            return YearMonth.parse(month);
        } catch (Exception ex) {
            return null;
        }
    }

    public Instant computeNextExecution(
            StandingOrderFrequency frequency,
            LocalDate startDate,
            LocalDate endDate,
            DayOfWeek executionDayOfWeek,
            Integer executionDayOfMonth,
            LocalTime executionTime,
            Instant referenceInstant
    ) {
        if (frequency == null || startDate == null) {
            return null;
        }

        ZonedDateTime zonedReference = (referenceInstant == null ? Instant.now() : referenceInstant).atZone(PLATFORM_ZONE);
        LocalDate referenceDate = zonedReference.toLocalDate();
        LocalTime scheduleTime = executionTime == null ? LocalTime.of(9, 0) : executionTime;

        LocalDate candidate = startDate.isAfter(referenceDate) ? startDate : referenceDate;

        if (frequency == StandingOrderFrequency.DAILY) {
            // Candidate date already tracks the first eligible day.
        } else if (frequency == StandingOrderFrequency.WEEKLY) {
            DayOfWeek target = executionDayOfWeek == null ? startDate.getDayOfWeek() : executionDayOfWeek;
            candidate = candidate.with(TemporalAdjusters.nextOrSame(target));
        } else if (frequency == StandingOrderFrequency.MONTHLY) {
            int targetDay = executionDayOfMonth == null ? Math.min(startDate.getDayOfMonth(), 28) : executionDayOfMonth;
            targetDay = Math.max(1, Math.min(28, targetDay));

            candidate = alignMonthlyCandidate(candidate, targetDay);
            if (candidate.isBefore(startDate)) {
                candidate = alignMonthlyCandidate(startDate, targetDay);
            }
        }

        ZonedDateTime candidateDateTime = ZonedDateTime.of(candidate, scheduleTime, PLATFORM_ZONE);
        if (!candidateDateTime.toInstant().isAfter(referenceInstant == null ? Instant.now() : referenceInstant)) {
            if (frequency == StandingOrderFrequency.DAILY) {
                candidateDateTime = candidateDateTime.plusDays(1);
            } else if (frequency == StandingOrderFrequency.WEEKLY) {
                candidateDateTime = candidateDateTime.plusWeeks(1);
            } else {
                int targetDay = executionDayOfMonth == null ? Math.min(startDate.getDayOfMonth(), 28) : executionDayOfMonth;
                targetDay = Math.max(1, Math.min(28, targetDay));
                LocalDate nextMonthDate = alignMonthlyCandidate(candidateDateTime.toLocalDate().plusMonths(1), targetDay);
                candidateDateTime = ZonedDateTime.of(nextMonthDate, scheduleTime, PLATFORM_ZONE);
            }
        }

        if (endDate != null && candidateDateTime.toLocalDate().isAfter(endDate)) {
            return null;
        }

        return candidateDateTime.toInstant();
    }

    public Instant computeNextExecutionAfterRun(StandingOrderEntity standingOrder, Instant executedAt) {
        Instant reference = executedAt == null ? Instant.now() : executedAt;
        return computeNextExecution(
                standingOrder.getFrequency(),
                standingOrder.getStartDate(),
                standingOrder.getEndDate(),
                parseDayOfWeek(standingOrder.getExecutionDayOfWeek()),
                standingOrder.getExecutionDayOfMonth(),
                parseExecutionTime(standingOrder.getExecutionTime()),
                reference.plusSeconds(1));
    }

    public DayOfWeek parseDayOfWeek(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return DayOfWeek.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    public LocalTime parseExecutionTime(String value) {
        if (value == null || value.isBlank()) {
            return LocalTime.of(9, 0);
        }
        try {
            return LocalTime.parse(value.trim());
        } catch (Exception ex) {
            return LocalTime.of(9, 0);
        }
    }

    private LocalDate alignMonthlyCandidate(LocalDate fromDate, int targetDay) {
        int day = Math.min(targetDay, fromDate.lengthOfMonth());
        LocalDate sameMonth = fromDate.withDayOfMonth(day);
        if (sameMonth.isBefore(fromDate)) {
            LocalDate nextMonth = fromDate.plusMonths(1);
            int nextDay = Math.min(targetDay, nextMonth.lengthOfMonth());
            return nextMonth.withDayOfMonth(nextDay);
        }
        return sameMonth;
    }
}
