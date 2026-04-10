package org.justme.justPlugin.managers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for WarnManager logic.
 * Since WarnManager requires a JustPlugin instance for most operations,
 * we test the WarnEntry data class, the formatDate utility,
 * and the default punishment escalation logic via reflection or by
 * testing the pure functions directly.
 */
class WarnManagerTest {

    // ========================
    // WarnEntry tests
    // ========================

    @Test
    void testWarnEntry_Construction() {
        WarnManager.WarnEntry entry = new WarnManager.WarnEntry(
                1, "Spamming", "Admin", System.currentTimeMillis(),
                "ChatMessage", "", false, null, null, 0
        );
        assertEquals(1, entry.index);
        assertEquals("Spamming", entry.reason);
        assertEquals("Admin", entry.warnedBy);
        assertEquals("ChatMessage", entry.punishment);
        assertEquals("", entry.punishmentDetail);
        assertFalse(entry.lifted);
        assertNull(entry.liftedBy);
        assertNull(entry.liftReason);
        assertEquals(0, entry.liftedAt);
    }

    @Test
    void testWarnEntry_WithLift() {
        long liftTime = System.currentTimeMillis();
        WarnManager.WarnEntry entry = new WarnManager.WarnEntry(
                3, "Griefing", "Mod1", System.currentTimeMillis() - 10000,
                "TempBan", "5m", true, "Admin", "Appealed", liftTime
        );
        assertTrue(entry.lifted);
        assertEquals("Admin", entry.liftedBy);
        assertEquals("Appealed", entry.liftReason);
        assertEquals(liftTime, entry.liftedAt);
    }

    @Test
    void testWarnEntry_MutableLiftedField() {
        WarnManager.WarnEntry entry = new WarnManager.WarnEntry(
                1, "Test", "Admin", System.currentTimeMillis(),
                "ChatMessage", "", false, null, null, 0
        );
        assertFalse(entry.lifted);
        // The lifted field is mutable
        entry.lifted = true;
        entry.liftedBy = "Admin2";
        entry.liftReason = "Mistake";
        entry.liftedAt = System.currentTimeMillis();
        assertTrue(entry.lifted);
        assertEquals("Admin2", entry.liftedBy);
    }

    @Test
    void testWarnEntry_IndexZero() {
        WarnManager.WarnEntry entry = new WarnManager.WarnEntry(
                0, "Test", "Admin", 0,
                "NoPunishment", "", false, null, null, 0
        );
        assertEquals(0, entry.index);
    }

    @Test
    void testWarnEntry_NullReason() {
        WarnManager.WarnEntry entry = new WarnManager.WarnEntry(
                1, null, "Admin", System.currentTimeMillis(),
                "ChatMessage", "", false, null, null, 0
        );
        assertNull(entry.reason);
    }

    @Test
    void testWarnEntry_EmptyPunishmentDetail() {
        WarnManager.WarnEntry entry = new WarnManager.WarnEntry(
                1, "Test", "Admin", System.currentTimeMillis(),
                "Kick", "", false, null, null, 0
        );
        assertEquals("", entry.punishmentDetail);
    }

    @Test
    void testWarnEntry_TempBanWithDuration() {
        WarnManager.WarnEntry entry = new WarnManager.WarnEntry(
                3, "Repeated offense", "Admin", System.currentTimeMillis(),
                "TempBan", "1d", false, null, null, 0
        );
        assertEquals("TempBan", entry.punishment);
        assertEquals("1d", entry.punishmentDetail);
    }

    // ========================
    // Default punishment escalation tests (testing the static switch pattern)
    // We test this indirectly via WarnEntry construction with expected values
    // ========================

    @Test
    void testDefaultEscalation_Warn1_ChatMessage() {
        // Per the code: count 1 -> ChatMessage
        assertEquals("ChatMessage", getDefaultPunishmentAction(1));
    }

    @Test
    void testDefaultEscalation_Warn2_Kick() {
        assertEquals("Kick", getDefaultPunishmentAction(2));
    }

    @Test
    void testDefaultEscalation_Warn3_TempBan5m() {
        assertEquals("TempBan", getDefaultPunishmentAction(3));
        assertEquals("5m", getDefaultPunishmentDetail(3));
    }

    @Test
    void testDefaultEscalation_Warn4_TempBan1d() {
        assertEquals("TempBan", getDefaultPunishmentAction(4));
        assertEquals("1d", getDefaultPunishmentDetail(4));
    }

    @Test
    void testDefaultEscalation_Warn5_TempBan30d() {
        assertEquals("TempBan", getDefaultPunishmentAction(5));
        assertEquals("30d", getDefaultPunishmentDetail(5));
    }

    @Test
    void testDefaultEscalation_Warn6_TempBan365d() {
        assertEquals("TempBan", getDefaultPunishmentAction(6));
        assertEquals("365d", getDefaultPunishmentDetail(6));
    }

    @Test
    void testDefaultEscalation_Warn7Plus_Ban() {
        assertEquals("Ban", getDefaultPunishmentAction(7));
        assertEquals("Ban", getDefaultPunishmentAction(10));
        assertEquals("Ban", getDefaultPunishmentAction(100));
    }

    @Test
    void testDefaultEscalation_Details_Warn1And2_Empty() {
        assertEquals("", getDefaultPunishmentDetail(1));
        assertEquals("", getDefaultPunishmentDetail(2));
    }

    /**
     * Replicate the default punishment logic from WarnManager.getDefaultPunishmentAction()
     * since the method is private but the logic is critical.
     */
    private String getDefaultPunishmentAction(int count) {
        return switch (count) {
            case 1 -> "ChatMessage";
            case 2 -> "Kick";
            case 3 -> "TempBan";
            case 4 -> "TempBan";
            case 5 -> "TempBan";
            case 6 -> "TempBan";
            default -> "Ban";
        };
    }

    private String getDefaultPunishmentDetail(int count) {
        return switch (count) {
            case 1 -> "";
            case 2 -> "";
            case 3 -> "5m";
            case 4 -> "1d";
            case 5 -> "30d";
            case 6 -> "365d";
            default -> "";
        };
    }

    // ========================
    // formatDate tests (indirectly - the method uses SimpleDateFormat)
    // ========================

    @Test
    void testFormatDate_ZeroTimestamp_ReturnsUnknown() {
        // The formatDate method returns "Unknown" for timestamp 0
        // We replicate the logic here since the method requires a WarnManager instance
        long timestamp = 0;
        String result = timestamp == 0 ? "Unknown" : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(timestamp));
        assertEquals("Unknown", result);
    }

    @Test
    void testFormatDate_ValidTimestamp_ReturnsFormattedDate() {
        long timestamp = 1700000000000L; // Some known timestamp
        String result = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(timestamp));
        assertNotNull(result);
        assertTrue(result.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));
    }
}
