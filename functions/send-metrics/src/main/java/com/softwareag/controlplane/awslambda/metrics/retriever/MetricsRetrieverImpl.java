package com.softwareag.controlplane.awslambda.metrics.retriever;

import com.softwareag.controlplane.agentaws.metrics.manager.impl.MetricsManagerImpl;
import com.softwareag.controlplane.agentaws.util.constants.Constants;
import com.softwareag.controlplane.agentaws.util.provider.EnvProvider;
import com.softwareag.controlplane.agentsdk.core.handler.SendMetricsHandler;
import com.softwareag.controlplane.agentsdk.model.Metrics;

import java.util.List;

public class MetricsRetrieverImpl implements SendMetricsHandler.MetricsRetriever {
    @Override
    public List<Metrics> getMetrics(long l, long l1, long l2) {
        return MetricsManagerImpl.getInstance(EnvProvider.getEnv(Constants.AWS_REGION), EnvProvider.getEnv(Constants.AWS_STAGE),
                        EnvProvider.getEnv(Constants.AWS_METRICS_BY_DATA_OR_STATISTICS))
                .getMetrics(l,l1,l2, Integer.parseInt(EnvProvider.getEnv(Constants.AWS_METRICS_SYNC_BUFFER_TIME)));
    }
}
