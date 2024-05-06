package com.softwareag.controlplane.agentaws.metrics.constants;


public final class Constants {
    private Constants() {
    }

    public static final String AWS_METRIC_COUNT = "Count";
    public static final String AWS_METRIC_LATENCY = "Latency";
    public static final String AWS_METRIC_INTEGRATION_LATENCY = "IntegrationLatency";
    public static final String AWS_METRIC_CLIENT_ERROR = "4XXError";
    public static final String AWS_METRIC_SERVER_ERROR = "5XXError";
    public static final String AWS_METRIC_COUNT_ID = "metricCountId";
    public static final String AWS_METRIC_LATENCY_ID = "metricLatencyId";
    public static final String AWS_METRIC_INTEGRATION_LATENCY_ID = "metricIntegrationLatencyId";
    public static final String AWS_METRIC_CLIENT_ERROR_ID = "metric4XXErrorId";
    public static final String AWS_METRIC_SERVER_ERROR_ID = "metric5XXErrorId";
    public static final String CLOUDWATCH_AWS_NAMESPACE = "AWS/ApiGateway";

    public static final String CLOUDWATCH_SUM_STATISTIC = "Sum";
    public static final String CLOUDWATCH_AVERAGE_STATISTIC = "Average";
    public static final String STAGE = "Stage";
    public static final String API_NAME = "ApiName";

    public static final String CP_METRIC_CLIENT_ERROR = "4xx";
    public static final String CP_METRIC_SERVER_ERROR = "5xx";
}

