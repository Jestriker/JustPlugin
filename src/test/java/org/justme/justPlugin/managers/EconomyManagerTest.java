package org.justme.justPlugin.managers;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.justme.justPlugin.JustPlugin;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for EconomyManager - balance operations, pay logic, and edge cases.
 * Uses Mockito to mock the JustPlugin and its dependencies.
 */
@ExtendWith(MockitoExtension.class)
class EconomyManagerTest {

    @Mock private JustPlugin plugin;
    @Mock private DataManager dataManager;
    @Mock private DatabaseManager databaseManager;

    private EconomyManager economy;
    private final UUID player1 = UUID.randomUUID();
    private final UUID player2 = UUID.randomUUID();
    private MockedStatic<Bukkit> bukkitMock;

    @BeforeEach
    void setUp() {
        // Mock Bukkit.getScheduler() for async save calls
        bukkitMock = mockStatic(Bukkit.class);
        BukkitScheduler scheduler = mock(BukkitScheduler.class);
        Server server = mock(Server.class);
        bukkitMock.when(Bukkit::getScheduler).thenReturn(scheduler);
        bukkitMock.when(Bukkit::getServer).thenReturn(server);
        lenient().when(server.getScheduler()).thenReturn(scheduler);
        lenient().when(plugin.getServer()).thenReturn(server);

        // Set up plugin config mock — all lenient to avoid strict stubbing issues
        org.bukkit.configuration.file.FileConfiguration config = mock(org.bukkit.configuration.file.FileConfiguration.class);
        lenient().when(plugin.getConfig()).thenReturn(config);
        lenient().when(config.getDouble("economy.starting-balance", 100.0)).thenReturn(100.0);
        lenient().when(config.getDouble("economy.max-balance", 1_000_000_000_000.0)).thenReturn(1_000_000_000_000.0);
        lenient().when(config.getString("economy.currency-symbol", "$")).thenReturn("$");

        lenient().when(plugin.getDataManager()).thenReturn(dataManager);
        lenient().when(plugin.getDatabaseManager()).thenReturn(databaseManager);
        // Return null provider so isUsingDatabase() returns false, and we avoid actual DB calls
        lenient().when(databaseManager.getProvider()).thenReturn(null);

        // DataManager returns empty YamlConfigurations for player data
        lenient().when(dataManager.getPlayerData(any(UUID.class))).thenReturn(new YamlConfiguration());

        economy = new EconomyManager(plugin);
    }

    @AfterEach
    void tearDown() {
        if (bukkitMock != null) bukkitMock.close();
    }

    // ========================
    // getBalance tests
    // ========================

    @Test
    void testGetBalance_NewPlayer_ReturnsStartingBalance() {
        double balance = economy.getBalance(player1);
        assertEquals(100.0, balance, 0.001);
    }

    @Test
    void testGetBalance_AfterSet_ReturnsSetValue() {
        economy.setBalance(player1, 500.0);
        assertEquals(500.0, economy.getBalance(player1), 0.001);
    }

    // ========================
    // setBalance tests
    // ========================

    @Test
    void testSetBalance_Zero() {
        economy.setBalance(player1, 0.0);
        assertEquals(0.0, economy.getBalance(player1), 0.001);
    }

    @Test
    void testSetBalance_NegativeClampedToZero() {
        economy.setBalance(player1, -100.0);
        assertEquals(0.0, economy.getBalance(player1), 0.001);
    }

    @Test
    void testSetBalance_NaN_ClampedToZero() {
        economy.setBalance(player1, Double.NaN);
        assertEquals(0.0, economy.getBalance(player1), 0.001);
    }

    @Test
    void testSetBalance_PositiveInfinity_ClampedToZero() {
        economy.setBalance(player1, Double.POSITIVE_INFINITY);
        assertEquals(0.0, economy.getBalance(player1), 0.001);
    }

    @Test
    void testSetBalance_NegativeInfinity_ClampedToZero() {
        economy.setBalance(player1, Double.NEGATIVE_INFINITY);
        assertEquals(0.0, economy.getBalance(player1), 0.001);
    }

    @Test
    void testSetBalance_ExceedsMaxBalance_ClampedToMax() {
        double maxBalance = economy.getMaxBalance();
        economy.setBalance(player1, maxBalance + 1000);
        assertEquals(maxBalance, economy.getBalance(player1), 0.001);
    }

    @Test
    void testSetBalance_ExactlyMaxBalance() {
        double maxBalance = economy.getMaxBalance();
        economy.setBalance(player1, maxBalance);
        assertEquals(maxBalance, economy.getBalance(player1), 0.001);
    }

    // ========================
    // addBalance tests
    // ========================

    @Test
    void testAddBalance_BasicAddition() {
        economy.setBalance(player1, 100.0);
        economy.addBalance(player1, 50.0);
        assertEquals(150.0, economy.getBalance(player1), 0.001);
    }

    @Test
    void testAddBalance_OverflowClampsToMax() {
        double maxBalance = economy.getMaxBalance();
        economy.setBalance(player1, maxBalance - 10);
        economy.addBalance(player1, 100);
        assertEquals(maxBalance, economy.getBalance(player1), 0.001);
    }

