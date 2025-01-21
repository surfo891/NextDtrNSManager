package com.doubleangels.nextdnsmanagement.utils;

import androidx.annotation.NonNull;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class DNSResolver {
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 1000;
    private static final String BASE_DOMAIN = "test.nextdns.io";
    
    /**
     * Validates if the provided hostname is a valid NextDNS subdomain
     *
     * @param hostname The hostname to validate
     * @return boolean indicating if the hostname is valid
     */
    public static boolean isValidNextDNSSubdomain(String hostname) {
        if (hostname == null || hostname.isEmpty()) {
            return false;
        }
        
        // Validate that it's a subdomain of test.nextdns.io
        if (!hostname.endsWith(BASE_DOMAIN)) {
            return false;
        }
        
        // For subdomains, validate the format (alphanumeric characters only)
        if (hostname.length() > BASE_DOMAIN.length() + 1) {
            String subdomain = hostname.substring(0, hostname.length() - BASE_DOMAIN.length() - 1);
            return subdomain.matches("^[a-zA-Z0-9]+$");
        }
        
        return hostname.equals(BASE_DOMAIN);
    }
    
    /**
     * Attempts to resolve a hostname with retries
     *
     * @param hostname The hostname to resolve
     * @return boolean indicating if resolution was successful
     */
    public static boolean resolveWithRetry(@NonNull String hostname) {
        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            try {
                InetAddress.getAllByName(hostname);
                return true;
            } catch (UnknownHostException e) {
                if (attempt < MAX_RETRIES - 1) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS * (attempt + 1));
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return false;
                    }
                }
            }
        }
        return false;
    }
}