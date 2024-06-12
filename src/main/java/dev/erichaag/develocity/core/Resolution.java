package dev.erichaag.develocity.core;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

enum Resolution {

    DAY(ChronoUnit.DAYS),
    WEEK(ChronoUnit.WEEKS),
    MONTH(ChronoUnit.MONTHS);

    private final ChronoUnit chronoUnit;

    Resolution(ChronoUnit chronoUnit) {
        this.chronoUnit = chronoUnit;
    }

    public ChronoUnit asChronoUnit() {
        return chronoUnit;
    }

    static Resolution from(Duration range) {
        if (range.compareTo(Duration.ofDays(90)) >= 0) {
            return MONTH;
        } else if (range.compareTo(Duration.ofDays(28)) >= 0) {
            return WEEK;
        }
        return DAY;
    }

    ZonedDateTime truncate(ZonedDateTime zonedDateTime) {
        if (this.equals(DAY)) {
            return zonedDateTime.truncatedTo(ChronoUnit.DAYS);
        } else if (this.equals(WEEK)) {
            return zonedDateTime.with(ChronoField.DAY_OF_WEEK, 1).truncatedTo(ChronoUnit.DAYS);
        } else {
            return zonedDateTime.withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS);
        }
    }

    String format(ZonedDateTime value) {
        if (this.equals(MONTH)) {
            return value.getMonth().getDisplayName(TextStyle.SHORT, Locale.US) + " " + value.getYear();
        } else if (this.equals(WEEK)) {
            final var start = value.with(ChronoField.DAY_OF_WEEK, 1).minusDays(1).truncatedTo(ChronoUnit.DAYS);
            final var end = value.with(ChronoField.DAY_OF_WEEK, 7).truncatedTo(ChronoUnit.DAYS).minusNanos(1);
            return start.getMonth().getDisplayName(TextStyle.SHORT, Locale.US) + " "
                    + start.getDayOfMonth() + " - "
                    + end.getMonth().getDisplayName(TextStyle.SHORT, Locale.US) + " "
                    + end.getDayOfMonth();
        }
        return value.getMonth().getDisplayName(TextStyle.SHORT, Locale.US) + " " + value.getDayOfMonth();
    }

}
