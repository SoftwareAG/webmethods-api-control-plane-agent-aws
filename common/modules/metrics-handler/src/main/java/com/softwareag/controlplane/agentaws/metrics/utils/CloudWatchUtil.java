package com.softwareag.controlplane.agentaws.metrics.utils;

import com.softwareag.controlplane.agentaws.metrics.constants.Constants;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;
import software.amazon.awssdk.services.cloudwatch.model.MetricDataQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * Util class for Cloudwatch
 */
public final class CloudWatchUtil {
    private CloudWatchUtil() {

    }

    /**
     * Creates a list of dimensions for querying metrics by API name and stage.
     *
     * @param stage The configured stage.
     * @param apiName The API name.
     * @return A list of dimensions, including the stage and API name.
     */
    public static List<Dimension> createDimensions(String stage, String apiName) {
        Dimension stageDimension = Dimension.builder().name(Constants.STAGE).value(stage).build();
        Dimension apiNameDimension = Dimension.builder().name(Constants.API_NAME).value(apiName).build();
        List<Dimension> dimensions = new ArrayList<>();
        dimensions.add(stageDimension);
        dimensions.add(apiNameDimension);
        return dimensions;
    }

    /**
     * Creates a list of MetricDataQuery objects for querying metrics data based on the provided dimensions and interval.
     *
     * @param dimensions List of dimensions, including stage and API name.
     * @param interval The period of time over which the metrics are aggregated, in seconds.
     * @return A list of MetricDataQuery objects, each representing a metric query.
     */
    public static List<MetricDataQuery> createMetricDataQueries(List<Dimension> dimensions, int interval) {
        List<MetricDataQuery> metricDataQueries = new ArrayList<>();
        metricDataQueries.add(createMetricDataQuery(Constants.AWS_METRIC_COUNT_ID, Constants.AWS_METRIC_COUNT, dimensions,
                Constants.CLOUDWATCH_SUM_STATISTIC, interval));
        metricDataQueries.add(createMetricDataQuery(Constants.AWS_METRIC_LATENCY_ID, Constants.AWS_METRIC_LATENCY, dimensions,
                Constants.CLOUDWATCH_AVERAGE_STATISTIC, interval));
        metricDataQueries.add(createMetricDataQuery(Constants.AWS_METRIC_INTEGRATION_LATENCY_ID, Constants.AWS_METRIC_INTEGRATION_LATENCY, dimensions,
                Constants.CLOUDWATCH_AVERAGE_STATISTIC, interval));
        metricDataQueries.add(createMetricDataQuery(Constants.AWS_METRIC_CLIENT_ERROR_ID, Constants.AWS_METRIC_CLIENT_ERROR, dimensions,
                Constants.CLOUDWATCH_SUM_STATISTIC, interval));
        metricDataQueries.add(createMetricDataQuery(Constants.AWS_METRIC_SERVER_ERROR_ID, Constants.AWS_METRIC_SERVER_ERROR, dimensions,
                Constants.CLOUDWATCH_SUM_STATISTIC, interval));
        return metricDataQueries;
    }

    /**
     * Creates a MetricDataQuery object for querying specific metrics data.
     *
     * @param metricId The unique identifier for the metric.
     * @param metricName The name of the metric for which MetricDataQuery should be created.
     * @param dimensions List of dimensions, including stage and API name.
     * @param statistic The statistic to be applied to the metric data.
     * @param period The period of time over which the metrics are aggregated, in seconds.
     * @return A MetricDataQuery object representing the query for the specified metric data.
     */
    private static MetricDataQuery createMetricDataQuery(String metricId, String metricName, List<Dimension> dimensions, String statistic,
                                                         Integer period) {
        return MetricDataQuery.builder()
                .metricStat(metStat -> metStat.stat(statistic)
                        .period(period)
                        .metric(met -> met.metricName(metricName).dimensions(dimensions).namespace(Constants.CLOUDWATCH_AWS_NAMESPACE)))
                .id(metricId)
                .returnData(true)
                .build();
    }
}
