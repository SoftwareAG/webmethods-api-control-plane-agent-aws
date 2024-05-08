package com.softwareag.controlplane.agentaws.metrics.manager.impl;

import com.softwareag.controlplane.agentaws.assets.manager.AssetsManager;
import com.softwareag.controlplane.agentaws.assets.manager.impl.AssetsManagerImpl;
import com.softwareag.controlplane.agentaws.metrics.client.AWSClientManager;
import com.softwareag.controlplane.agentaws.metrics.constants.Constants;
import com.softwareag.controlplane.agentaws.metrics.manager.MetricsManager;
import com.softwareag.controlplane.agentaws.metrics.utils.CloudWatchUtil;
import com.softwareag.controlplane.agentaws.metrics.utils.MetricsModelConverter;
import com.softwareag.controlplane.agentaws.metrics.utils.Utility;
import com.softwareag.controlplane.agentsdk.model.API;
import com.softwareag.controlplane.agentsdk.model.APITransactionMetrics;
import com.softwareag.controlplane.agentsdk.model.Metrics;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.Datapoint;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricDataRequest;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricDataResponse;
import software.amazon.awssdk.services.cloudwatch.model.MetricDataResult;
import software.amazon.awssdk.services.cloudwatch.model.Statistic;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricStatisticsRequest;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricStatisticsResponse;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.Comparator;

import static com.softwareag.controlplane.agentaws.metrics.utils.MetricsModelConverter.updateMetricsMap;

public class MetricsManagerImpl implements MetricsManager {
    private static MetricsManagerImpl cloudWatchManager;
    private final String stage;
    private final String getMetricsByData;
    
    private CloudWatchClient cloudWatchClient;
    
    private AssetsManager assetsManager;
    
    private MetricsManagerImpl(String region, String stage, String getMetricsByData) {
        this.stage = stage;
        this.getMetricsByData = getMetricsByData;
        this.cloudWatchClient = AWSClientManager.getInstance(region).cloudWatchClient();
        this.assetsManager = AssetsManagerImpl.getInstance(region);
    }

    /**
     * Returns an instance of MetricsManagerImpl using the provided region, stage, and method. Initializing it if necessary.
     *
     * @param region The AWS region.
     * @param stage The stage for which the agent is configured.
     * @param getMetricsByData The method to retrieve data points from CloudWatch. It can either be 'data' or 'statistics'(default).
     * @return An instance of MetricsManagerImpl with the provided configuration.
     */
    public static MetricsManagerImpl getInstance(String region, String stage, String getMetricsByData) {
        if(cloudWatchManager != null) {
            return cloudWatchManager;
        }
        cloudWatchManager = new MetricsManagerImpl(region, stage, getMetricsByData);
        return cloudWatchManager;
    }

    /**
     * Retrieves a list of metrics associated with each sync interval between the specified time range.
     *
     * @param fromTimestamp The start time of the metrics range (inclusive).
     * @param toTimestamp The end time of the metrics range (exclusive).
     * @param interval The interval, in milliseconds, at which metrics are synchronized.
     * @param bufferSeconds The assumed time, in seconds, that a metric takes to register to CloudWatch after it occurred.
     * @return A list of Metrics objects representing the metrics associated with each sync interval between fromTimestamp (inclusive) and toTimestamp (exclusive).
     */
    @Override
    public List<Metrics> getMetrics(long fromTimestamp, long toTimestamp, long interval, int bufferSeconds) {
        List<API> apis = assetsManager.getRestAPIs(stage, false); // get APIs for the configured stage

        long bufferedFromTimestamp = Utility.reduceEpochTime(fromTimestamp, bufferSeconds);
        long bufferedToTimestamp = Utility.reduceEpochTime(toTimestamp, bufferSeconds);

        Instant startTime = Utility.alignTimestampsWithInterval(bufferedFromTimestamp, interval);
        Instant endTime = Utility.alignTimestampsWithInterval(bufferedToTimestamp, interval);

        if(toTimestamp == 0) {
            endTime = Utility.alignTimestampsWithInterval(Instant.now(), interval);
        }
        if(fromTimestamp == 0 || startTime.isAfter(endTime.minusSeconds(interval))) {
            startTime = endTime.minusSeconds(interval);
        }

        // Map with timestamp as a key and list of APITransactionMetrics for that key
        Map<Long, List<APITransactionMetrics>> metricsMap = new HashMap<>();
        List<Metrics> metricsList = new ArrayList<>();
        if (getMetricsByData.equals("data")) {
            for (API api : apis) {
                getByMetricData(api, metricsMap, startTime, endTime, (int) interval);
            }
        } else {
            for (API api : apis) {
                getByGetMetricStatistics(api, metricsMap, startTime, endTime, (int) interval);
            }
        }

        // iterating through map and create Metrics object
        metricsMap.forEach((key, value) -> {
            Metrics metrics = MetricsModelConverter.createMetrics(key, value);
            metricsList.add(metrics);
        });

        //missing metric intervals should be sent with no transaction runtime metrics
        long currentTimestamp = startTime.toEpochMilli();
        long endTimeStamp = endTime.toEpochMilli();

        while(currentTimestamp < endTimeStamp) {
            if(!metricsMap.containsKey(currentTimestamp)){
                metricsList.add(MetricsModelConverter.createNoTransanctionMetrics(currentTimestamp));
            }
            currentTimestamp += (interval * 1000);
        }

        Collections.sort(metricsList, Comparator.comparingLong(Metrics::getTimestamp));
        return metricsList;
    }

