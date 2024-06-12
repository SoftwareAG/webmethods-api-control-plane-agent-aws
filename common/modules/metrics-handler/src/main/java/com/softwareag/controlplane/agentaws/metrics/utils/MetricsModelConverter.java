package com.softwareag.controlplane.agentaws.metrics.utils;

import com.softwareag.controlplane.agentaws.metrics.constants.Constants;
import com.softwareag.controlplane.agentsdk.model.API;
import com.softwareag.controlplane.agentsdk.model.APIMetrics;
import com.softwareag.controlplane.agentsdk.model.APITransactionMetrics;
import com.softwareag.controlplane.agentsdk.model.Metrics;
import com.softwareag.controlplane.agentsdk.model.RuntimeTransactionMetrics;


import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Model converter to convert from Cloudwatch results to Agent SDK Metrics model
 */
public final class MetricsModelConverter {
    private MetricsModelConverter() {

    }

    /**
     * Creates an instance of APITransactionMetrics.
     *
     * @param api The API associated with the metrics.
     * @param transactionCount The total count of transactions.
     * @param latency The average overall latency of transactions(in AWS API Gateway).
     * @param averageLatency The average AWS API gateway(only gateway overhead) latency.
     * @param integrationLatency The integration latency of transactions(in AWS API Gateway).
     * @param clientError The count of client errors.
     * @param serverError The count of server errors.
     * @return An instance of APITransactionMetrics containing the provided metrics.
     */
    public static APITransactionMetrics createAPITransactionMetrics(API api, Double transactionCount, Double latency,
                                                                    Double averageLatency, Double integrationLatency,
                                                                    Double clientError, Double serverError) {

        APIMetrics apiMetrics = new APIMetrics.Builder(transactionCount.longValue())
                .averageGatewayLatency(averageLatency.floatValue())
                .averageTotalLatency(latency.floatValue())
                .averageBackendLatency(integrationLatency.floatValue()).build();
        APITransactionMetrics apiTransactionMetrics = new APITransactionMetrics.Builder(apiMetrics, api.getId(),
                api.getName(), api.getVersion()).build();
        Map<String, APIMetrics> metricsBysStatusCode = new HashMap<>();
        metricsBysStatusCode.put(Constants.CP_METRIC_CLIENT_ERROR, getAPIMetricsForStatusCode(clientError.longValue()));
        metricsBysStatusCode.put(Constants.CP_METRIC_SERVER_ERROR, getAPIMetricsForStatusCode(serverError.longValue()));
        apiTransactionMetrics.setMetricsByStatusCode(metricsBysStatusCode);
        return apiTransactionMetrics;
    }

    /**
     * Creates an instance of Metrics based on the provided timestamp and list of API transaction metrics.
     *
     * @param timestamp The timestamp associated with the metrics.
     * @param apiTransactionMetricsList The list of API transaction metrics.
     * @return An instance of Metrics containing the provided timestamp and API transaction metrics.
     */
    public static Metrics createMetrics(long timestamp, List<APITransactionMetrics> apiTransactionMetricsList) {
        return new Metrics.Builder()
                .apiTransactionMetricsList(apiTransactionMetricsList)
                .runtimeTransactionMetrics(createRuntimeMetrics(apiTransactionMetricsList))
                .timestamp(timestamp).build();
    }

    /**
     * Creates an instance of Metrics representing a point in time with no transactions.
     *
     * @param timestamp The timestamp associated with the metrics.
     * @return An instance of Metrics with no transactions and the provided timestamp.
     */
    public static Metrics createNoTransanctionMetrics(long timestamp) {
        List<APITransactionMetrics> apiTransactionMetricsList = new ArrayList<>();
        APIMetrics apiMetrics = new APIMetrics.Builder(0L)
                .averageBackendLatency((float) 0)
                .averageGatewayLatency((float) 0)
                .averageTotalLatency((float) 0)
                .build();

        return new Metrics.Builder()
                .apiTransactionMetricsList(apiTransactionMetricsList)
                .runtimeTransactionMetrics(new RuntimeTransactionMetrics.Builder(apiMetrics).build())
                .timestamp(timestamp).build();
    }

