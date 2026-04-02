package org.justme.justPlugin.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TimeUtilTest {
    @Test void testParseSeconds() { assertEquals(5000L, TimeUtil.parseDuration("5s")); }
    @Test void testParseMinutes() { assertEquals(300000L, TimeUtil.parseDuration("5m")); }
    @Test void testParseHours() { assertEquals(3600000L, TimeUtil.parseDuration("1h")); }
    @Test void testParseDays() { assertEquals(86400000L, TimeUtil.parseDuration("1d")); }
    @Test void testParseWeeks() { assertEquals(604800000L, TimeUtil.parseDuration("1w")); }
    @Test void testParseCombined() { assertEquals(90000L, TimeUtil.parseDuration("1m30s")); }
    @Test void testParseComplex() { assertEquals(86400000L + 3600000L + 60000L, TimeUtil.parseDuration("1d1h1m")); }
    @Test void testParseDefaultSeconds() { assertEquals(30000L, TimeUtil.parseDuration("30")); }
    @Test void testFormatShort() { assertEquals("30s", TimeUtil.formatDuration(30000L)); }
    @Test void testFormatMinutes() { assertEquals("5m 0s", TimeUtil.formatDuration(300000L)); }
    @Test void testFormatZero() { assertEquals("0s", TimeUtil.formatDuration(0L)); }
    @Test void testFormatComplex() {
        String result = TimeUtil.formatDuration(93784000L);
        assertTrue(result.contains("1d") && result.contains("2h"));
    }
}
