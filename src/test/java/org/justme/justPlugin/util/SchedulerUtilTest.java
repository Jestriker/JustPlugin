package org.justme.justPlugin.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for SchedulerUtil pure utility methods.
 * Most SchedulerUtil methods require Bukkit, but we can test
 * the Folia detection and the CancellableTask interface.
 */
class SchedulerUtilTest {

    @Test
    void testIsFolia_ReturnsFalseInTestEnvironment() {
        // In a test environment without Folia, this should be false
        assertFalse(SchedulerUtil.isFolia(),
                "isFolia() should return false when Folia classes are not on the classpath");
    }

    @Test
    void testIsFolia_IsConsistent() {
        // Multiple calls should return the same value (static final field)
        boolean first = SchedulerUtil.isFolia();
        boolean second = SchedulerUtil.isFolia();
        assertEquals(first, second, "isFolia() should return consistent results");
    }

    @Test
    void testCancellableTask_FunctionalInterface() {
        // CancellableTask is a @FunctionalInterface - verify it works as a lambda
        boolean[] cancelled = {false};
        SchedulerUtil.CancellableTask task = () -> cancelled[0] = true;
        assertFalse(cancelled[0]);
        task.cancel();
        assertTrue(cancelled[0], "CancellableTask.cancel() should invoke the lambda");
    }

    @Test
    void testCancellableTask_MultipleCancelCalls() {
        int[] count = {0};
        SchedulerUtil.CancellableTask task = () -> count[0]++;
        task.cancel();
        task.cancel();
        task.cancel();
        assertEquals(3, count[0], "CancellableTask.cancel() should be callable multiple times");
    }

    @Test
    void testCancellableTask_NoOp() {
        // A no-op cancellable task should not throw
        SchedulerUtil.CancellableTask task = () -> {};
        assertDoesNotThrow(task::cancel);
    }

    @Test
    void testCancellableTask_AsMethodReference() {
        // Verify it works with method references too
        StringBuilder sb = new StringBuilder();
        SchedulerUtil.CancellableTask task = sb::trimToSize;
        assertDoesNotThrow(task::cancel);
    }
}