    /**
     * get metrics from cloudwatch using GetMetricData method
     */
    private void getByMetricData(API api, Map<Long, List<APITransactionMetrics>> metricsMap, Instant startTime,
                                 Instant endTime, int interval) {


        List<Dimension> dimensions = CloudWatchUtil.createDimensions(stage, api.getName());
        List<MetricDataResult> metricDataResults = getResultsByMetricData(startTime, endTime, dimensions, interval);
        MetricDataResult countResults = null;
        MetricDataResult latencyResults = null;
        MetricDataResult integrationLatencyResults = null;
        MetricDataResult clientErrorResults = null;
        MetricDataResult serverErrorResults = null;

        for (MetricDataResult metricDataResult : metricDataResults) {
            if (Constants.AWS_METRIC_COUNT_ID.equals(metricDataResult.id())) {
                countResults = metricDataResult;
            } else if (Constants.AWS_METRIC_LATENCY_ID.equals(metricDataResult.id())) {
                latencyResults = metricDataResult;
            } else if (Constants.AWS_METRIC_INTEGRATION_LATENCY_ID.equals(metricDataResult.id())) {
                integrationLatencyResults = metricDataResult;
            } else if (Constants.AWS_METRIC_CLIENT_ERROR_ID.equals(metricDataResult.id())) {
                clientErrorResults = metricDataResult;
            } else {
                serverErrorResults = metricDataResult;
            }
        }

        if(countResults != null) {
            for (int i = 0; i < countResults.values().size(); i++) {
                Double count = countResults.values().get(i);
                Double latency = latencyResults.values().size() > i ? latencyResults.values().get(i) : 0;
                Double integrationLatency = integrationLatencyResults.values().size() > i ? integrationLatencyResults.values().get(i) : 0;
                Double averageLatency = latency - integrationLatency > 0 ? latency - integrationLatency : 0;
                Double clientError = clientErrorResults.values().size() > i ? clientErrorResults.values().get(i) : 0;
                Double serverError = serverErrorResults.values().size() > i ? serverErrorResults.values().get(i) : 0;
                APITransactionMetrics apiTransactionMetrics = MetricsModelConverter.createAPITransactionMetrics(api, count, latency, averageLatency, integrationLatency, clientError, serverError);
                Instant timeStamp = countResults.timestamps().get(i);
                updateMetricsMap(metricsMap, apiTransactionMetrics, timeStamp);
            }
        }
    }

    /**
     * get metrics from cloudwatch using GetMetricData method
     *
     * @return List of MetricDataResult, each MetricDataResult corresponding to one metric
     */
    private List<MetricDataResult> getResultsByMetricData(Instant startTime, Instant endTime, List<Dimension> dimensions,
                                                          int interval) {

        GetMetricDataRequest getMetReq = GetMetricDataRequest.builder()
                .startTime(startTime)
                .endTime(endTime)
                .metricDataQueries(CloudWatchUtil.createMetricDataQueries(dimensions, interval))
                .build();
        GetMetricDataResponse response = cloudWatchClient.getMetricData(getMetReq);
        return response.metricDataResults();
    }