    @Test
    void testAddBalance_ToNewPlayer() {
        economy.addBalance(player1, 50.0);
        // Starting balance is 100, adding 50 should give 150
        assertEquals(150.0, economy.getBalance(player1), 0.001);
    }

    // ========================
    // removeBalance tests
    // ========================

    @Test
    void testRemoveBalance_SufficientFunds() {
        economy.setBalance(player1, 500.0);
        boolean result = economy.removeBalance(player1, 200.0);
        assertTrue(result);
        assertEquals(300.0, economy.getBalance(player1), 0.001);
    }

    @Test
    void testRemoveBalance_InsufficientFunds() {
        economy.setBalance(player1, 100.0);
        boolean result = economy.removeBalance(player1, 200.0);
        assertFalse(result);
        // Balance should remain unchanged
        assertEquals(100.0, economy.getBalance(player1), 0.001);
    }

    @Test
    void testRemoveBalance_ExactBalance() {
        economy.setBalance(player1, 100.0);
        boolean result = economy.removeBalance(player1, 100.0);
        assertTrue(result);
        assertEquals(0.0, economy.getBalance(player1), 0.001);
    }

    @Test
    void testRemoveBalance_ZeroAmount() {
        economy.setBalance(player1, 100.0);
        boolean result = economy.removeBalance(player1, 0.0);
        assertTrue(result);
        assertEquals(100.0, economy.getBalance(player1), 0.001);
    }

    // ========================
    // pay tests
    // ========================

    @Test
    void testPay_TransfersCorrectly() {
        economy.setBalance(player1, 500.0);
        economy.setBalance(player2, 200.0);
        boolean result = economy.pay(player1, player2, 100.0);
        assertTrue(result);
        assertEquals(400.0, economy.getBalance(player1), 0.001);
        assertEquals(300.0, economy.getBalance(player2), 0.001);
    }

    @Test
    void testPay_InsufficientFunds() {
        economy.setBalance(player1, 50.0);
        economy.setBalance(player2, 200.0);
        boolean result = economy.pay(player1, player2, 100.0);
        assertFalse(result);
        // Both balances unchanged
        assertEquals(50.0, economy.getBalance(player1), 0.001);
        assertEquals(200.0, economy.getBalance(player2), 0.001);
    }

    @Test
    void testPay_ZeroAmount() {
        economy.setBalance(player1, 500.0);
        economy.setBalance(player2, 200.0);
        boolean result = economy.pay(player1, player2, 0.0);
        assertFalse(result, "Pay with zero amount should return false");
    }

    @Test
    void testPay_NegativeAmount() {
        economy.setBalance(player1, 500.0);
        economy.setBalance(player2, 200.0);
        boolean result = economy.pay(player1, player2, -50.0);
        assertFalse(result, "Pay with negative amount should return false");
    }

    @Test
    void testPay_RecipientPayToggleOff() {
        economy.setBalance(player1, 500.0);
        economy.setBalance(player2, 200.0);
        // Toggle pay off for player2
        // First, need player2 in balances cache so isPayToggleOff checks cache
        economy.getBalance(player2);
        economy.togglePay(player2);
        boolean result = economy.pay(player1, player2, 100.0);
        assertFalse(result, "Pay should be rejected when recipient has pay toggled off");
        // Balances unchanged
        assertEquals(500.0, economy.getBalance(player1), 0.001);
        assertEquals(200.0, economy.getBalance(player2), 0.001);
    }

    // ========================
    // pay toggle tests
    // ========================

    @Test
    void testPayToggle_DefaultIsOff() {
        // Load player into balances cache first
        economy.getBalance(player1);
        assertFalse(economy.isPayToggleOff(player1));
    }

    @Test
    void testPayToggle_ToggleOn() {
        economy.getBalance(player1);
        economy.togglePay(player1);
        assertTrue(economy.isPayToggleOff(player1));
    }

    @Test
    void testPayToggle_ToggleOnThenOff() {
        economy.getBalance(player1);
        economy.togglePay(player1);
        assertTrue(economy.isPayToggleOff(player1));
        economy.togglePay(player1);
        assertFalse(economy.isPayToggleOff(player1));
    }

    // ========================
    // format tests
    // ========================

    @Test
    void testFormat_BasicAmount() {
        String formatted = economy.format(1234.56);
        assertEquals("$1,234.56", formatted);
    }

    @Test
    void testFormat_Zero() {
        String formatted = economy.format(0.0);
        assertEquals("$0.00", formatted);
    }

    @Test
    void testFormat_LargeAmount() {
        String formatted = economy.format(1000000.0);
        assertEquals("$1,000,000.00", formatted);
    }

    @Test
    void testFormat_SmallFraction() {
        String formatted = economy.format(0.01);
        assertEquals("$0.01", formatted);
    }

    // ========================
    // provider tests
    // ========================

    @Test
    void testIsUsingVault_DefaultFalse() {
        assertFalse(economy.isUsingVault());
    }

    @Test
    void testGetProviderName_DefaultJustPlugin() {
        assertEquals("JustPlugin", economy.getProviderName());
    }

    @Test
    void testGetMaxBalance() {
        assertEquals(1_000_000_000_000.0, economy.getMaxBalance(), 0.001);
    }
}
