package com.softwareag.controlplane.agentaws.metrics.manager;

import com.softwareag.controlplane.agentsdk.model.Metrics;

import java.util.List;

/**
 * Interface to make calls to Cloudwatch
 * We will be using cloudwatch to fetch metrics for AWS API Gateway
 */
public interface MetricsManager {

    /**
     * Retrieves a list of metrics associated with each sync interval between the specified time range.
     *
     * @param fromTimestamp The start time of the metrics range (inclusive).
     * @param toTimestamp The end time of the metrics range (exclusive).
     * @param interval The interval, in milliseconds, at which metrics are synchronized.
     * @param bufferTime The assumed time, in seconds, that a metric takes to register to CloudWatch after it occurred.
     * @return A list of Metrics objects representing the metrics associated with each sync interval between fromTimestamp (inclusive) and toTimestamp (exclusive).
     */
    List<Metrics> getMetrics(long fromTimestamp, long toTimestamp, long interval, int bufferTime);
}
