package com.softwareag.controlplane.awslambda.metrics.retriever;

import com.softwareag.controlplane.agentaws.metrics.manager.impl.MetricsManagerImpl;
import com.softwareag.controlplane.awslambda.util.constants.Constants;
import com.softwareag.controlplane.awslambda.util.provider.EnvProvider;
import com.softwareag.controlplane.agentsdk.core.handler.SendMetricsHandler;
import com.softwareag.controlplane.agentsdk.model.Metrics;

import java.util.List;

/**
 * Implementation of the MetricsRetriever interface for retrieving metrics.
 * This implementation is intended to be used with the SendMetricsHandler class.
 */
public class MetricsRetrieverImpl implements SendMetricsHandler.MetricsRetriever {

    /**
     * Retrieves runtime metrics within a specified time range and interval.
     * @param l start time to fetch metrics
     * @param l1 end time to fetch metrics
     * @param l2 time gap between each fetch of metrics within the specified start and end times
     * @return Returns a list of {@link Metrics} captured within the specified time range and intervals
     */
    @Override
    public List<Metrics> getMetrics(long l, long l1, long l2) {
        return MetricsManagerImpl.getInstance(EnvProvider.getEnv(Constants.AWS_REGION), EnvProvider.getEnv(Constants.AWS_STAGE),
                        EnvProvider.getEnv(Constants.AWS_METRICS_BY_DATA_OR_STATISTICS))
                .getMetrics(l,l1,l2, Integer.parseInt(EnvProvider.getEnv(Constants.AWS_METRICS_SYNC_BUFFER_INTERVAL_SECONDS)));
    }
}
