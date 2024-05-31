package com.softwareag.controlplane.agentaws.metrics.utils;

import java.time.Instant;

/**
 * Utility functions that can used across different functions
 */
public final class Utility {
    private Utility(){

    }

    /**
     * Converts the given timestamp in epoch milliseconds to an Instant.
     *
     * @param timestamp The timestamp in epoch milliseconds.
     * @return An Instant representing the epoch timestamp.
     */
    public static Instant getInstant(long timestamp) {
        return Instant.ofEpochMilli(timestamp);
    }

    /**
     * Aligns the given epoch timestamp with the specified interval by rounding it down to the nearest interval boundary.
     *
     * @param epochTimestamp The epoch timestamp to align.
     * @param interval The interval, in seconds, to align the timestamp with.
     * @return An Instant representing the epoch timestamp aligned with the specified interval.
     */
    public static Instant alignTimestampsWithInterval(long epochTimestamp, long interval) {
        Instant timestamp = Utility.getInstant(epochTimestamp);
        long remainder = timestamp.toEpochMilli() % (interval * 1000);
        long alignedEpochTimestampMillis = ((epochTimestamp - remainder) / 1000) * 1000;
        return Instant.ofEpochMilli(alignedEpochTimestampMillis);
    }

    /**
     * Aligns the given Instant with the specified interval by rounding it down to the nearest interval boundary.
     *
     * @param timestamp The Instant to align.
     * @param interval The interval, in seconds, to align the timestamp with.
     * @return An Instant aligned with the specified interval.
     */
    public static Instant alignTimestampsWithInterval(Instant timestamp, long interval) {
        long remainder = timestamp.toEpochMilli() % (interval * 1000);
        long alignedEpochTimestampMillis = timestamp.toEpochMilli() - remainder;
        return Instant.ofEpochMilli(alignedEpochTimestampMillis);
    }

    /**
     * Reduces the given epoch time by the specified number of seconds.
     *
     * @param epochTimeInMillis The epoch time in milliseconds.
     * @param secondsToReduce   The number of seconds to reduce from the epoch time.
     * @return The reduced epoch time in milliseconds.
     */
    public static long reduceEpochTime(long epochTimeInMillis, int secondsToReduce) {
        long epochTimeInSeconds = epochTimeInMillis / 1000;

        long reducedEpochTimeInSeconds = epochTimeInSeconds - secondsToReduce;

        return reducedEpochTimeInSeconds * 1000;
    }
}
