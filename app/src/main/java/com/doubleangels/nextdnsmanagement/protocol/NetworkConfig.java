package com.doubleangels.nextdnsmanagement.protocol;

import android.content.Context;
import okhttp3.ConnectionPool;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import android.app.ActivityManager;
import java.util.concurrent.TimeUnit;
import java.io.IOException;

public class NetworkConfig {
    private static final int CONNECTION_TIMEOUT = 10;
    private static final int READ_TIMEOUT = 10;
    private static final int WRITE_TIMEOUT = 10;
    private static final int MAX_RETRIES = 3;
    private static final int MAX_POOL_SIZE = 5;
    private final Context context;

    public NetworkConfig(Context context) {
        this.context = context;
    }

    public OkHttpClient createOptimizedClient() {
        ConnectionPool connectionPool = new ConnectionPool(
            MAX_POOL_SIZE,
            5,
            TimeUnit.MINUTES
        );

        return new OkHttpClient.Builder()
            .connectTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .connectionPool(connectionPool)
            .addInterceptor(createRetryInterceptor())
            .addInterceptor(createMemoryManagementInterceptor(connectionPool))
            .build();
    }

    private Interceptor createRetryInterceptor() {
        return chain -> {
            int retryCount = 0;
            IOException lastException = null;
            
            while (retryCount < MAX_RETRIES) {
                try {
                    return chain.proceed(chain.request());
                } catch (IOException e) {
                    lastException = e;
                    retryCount++;
                    
                    if (e.getMessage() != null && 
                        e.getMessage().contains("CONNECT: 500")) {
                        throw e;
                    }
                    
                    if (retryCount == MAX_RETRIES) {
                        throw e;
                    }
                    
                    try {
                        Thread.sleep(1000L * retryCount);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw lastException;
                    }
                }
            }
            throw lastException;
        };
    }

    private Interceptor createMemoryManagementInterceptor(ConnectionPool connectionPool) {
        return chain -> {
            ActivityManager activityManager = (ActivityManager) 
                context.getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
            activityManager.getMemoryInfo(memoryInfo);
            
            if (memoryInfo.lowMemory) {
                connectionPool.evictAll();
            }
            return chain.proceed(chain.request());
        };
    }
}