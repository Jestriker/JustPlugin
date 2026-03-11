package org.justme.justPlugin.util;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

public final class TimeUtil {

    private TimeUtil() {}

    public static String formatDuration(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        seconds %= 60;
        minutes %= 60;
        hours %= 24;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        sb.append(seconds).append("s");
        return sb.toString().trim();
    }

    public static long parseDuration(String input) {
        long total = 0;
        StringBuilder num = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (Character.isDigit(c)) {
                num.append(c);
            } else {
                if (num.isEmpty()) continue;
                long val = Long.parseLong(num.toString());
                num = new StringBuilder();
                switch (Character.toLowerCase(c)) {
                    case 's' -> total += val * 1000L;
                    case 'm' -> total += val * 60_000L;
                    case 'h' -> total += val * 3_600_000L;
                    case 'd' -> total += val * 86_400_000L;
                    case 'w' -> total += val * 604_800_000L;
                }
            }
        }
        if (!num.isEmpty()) {
            total += Long.parseLong(num.toString()) * 1000L; // default seconds
        }
        return total;
    }

    public static String getRealTime(String timezone) {
        ZonedDateTime now = ZonedDateTime.now(TimeZone.getTimeZone(timezone).toZoneId());
        return now.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    public static String getRealDate(String timezone) {
        ZonedDateTime now = ZonedDateTime.now(TimeZone.getTimeZone(timezone).toZoneId());
        return now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    public static String getGameTime(long ticks) {
        long hours = (ticks / 1000 + 6) % 24;
        long minutes = (ticks % 1000) * 60 / 1000;
        return String.format("%02d:%02d", hours, minutes);
    }
}

