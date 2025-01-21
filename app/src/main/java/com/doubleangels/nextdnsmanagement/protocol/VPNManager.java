package com.doubleangels.nextdnsmanagement.protocol;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;

public class VPNManager {
    private final Context context;

    public VPNManager(Context context) {
        this.context = context;
    }

    public boolean isVPNActive() {
        ConnectivityManager cm = (ConnectivityManager) 
            context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network activeNetwork = cm.getActiveNetwork();
        
        if (activeNetwork != null) {
            NetworkCapabilities capabilities = 
                cm.getNetworkCapabilities(activeNetwork);
            if (capabilities != null) {
                return capabilities.hasTransport(
                    NetworkCapabilities.TRANSPORT_VPN);
            }
        }
        return false;
    }

    public void executeWithoutVPN(Runnable action) {
        NetworkRequest.Builder builder = new NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN);
        
        NetworkRequest request = builder.build();
        ConnectivityManager cm = (ConnectivityManager)
            context.getSystemService(Context.CONNECTIVITY_SERVICE);

        ConnectivityManager.NetworkCallback networkCallback = 
            new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(Network network) {
                    try {
                        // Bind this specific request to the non-VPN network
                        cm.bindProcessToNetwork(network);
                        
                        // Execute the provided action
                        action.run();
                    } finally {
                        // Reset the network binding
                        cm.bindProcessToNetwork(null);
                        
                        // Unregister the callback
                        cm.unregisterNetworkCallback(this);
                    }
                }
            };
        cm.requestNetwork(request, networkCallback);
    }
}