    /**
     * Calculates the runtime transaction metrics based on the provided list of API transaction metrics.
     * The runtime transaction count is the sum of all APIs' transaction counts in the list.
     * The runtime latency is the average of all APIs' latencies in the list.
     * The runtime average backend response is the average backend response of all APIs in the list.
     * The runtime response time is the average response time of all APIs in the list.
     * The runtime client error is the sum of all the APIs' client errors in the list.
     * The runtime server error is the sum of all the APIs' server errors in the list.
     *
     * @param apiTransactionMetricsList List of all API transaction metrics in that runtime.
     * @return RuntimeTransactionMetrics for the given list of API transaction metrics.
     */
    public static RuntimeTransactionMetrics createRuntimeMetrics(List<APITransactionMetrics> apiTransactionMetricsList) {

        long totalTransactionCount = 0;
        float totalLatencyOfAllApis = 0;
        float totalBackendResponseTimeOfAllApis = 0;
        float totalResponseTimeOfAllApis = 0;
        long clientError = 0;
        long serverError = 0;

        for (APITransactionMetrics apiTransactionMetrics : apiTransactionMetricsList) {
            long currentApiTransactionCount = apiTransactionMetrics.getApiMetrics().getTransactionCount();
            totalTransactionCount += currentApiTransactionCount;
            totalLatencyOfAllApis += apiTransactionMetrics.getApiMetrics().getAverageGatewayLatency() * currentApiTransactionCount;
            totalBackendResponseTimeOfAllApis += apiTransactionMetrics.getApiMetrics().getAverageBackendLatency() * currentApiTransactionCount;
            totalResponseTimeOfAllApis += apiTransactionMetrics.getApiMetrics().getAverageTotalLatency() * currentApiTransactionCount;
            clientError += apiTransactionMetrics.getMetricsByStatusCode().get(Constants.CP_METRIC_CLIENT_ERROR).getTransactionCount();
            serverError += apiTransactionMetrics.getMetricsByStatusCode().get(Constants.CP_METRIC_SERVER_ERROR).getTransactionCount();
        }

        APIMetrics runtimeMetrics = new APIMetrics.Builder(totalTransactionCount)
                .averageGatewayLatency(totalLatencyOfAllApis != 0 && totalTransactionCount != 0 ? totalLatencyOfAllApis / totalTransactionCount : 0)
                .averageBackendLatency(totalBackendResponseTimeOfAllApis != 0 && totalTransactionCount != 0 ? totalBackendResponseTimeOfAllApis / totalTransactionCount : 0)
                .averageTotalLatency(totalResponseTimeOfAllApis != 0 && totalTransactionCount != 0 ? totalResponseTimeOfAllApis / totalTransactionCount : 0).build();

        RuntimeTransactionMetrics runtimeTransactionMetrics = new RuntimeTransactionMetrics.Builder(runtimeMetrics).build();
        Map<String, APIMetrics> metricsByStatusCode = new HashMap<>();
        metricsByStatusCode.put(Constants.CP_METRIC_CLIENT_ERROR, getAPIMetricsForStatusCode(clientError));
        metricsByStatusCode.put(Constants.CP_METRIC_SERVER_ERROR, getAPIMetricsForStatusCode(serverError));
        runtimeTransactionMetrics.setMetricsByStatusCode(metricsByStatusCode);

        return runtimeTransactionMetrics;

    }

    /**
     * Creates an instance of APIMetrics with the count metric alone for a specific status code.
     *
     * @param count The count metric for the status code.
     * @return An instance of APIMetrics with the provided count metric for the status code.
     */
    private static APIMetrics getAPIMetricsForStatusCode(Long count) {
        return new APIMetrics.Builder(count).build();
    }

    /**
     * Updates a map of API metrics with the provided API transaction metrics for a specific timestamp.
     * If the map does not contain an entry for the given timestamp, a new entry is created.
     *
     * @param apiMetricsMap The map containing API metrics, keyed by timestamp.
     * @param apiTransactionMetrics The API transaction metrics to be added to the map.
     * @param timeStamp The timestamp associated with the API transaction metrics.
     */
    public static void updateMetricsMap(Map<Long, List<APITransactionMetrics>> apiMetricsMap,
                                        APITransactionMetrics apiTransactionMetrics, Instant timeStamp) {
        long timestamp = timeStamp.toEpochMilli();
        apiMetricsMap.computeIfAbsent(timestamp, k -> new ArrayList<>());
        apiMetricsMap.get(timestamp).add(apiTransactionMetrics);
    }
}