    /**
     * get metrics from cloudwatch using GetMetricStatistics method
     */
    private void getByGetMetricStatistics(API api, Map<Long, List<APITransactionMetrics>> metricsMap,
                                          Instant startTime, Instant endTime, int interval) {
        List<Dimension> dimensions = CloudWatchUtil.createDimensions(stage, api.getName());
        List<Datapoint> countDataPoints = getCountMetrics(startTime, endTime, dimensions, interval);
        List<Datapoint> latencyDataPoints = getLatencyMetrics(startTime, endTime, dimensions, interval);
        List<Datapoint> integrationLatencyPoints = getIntegrationLatencyMetrics(startTime, endTime, dimensions, interval);
        List<Datapoint> clientErrorDataPoints = get4XXErrorCount(startTime, endTime, dimensions, interval);
        List<Datapoint> serverErrorDataPoints = get5XXErrorCount(startTime, endTime, dimensions, interval);

        for (int i = 0; i < countDataPoints.size(); i++) {
            Double count = countDataPoints.get(i).sum();
            Double latency = latencyDataPoints.get(i).average();
            Double integrationLatency = integrationLatencyPoints.get(i).average();
            Double averageLatency = latency - integrationLatency > 0 ? latency - integrationLatency : 0;
            Double clientError = clientErrorDataPoints.get(i).sum();
            Double serverError = serverErrorDataPoints.get(i).sum();
            APITransactionMetrics apiTransactionMetrics = MetricsModelConverter.createAPITransactionMetrics(api
                    , count, latency, averageLatency, integrationLatency, clientError, serverError);
            Instant timeStamp = countDataPoints.get(i).timestamp();
            updateMetricsMap(metricsMap, apiTransactionMetrics, timeStamp);
        }
    }


    /**
     * get Count Metrics by GetMetricStatistics method
     */
    private List<Datapoint> getCountMetrics(Instant startTime, Instant endTime, List<Dimension> dimensions, int interval) {
        return getResultsByMetricStatistics(startTime, endTime, dimensions, interval, Constants.AWS_METRIC_COUNT, Statistic.SUM);
    }

    /**
     * get Latency Metrics by GetMetricStatistics method
     */
    private List<Datapoint> getLatencyMetrics(Instant startTime, Instant endTime, List<Dimension> dimensions, int interval) {
        return getResultsByMetricStatistics(startTime, endTime, dimensions, interval, Constants.AWS_METRIC_LATENCY, Statistic.AVERAGE);
    }

    /**
     * get IntegrationLatency Metrics by GetMetricStatistics method
     */
    private List<Datapoint> getIntegrationLatencyMetrics(Instant startTime, Instant endTime, List<Dimension> dimensions,
                                                         int interval) {
        return getResultsByMetricStatistics(startTime, endTime, dimensions, interval, Constants.AWS_METRIC_INTEGRATION_LATENCY,
                Statistic.AVERAGE);
    }

    /**
     * get 4xx Metrics by GetMetricStatistics method
     */
    private List<Datapoint> get4XXErrorCount(Instant startTime, Instant endTime, List<Dimension> dimensions, int interval) {
        return getResultsByMetricStatistics(startTime, endTime, dimensions, interval, Constants.AWS_METRIC_CLIENT_ERROR
                , Statistic.SUM);
    }

    /**
     * get 5xx Metrics by GetMetricStatistics method
     */
    private List<Datapoint> get5XXErrorCount(Instant startTime, Instant endTime, List<Dimension> dimensions, int interval) {

        return getResultsByMetricStatistics(startTime, endTime, dimensions, interval, Constants.AWS_METRIC_SERVER_ERROR
                , Statistic.SUM);
    }


    /**
     * get datapoints for a metric by GetMetricStatistics mthod
     *
     * @return list of datapoint for a single metric
     */
    private List<Datapoint> getResultsByMetricStatistics(Instant startTime, Instant endTime, List<Dimension> dimensions,
                                                         int interval, String metricName, Statistic statistic) {
        GetMetricStatisticsRequest request = GetMetricStatisticsRequest.builder()
                .startTime(startTime)
                .endTime(endTime)
                .metricName(metricName)
                .namespace(Constants.CLOUDWATCH_AWS_NAMESPACE)
                .dimensions(dimensions)
                .period(interval)
                .statistics(statistic)
                .build();

        GetMetricStatisticsResponse response = cloudWatchClient.getMetricStatistics(request);
        return response.datapoints();
    }


}
