package com.awakenedredstone.neoskies.util;

import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.util.concurrent.TimeUnit;

public class UnitConvertions {
    public static String formatTimings(double time) {
        return formatTimings(time, TimeUnit.MICROSECONDS);
    }

    public static String formatTimings(double time, TimeUnit origin) {
        double convertedTime = time * timeUnitScale(origin);

        String metric = "ns";
        if (convertedTime > 1000) {
            convertedTime /= 1000;
            metric = "µs";
        }
        if (convertedTime > 1000) {
            convertedTime /= 1000;
            metric = "ms";
        }
        if (convertedTime > 1000) {
            convertedTime /= 1000;
            metric = "s";

            if (convertedTime > 60) {
                convertedTime /= 60;
                metric = "m";
            }
            if (convertedTime > 60) {
                convertedTime /= 60;
                metric = "h";

                if (convertedTime > 24) {
                    convertedTime /= 24;
                    metric = "d";
                }
            }
        }
        return String.format("%.2f", convertedTime) + metric;
    }

    private static final NumberFormat DECIMAL_FORMAT = NumberFormat.getNumberInstance();
    public static String readableNumber(double value) {
        return DECIMAL_FORMAT.format(value);
    }

    public static long timeUnitScale(@NotNull TimeUnit unit) {
        final long NANO_SCALE   = 1L;
        final long MICRO_SCALE  = 1000L * NANO_SCALE;
        final long MILLI_SCALE  = 1000L * MICRO_SCALE;
        final long SECOND_SCALE = 1000L * MILLI_SCALE;
        final long MINUTE_SCALE = 60L * SECOND_SCALE;
        final long HOUR_SCALE   = 60L * MINUTE_SCALE;
        final long DAY_SCALE    = 24L * HOUR_SCALE;

        return switch (unit) {
            case NANOSECONDS -> NANO_SCALE;
            case MICROSECONDS -> MICRO_SCALE;
            case MILLISECONDS -> MILLI_SCALE;
            case SECONDS -> SECOND_SCALE;
            case MINUTES -> MINUTE_SCALE;
            case HOURS -> HOUR_SCALE;
            case DAYS -> DAY_SCALE;
        };
    }
}
