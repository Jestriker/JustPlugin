package org.justme.justPlugin.api;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for JustPluginProvider - the static API accessor.
 */
class JustPluginProviderTest {

    @BeforeEach
    void setUp() {
        // Ensure clean state before each test
        JustPluginProvider.clear();
    }

    @AfterEach
    void tearDown() {
        // Clean up after each test
        JustPluginProvider.clear();
    }

    @Test
    void testGetReturnsNullBeforeSet() {
        assertNull(JustPluginProvider.get(), "Provider should return null before any instance is set");
    }

    @Test
    void testSetThenGetReturnsInstance() {
        JustPluginAPI mockApi = new DummyJustPluginAPI();
        JustPluginProvider.set(mockApi);
        assertSame(mockApi, JustPluginProvider.get(), "Provider should return the exact instance that was set");
    }

    @Test
    void testClearThenGetReturnsNull() {
        JustPluginAPI mockApi = new DummyJustPluginAPI();
        JustPluginProvider.set(mockApi);
        assertNotNull(JustPluginProvider.get());
        JustPluginProvider.clear();
        assertNull(JustPluginProvider.get(), "Provider should return null after clear()");
    }

    @Test
    void testSetOverwritesPreviousInstance() {
        JustPluginAPI first = new DummyJustPluginAPI();
        JustPluginAPI second = new DummyJustPluginAPI();
        JustPluginProvider.set(first);
        assertSame(first, JustPluginProvider.get());
        JustPluginProvider.set(second);
        assertSame(second, JustPluginProvider.get(), "Setting a new instance should overwrite the previous one");
        assertNotSame(first, JustPluginProvider.get());
    }

    @Test
    void testSetNullExplicitly() {
        JustPluginAPI mockApi = new DummyJustPluginAPI();
        JustPluginProvider.set(mockApi);
        assertNotNull(JustPluginProvider.get());
        JustPluginProvider.set(null);
        assertNull(JustPluginProvider.get(), "Setting null should effectively clear the provider");
    }

    @Test
    void testClearIsIdempotent() {
        JustPluginProvider.clear();
        JustPluginProvider.clear();
        assertNull(JustPluginProvider.get(), "Calling clear() multiple times should not throw or change state");
    }

    @Test
    void testMultipleSetClearCycles() {
        JustPluginAPI api1 = new DummyJustPluginAPI();
        JustPluginAPI api2 = new DummyJustPluginAPI();

        // Cycle 1
        JustPluginProvider.set(api1);
        assertSame(api1, JustPluginProvider.get());
        JustPluginProvider.clear();
        assertNull(JustPluginProvider.get());

        // Cycle 2
        JustPluginProvider.set(api2);
        assertSame(api2, JustPluginProvider.get());
        JustPluginProvider.clear();
        assertNull(JustPluginProvider.get());
    }

    @Test
    void testGetReturnsSameInstanceMultipleTimes() {
        JustPluginAPI mockApi = new DummyJustPluginAPI();
        JustPluginProvider.set(mockApi);
        assertSame(JustPluginProvider.get(), JustPluginProvider.get(),
                "Multiple calls to get() should return the same instance");
    }

    /**
     * Minimal dummy implementation of JustPluginAPI for testing purposes.
     * We don't need real implementations - just need an object reference.
     */
    private static class DummyJustPluginAPI implements JustPluginAPI {
        @Override public EconomyAPI getEconomyAPI() { return null; }
        @Override public PunishmentAPI getPunishmentAPI() { return null; }
        @Override public VanishAPI getVanishAPI() { return null; }
    }
}
