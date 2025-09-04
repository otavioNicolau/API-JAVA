package com.pdfprocessor.api.service;

import com.pdfprocessor.api.exception.SecurityValidationException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service for managing API rate limiting per API key.
 * Implements a sliding window rate limiter with 100 requests per hour limit.
 */
@Service
public class RateLimitService {

    private static final int MAX_REQUESTS_PER_HOUR = 100;
    private static final int WINDOW_SIZE_MINUTES = 60;

    private final Clock clock;

    // Map to store request counts per API key
    private final ConcurrentHashMap<String, RequestWindow> requestWindows = new ConcurrentHashMap<>();

    public RateLimitService() {
        this.clock = Clock.systemDefaultZone();
    }

    // Constructor for testing
    public RateLimitService(Clock clock) {
        this.clock = clock;
    }

    /**
     * Checks if the API key has exceeded the rate limit.
     *
     * @param apiKey the API key to check
     * @throws SecurityValidationException if rate limit is exceeded
     */
    public void checkRateLimit(String apiKey) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            // Skip rate limit check for null/empty API keys
            // Authentication filter will handle this case
            return;
        }

        RequestWindow window = requestWindows.computeIfAbsent(apiKey, k -> new RequestWindow());
        
        synchronized (window) {
            LocalDateTime now = LocalDateTime.now(clock);
            
            // Reset window if it's older than 1 hour
            if (window.windowStart == null || 
                ChronoUnit.MINUTES.between(window.windowStart, now) >= WINDOW_SIZE_MINUTES) {
                window.reset(now);
            }
            
            // Check if limit is exceeded
            if (window.requestCount.get() >= MAX_REQUESTS_PER_HOUR) {
                long minutesUntilReset = WINDOW_SIZE_MINUTES - 
                    ChronoUnit.MINUTES.between(window.windowStart, now);
                throw new SecurityValidationException(
                    String.format("Rate limit exceeded. Try again in %d minutes.", minutesUntilReset),
                    "RATE_LIMIT_EXCEEDED"
                );
            }
            
            // Increment request count
            window.requestCount.incrementAndGet();
        }
    }

    /**
     * Gets the current request count for an API key.
     *
     * @param apiKey the API key
     * @return current request count in the current window
     */
    public int getCurrentRequestCount(String apiKey) {
        RequestWindow window = requestWindows.get(apiKey);
        if (window == null) {
            return 0;
        }
        
        synchronized (window) {
            LocalDateTime now = LocalDateTime.now(clock);
            
            // Return 0 if window is expired
            if (window.windowStart == null || 
                ChronoUnit.MINUTES.between(window.windowStart, now) >= WINDOW_SIZE_MINUTES) {
                return 0;
            }
            
            return window.requestCount.get();
        }
    }

    /**
     * Clears rate limit data for testing purposes.
     */
    public void clearRateLimitData() {
        requestWindows.clear();
    }

    /**
     * Internal class to track request windows per API key.
     */
    private static class RequestWindow {
        private LocalDateTime windowStart;
        private final AtomicInteger requestCount = new AtomicInteger(0);

        void reset(LocalDateTime newStart) {
            this.windowStart = newStart;
            this.requestCount.set(0);
        }
    }
}