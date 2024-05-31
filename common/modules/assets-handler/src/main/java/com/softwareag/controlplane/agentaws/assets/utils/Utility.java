package com.softwareag.controlplane.agentaws.assets.utils;

import java.time.Instant;

/**
 * Utility functions that can used across different functions
 */
public final class Utility {
    private Utility(){

    }

    /**
     * Converts a timestamp from epoch milliseconds to an Instant.
     *
     * @param timestamp The timestamp in epoch milliseconds
     * @return An Instant representing the epoch timestamp.
     */
    public static Instant getInstant(long timestamp) {
        return Instant.ofEpochMilli(timestamp);
    }
}
