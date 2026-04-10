package org.justme.justPlugin.managers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for BanManager static utility methods.
 * These methods are pure functions that don't require a Bukkit server or plugin instance.
 */
class BanManagerTest {

    // ========================
    // isLocalIp tests
    // ========================

    @Test
    void testIsLocalIp_Loopback127() {
        assertTrue(BanManager.isLocalIp("127.0.0.1"));
    }

    @Test
    void testIsLocalIp_Loopback0000() {
        assertTrue(BanManager.isLocalIp("0.0.0.0"));
    }

    @Test
    void testIsLocalIp_Localhost() {
        assertTrue(BanManager.isLocalIp("localhost"));
    }

    @Test
    void testIsLocalIp_IPv6Loopback() {
        assertTrue(BanManager.isLocalIp("::1"));
    }

    @Test
    void testIsLocalIp_IPv6LoopbackExpanded() {
        assertTrue(BanManager.isLocalIp("0:0:0:0:0:0:0:1"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"192.168.0.1", "192.168.1.100", "192.168.255.255"})
    void testIsLocalIp_PrivateClassC(String ip) {
        assertTrue(BanManager.isLocalIp(ip));
    }

    @ParameterizedTest
    @ValueSource(strings = {"10.0.0.1", "10.255.255.255", "10.10.10.10"})
    void testIsLocalIp_PrivateClassA(String ip) {
        assertTrue(BanManager.isLocalIp(ip));
    }

    @ParameterizedTest
    @ValueSource(strings = {"172.16.0.1", "172.20.0.1", "172.31.255.255"})
    void testIsLocalIp_PrivateClassB(String ip) {
        assertTrue(BanManager.isLocalIp(ip));
    }

    @ParameterizedTest
    @ValueSource(strings = {"8.8.8.8", "1.1.1.1", "203.0.113.1", "172.32.0.1", "172.15.0.1", "11.0.0.1"})
    void testIsLocalIp_PublicAddresses(String ip) {
        assertFalse(BanManager.isLocalIp(ip));
    }

    @Test
    void testIsLocalIp_Null() {
        assertFalse(BanManager.isLocalIp(null));
    }

    // ========================
    // isInSubnet tests
    // ========================

    @Test
    void testIsInSubnet_ExactMatch() {
        assertTrue(BanManager.isInSubnet("192.168.1.1", "192.168.1.1/32"));
    }

    @Test
    void testIsInSubnet_ClassC() {
        assertTrue(BanManager.isInSubnet("192.168.1.50", "192.168.1.0/24"));
        assertTrue(BanManager.isInSubnet("192.168.1.255", "192.168.1.0/24"));
        assertTrue(BanManager.isInSubnet("192.168.1.0", "192.168.1.0/24"));
    }

    @Test
    void testIsInSubnet_ClassC_OutOfRange() {
        assertFalse(BanManager.isInSubnet("192.168.2.1", "192.168.1.0/24"));
    }

    @Test
    void testIsInSubnet_ClassB() {
        assertTrue(BanManager.isInSubnet("10.0.50.1", "10.0.0.0/16"));
        assertTrue(BanManager.isInSubnet("10.0.255.255", "10.0.0.0/16"));
        assertFalse(BanManager.isInSubnet("10.1.0.1", "10.0.0.0/16"));
    }

    @Test
    void testIsInSubnet_ClassA() {
        assertTrue(BanManager.isInSubnet("10.50.50.50", "10.0.0.0/8"));
        assertTrue(BanManager.isInSubnet("10.255.255.255", "10.0.0.0/8"));
        assertFalse(BanManager.isInSubnet("11.0.0.1", "10.0.0.0/8"));
    }

    @Test
    void testIsInSubnet_Slash0_MatchesEverything() {
        assertTrue(BanManager.isInSubnet("1.2.3.4", "0.0.0.0/0"));
        assertTrue(BanManager.isInSubnet("255.255.255.255", "0.0.0.0/0"));
    }

    @Test
    void testIsInSubnet_InvalidCidr_NoPrefixLength() {
        assertFalse(BanManager.isInSubnet("192.168.1.1", "192.168.1.0"));
    }

    @Test
    void testIsInSubnet_InvalidIp() {
        assertFalse(BanManager.isInSubnet("not.an.ip", "192.168.1.0/24"));
    }

    @Test
    void testIsInSubnet_SmallSubnet_Slash30() {
        // /30 = 4 IPs: .0, .1, .2, .3
        assertTrue(BanManager.isInSubnet("192.168.1.0", "192.168.1.0/30"));
        assertTrue(BanManager.isInSubnet("192.168.1.1", "192.168.1.0/30"));
        assertTrue(BanManager.isInSubnet("192.168.1.2", "192.168.1.0/30"));
        assertTrue(BanManager.isInSubnet("192.168.1.3", "192.168.1.0/30"));
        assertFalse(BanManager.isInSubnet("192.168.1.4", "192.168.1.0/30"));
    }

    @Test
    void testIsInSubnet_NonAlignedSubnet() {
        // /25 splits the last octet in half
        assertTrue(BanManager.isInSubnet("192.168.1.0", "192.168.1.0/25"));
        assertTrue(BanManager.isInSubnet("192.168.1.127", "192.168.1.0/25"));
        assertFalse(BanManager.isInSubnet("192.168.1.128", "192.168.1.0/25"));
    }
}
