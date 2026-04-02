package org.justme.justPlugin.managers;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CooldownManagerFormatTest {
    @Test void testSeconds() { assertEquals("30s", CooldownManager.formatTime(30)); }
    @Test void testMinutes() { assertEquals("5m", CooldownManager.formatTime(300)); }
    @Test void testMinSec() { assertEquals("2m 30s", CooldownManager.formatTime(150)); }
    @Test void testHours() { assertEquals("1h", CooldownManager.formatTime(3600)); }
    @Test void testHourMin() { assertEquals("1h 30m", CooldownManager.formatTime(5400)); }
    @Test void testZero() { assertEquals("0s", CooldownManager.formatTime(0)); }
}
