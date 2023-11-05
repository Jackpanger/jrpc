package com.jackpang.utils;

import com.jackpang.exceptions.NetworkException;
import lombok.extern.slf4j.Slf4j;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * description: NetUtils
 * date: 11/4/23 6:06 PM
 * author: jinhao_pang
 * version: 1.0
 */
@Slf4j
public class NetUtils {
    public static String getIp() throws NetworkException {
        try {
            // Get the network interface
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                // Filter out loopback interface, virtual interface and down interface
                if (iface.isLoopback() || iface.isVirtual() || !iface.isUp()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    // Filter out IPv6 address and loopback address
                    if (addr instanceof Inet6Address || addr.isLoopbackAddress()) {
                        continue;
                    }
                    String ipAddress = addr.getHostAddress();
                    if (log.isDebugEnabled()) {
                        log.debug("Local network address：{}", ipAddress);
                    }
                    return ipAddress;
                }
            }
            throw new NetworkException();
        } catch (SocketException e) {
            log.error("Get local network address failed", e);
            throw new NetworkException(e);
        }
    }

    public static void main(String[] args) {
        String ip = NetUtils.getIp();
        System.out.println("ip = " + ip);
    }
}
