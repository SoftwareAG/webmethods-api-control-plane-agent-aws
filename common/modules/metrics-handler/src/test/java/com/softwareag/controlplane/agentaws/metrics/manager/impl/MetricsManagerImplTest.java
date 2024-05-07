package com.softwareag.controlplane.agentaws.metrics.manager.impl;

import com.softwareag.controlplane.agentaws.metrics.constants.Constants;
import com.softwareag.controlplane.agentaws.assets.manager.AssetsManager;
import com.softwareag.controlplane.agentaws.metrics.manager.MetricsManager;
import com.softwareag.controlplane.agentaws.metrics.utils.Utility;
import com.softwareag.controlplane.agentsdk.model.API;
import com.softwareag.controlplane.agentsdk.model.Metrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.Datapoint;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricDataRequest;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricDataResponse;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricStatisticsRequest;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricStatisticsResponse;
import software.amazon.awssdk.services.cloudwatch.model.MetricDataResult;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


public class MetricsManagerImplTest {
    @Mock
    CloudWatchClient cloudWatchClient;

    @Mock
    AssetsManager assetsManager;

    final String stage = "dev";

    final String region = "us-east-1";

    MetricsManager metricsManager;

    long fromTimestamp;

    long toTimestamp;

    Instant startTime;

    long intervalInSeconds;

    int bufferIntervalInSeconds;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    //Verify get metrics by statistics returns correct metric values for API and Runtime transactions.
    @Test
    public void getMetricsByStatistics() throws NoSuchFieldException, IllegalAccessException {
        mockMetricStatistics();

        List<Metrics> metricsList = metricsManager.getMetrics(fromTimestamp, toTimestamp, intervalInSeconds, bufferIntervalInSeconds);
        Metrics metrics = metricsList.get(0);

        //verify size of the Metrics list
        assertEquals(1, metricsList.size());

        //verify timestamp of the Metric
        assertEquals(startTime.toEpochMilli(), metrics.getTimestamp());

        //verify API metrics
        assertEquals(metrics.getApiTransactionMetricsList().get(0).getApiMetrics().getTransactionCount(), 100);
        assertEquals(metrics.getApiTransactionMetricsList().get(0).getApiMetrics().getAverageBackendResponseTime(), 1.2F);
        assertEquals(metrics.getApiTransactionMetricsList().get(0).getApiMetrics().getAverageLatency(), 1.14F);
        assertEquals(metrics.getApiTransactionMetricsList().get(0).getApiMetrics().getAverageResponseTime(), 2.34F);
        assertEquals(metrics.getApiTransactionMetricsList().get(0).getMetricsByStatusCode().get("4xx").getTransactionCount(), 1);
        assertEquals(metrics.getApiTransactionMetricsList().get(0).getMetricsByStatusCode().get("5xx").getTransactionCount(), 2);

        //verify Runtime metrics
        assertEquals(metrics.getRuntimeTransactionMetrics().getApiMetrics().getTransactionCount(), 100);
        assertEquals(metrics.getRuntimeTransactionMetrics().getApiMetrics().getAverageResponseTime(), 2.34F);
        assertEquals(metrics.getRuntimeTransactionMetrics().getApiMetrics().getAverageLatency(), 1.14F);
        assertEquals(metrics.getRuntimeTransactionMetrics().getApiMetrics().getAverageBackendResponseTime(), 1.2F);
        assertEquals(metrics.getRuntimeTransactionMetrics().getMetricsByStatusCode().get("4xx").getTransactionCount(), 1);
        assertEquals(metrics.getRuntimeTransactionMetrics().getMetricsByStatusCode().get("5xx").getTransactionCount(), 2);

    }


    //Verify get metrics by data returns correct metric values for API and Runtime transactions.
    @Test
    public void verifyGetMetricsByData() throws NoSuchFieldException, IllegalAccessException {
        mockMetricData();

        List<Metrics> metrics = metricsManager.getMetrics(fromTimestamp, toTimestamp, intervalInSeconds, bufferIntervalInSeconds);

        Metrics metrics2 = metrics.get(1);
        Metrics metrics4 = metrics.get(3);

        //verify API metrics for metrics2
        assertEquals(20, metrics2.getApiTransactionMetricsList().get(0).getApiMetrics().getTransactionCount());
        assertEquals(metrics2.getApiTransactionMetricsList().get(0).getApiMetrics().getAverageBackendResponseTime(), 0.9F);
        assertEquals(metrics2.getApiTransactionMetricsList().get(0).getApiMetrics().getAverageLatency(), 0.9F);
        assertEquals(metrics2.getApiTransactionMetricsList().get(0).getApiMetrics().getAverageResponseTime(), 1.8F);
        assertEquals(metrics2.getApiTransactionMetricsList().get(0).getMetricsByStatusCode().get("4xx").getTransactionCount(), 1);
        assertEquals(metrics2.getApiTransactionMetricsList().get(0).getMetricsByStatusCode().get("5xx").getTransactionCount(), 8);

        //verify Runtime metrics for metrics4
        assertEquals(metrics4.getRuntimeTransactionMetrics().getApiMetrics().getTransactionCount(), 62);
        assertEquals(metrics4.getRuntimeTransactionMetrics().getApiMetrics().getAverageResponseTime(), 3.8258064F);
        assertEquals(metrics4.getRuntimeTransactionMetrics().getApiMetrics().getAverageLatency(), 1.9000001F);
        assertEquals(metrics4.getRuntimeTransactionMetrics().getApiMetrics().getAverageBackendResponseTime(), 1.9258064F);
        assertEquals(metrics4.getRuntimeTransactionMetrics().getMetricsByStatusCode().get("4xx").getTransactionCount(), 32);
        assertEquals(metrics4.getRuntimeTransactionMetrics().getMetricsByStatusCode().get("5xx").getTransactionCount(), 69);
    }


    //Verify if all the intervals within the defined period has a Metrics object associated with it.
    @Test
    public void verifyIfMetricsAreReturnedForEveryTimestampInTheDefinedInterval() throws NoSuchFieldException, IllegalAccessException {
        mockMetricData();

        List<Metrics> metrics = metricsManager.getMetrics(fromTimestamp, toTimestamp, intervalInSeconds, bufferIntervalInSeconds);

        Metrics metrics1 = metrics.get(0);
        Metrics metrics2 = metrics.get(1);
        Metrics metrics3 = metrics.get(2);
        Metrics metrics4 = metrics.get(3);

        //verify if 4 Metrics objects are returned(one for each interval)
        assertEquals(4, metrics.size());

        //verify if timestamps are correct
        assertEquals(metrics1.getTimestamp(), startTime.toEpochMilli());
        assertEquals(metrics2.getTimestamp(), startTime.plusSeconds(intervalInSeconds).toEpochMilli());
        assertEquals(metrics3.getTimestamp(), startTime.plusSeconds(intervalInSeconds * 2).toEpochMilli());
        assertEquals(metrics4.getTimestamp(), startTime.plusSeconds(intervalInSeconds * 3).toEpochMilli());
    }


    //Verify if no transaction metrics are generated if there are no transactions in a particular interval within the period
    @Test
    public void verifyIfNoTransactionMetricsAreReturned() throws NoSuchFieldException, IllegalAccessException {
        mockMetricData();

        List<Metrics> metrics = metricsManager.getMetrics(fromTimestamp, toTimestamp, intervalInSeconds, bufferIntervalInSeconds);

        Metrics metrics3 = metrics.get(2);

        //verify if only no apis transaction is present in metrics3
        assertEquals(metrics3.getApiTransactionMetricsList().size(), 0);

        //verify no transaction metric is created for metrics3(since there is no transaction for this timestamp metric)
        assertEquals(metrics3.getRuntimeTransactionMetrics().getApiMetrics().getTransactionCount(), 0);
        assertEquals(metrics3.getRuntimeTransactionMetrics().getApiMetrics().getAverageResponseTime(), 0);
        assertEquals(metrics3.getRuntimeTransactionMetrics().getApiMetrics().getAverageLatency(), 0);
        assertEquals(metrics3.getRuntimeTransactionMetrics().getApiMetrics().getAverageBackendResponseTime(), 0);
    }


    //Verify if fromTimestamp validations take place(if it is undefined or is more than the toTimestamp)
    @Test
    public void verifyIfMetricsAreReturnedIfTimestampIsInvalid() throws NoSuchFieldException, IllegalAccessException {
        mockMetricStatistics();

        //fromTimestamp is not defined
        fromTimestamp = 0;

        List<Metrics> metrics = metricsManager.getMetrics(fromTimestamp, toTimestamp, intervalInSeconds, bufferIntervalInSeconds);

        //verify if metrics are returned and timestamps are correct
        assertEquals(1, metrics.size());
        assertEquals(startTime.toEpochMilli(), metrics.get(0).getTimestamp());

        //fromTimestamp is after than toTimestamp
        fromTimestamp = toTimestamp + 600000;

        metrics = metricsManager.getMetrics(fromTimestamp, toTimestamp, intervalInSeconds, bufferIntervalInSeconds);

        //verify if metrics are returned and timestamps are correct
        assertEquals(1, metrics.size());
        assertEquals(startTime.toEpochMilli(), metrics.get(0).getTimestamp());
    }


    //Sets mock for cloudwatch client and metrics manager
    public void setPrivateFields(String metricsRetrievalMethod) throws NoSuchFieldException, IllegalAccessException {
        Field cloudWatchField = MetricsManagerImpl.class.getDeclaredField("cloudWatchClient");
        cloudWatchField.setAccessible(true);
        cloudWatchField.set(metricsManager, cloudWatchClient);

        Field assetsManagerField = MetricsManagerImpl.class.getDeclaredField("assetsManager");
        assetsManagerField.setAccessible(true);
        assetsManagerField.set(metricsManager, assetsManager);

        Field metricsRetrievalMethodField = MetricsManagerImpl.class.getDeclaredField("getMetricsByData");
        metricsRetrievalMethodField.setAccessible(true);
        metricsRetrievalMethodField.set(metricsManager, metricsRetrievalMethod);
    }


    //Mocks GetMetricByStatistics for 2 APIs with a 10-minute period and 10-minute interval
    public void mockMetricStatistics() throws NoSuchFieldException, IllegalAccessException {
        metricsManager = MetricsManagerImpl.getInstance(region, stage, "statistics");
        setPrivateFields("statistics");

        //10 minutes interval
        fromTimestamp = 1620050340000L;
        toTimestamp = 1620050940000L;

        intervalInSeconds = 10 * 60;
        bufferIntervalInSeconds = 60;

        long bufferedTime = Utility.reduceEpochTime(fromTimestamp, bufferIntervalInSeconds);
        startTime = Utility.alignTimestampsWithInterval(bufferedTime, intervalInSeconds);

        API api = mock(API.class);

        List<API> apis = new ArrayList<>();
        apis.add(api);

        when(assetsManager.getRestAPIs(stage, false)).thenReturn(apis);

        List<Datapoint> datapoints1 = new ArrayList<>();
        datapoints1.add(Datapoint.builder().sum(100D).timestamp(startTime).build());

        List<Datapoint> datapoints2 = new ArrayList<>();
        datapoints2.add(Datapoint.builder().average(2.34D).build());

        List<Datapoint> datapoints3 = new ArrayList<>();
        datapoints3.add(Datapoint.builder().average(1.2D).build());

        List<Datapoint> datapoints4 = new ArrayList<>();
        datapoints4.add(Datapoint.builder().sum(1D).build());

        List<Datapoint> datapoints5 = new ArrayList<>();
        datapoints5.add(Datapoint.builder().sum(2D).build());

        GetMetricStatisticsResponse response1 = GetMetricStatisticsResponse.builder().datapoints(datapoints1).build();
        GetMetricStatisticsResponse response2 = GetMetricStatisticsResponse.builder().datapoints(datapoints2).build();
        GetMetricStatisticsResponse response3 = GetMetricStatisticsResponse.builder().datapoints(datapoints3).build();
        GetMetricStatisticsResponse response4 = GetMetricStatisticsResponse.builder().datapoints(datapoints4).build();
        GetMetricStatisticsResponse response5 = GetMetricStatisticsResponse.builder().datapoints(datapoints5).build();

        when(cloudWatchClient.getMetricStatistics(any(GetMetricStatisticsRequest.class)))
                .thenAnswer(invocation -> {
                    GetMetricStatisticsRequest request = invocation.getArgument(0);

                    if (request.metricName().equals(Constants.AWS_METRIC_COUNT)) {
                        return response1;
                    } else if(request.metricName().equals(Constants.AWS_METRIC_LATENCY)){
                        return response2;
                    } else if(request.metricName().equals(Constants.AWS_METRIC_INTEGRATION_LATENCY)) {
                        return response3;
                    } else if(request.metricName().equals(Constants.AWS_METRIC_CLIENT_ERROR)) {
                        return response4;
                    } else {
                        return response5;
                    }

                });

    }


    //Mocks GetMetricByData for 2 APIs with a 1-hour period and 15-minute intervals
    public void mockMetricData() throws NoSuchFieldException, IllegalAccessException {
        metricsManager = MetricsManagerImpl.getInstance(region, stage, "data");
        setPrivateFields("data");

        //get metrics for 1 hour with 15 minutes interval
        toTimestamp = 1824528400000L;
        fromTimestamp = 1824524800000L;

        intervalInSeconds = 15 * 60;
        bufferIntervalInSeconds = 60;

        long bufferedTime = Utility.reduceEpochTime(fromTimestamp, bufferIntervalInSeconds);
        startTime = Utility.alignTimestampsWithInterval(bufferedTime, intervalInSeconds);

        String apiName1 = "API1";
        String apiName2 = "API2";

        API api1 = mock(API.class);
        API api2 = mock(API.class);

        List<API> apis = new ArrayList<>();
        apis.add(api1);
        apis.add(api2);

        when(assetsManager.getRestAPIs(stage, false)).thenReturn(apis);
        when(api1.getName()).thenReturn(apiName1);
        when(api2.getName()).thenReturn(apiName2);

        GetMetricDataResponse getMetricDataResponse1 = mock(GetMetricDataResponse.class);
        GetMetricDataResponse getMetricDataResponse2 = mock(GetMetricDataResponse.class);

        when(cloudWatchClient.getMetricData(any(GetMetricDataRequest.class)))
                .thenAnswer(invocation -> {
                    GetMetricDataRequest request = invocation.getArgument(0);
                    if (request.metricDataQueries().get(0).metricStat().metric().dimensions().get(1).value().equals(apiName1)) {
                        return getMetricDataResponse1;
                    } else {
                        return getMetricDataResponse2;
                    }
                });

        MetricDataResult metricDataResult1Api1 = mock(MetricDataResult.class);
        MetricDataResult metricDataResult2Api1 = mock(MetricDataResult.class);
        MetricDataResult metricDataResult3Api1 = mock(MetricDataResult.class);
        MetricDataResult metricDataResult4Api1 = mock(MetricDataResult.class);
        MetricDataResult metricDataResult5Api1 = mock(MetricDataResult.class);

        List<MetricDataResult> metricDataResultsApi1 = new ArrayList<>();
        metricDataResultsApi1.add(metricDataResult1Api1);
        metricDataResultsApi1.add(metricDataResult2Api1);
        metricDataResultsApi1.add(metricDataResult3Api1);
        metricDataResultsApi1.add(metricDataResult4Api1);
        metricDataResultsApi1.add(metricDataResult5Api1);

        MetricDataResult metricDataResult1Api2 = mock(MetricDataResult.class);
        MetricDataResult metricDataResult2Api2 = mock(MetricDataResult.class);
        MetricDataResult metricDataResult3Api2 = mock(MetricDataResult.class);
        MetricDataResult metricDataResult4Api2 = mock(MetricDataResult.class);
        MetricDataResult metricDataResult5Api2 = mock(MetricDataResult.class);

        List<MetricDataResult> metricDataResultsApi2 = new ArrayList<>();
        metricDataResultsApi2.add(metricDataResult1Api2);
        metricDataResultsApi2.add(metricDataResult2Api2);
        metricDataResultsApi2.add(metricDataResult3Api2);
        metricDataResultsApi2.add(metricDataResult4Api2);
        metricDataResultsApi2.add(metricDataResult5Api2);

        when(getMetricDataResponse1.metricDataResults()).thenReturn(metricDataResultsApi1);
        when(getMetricDataResponse2.metricDataResults()).thenReturn(metricDataResultsApi2);

        when(metricDataResult1Api1.id()).thenReturn(Constants.AWS_METRIC_COUNT_ID);
        when(metricDataResult2Api1.id()).thenReturn(Constants.AWS_METRIC_LATENCY_ID);
        when(metricDataResult3Api1.id()).thenReturn(Constants.AWS_METRIC_INTEGRATION_LATENCY_ID);
        when(metricDataResult4Api1.id()).thenReturn(Constants.AWS_METRIC_CLIENT_ERROR_ID);
        when(metricDataResult5Api1.id()).thenReturn(Constants.AWS_METRIC_SERVER_ERROR_ID);

        when(metricDataResult1Api2.id()).thenReturn(Constants.AWS_METRIC_COUNT_ID);
        when(metricDataResult2Api2.id()).thenReturn(Constants.AWS_METRIC_LATENCY_ID);
        when(metricDataResult3Api2.id()).thenReturn(Constants.AWS_METRIC_INTEGRATION_LATENCY_ID);
        when(metricDataResult4Api2.id()).thenReturn(Constants.AWS_METRIC_CLIENT_ERROR_ID);
        when(metricDataResult5Api2.id()).thenReturn(Constants.AWS_METRIC_SERVER_ERROR_ID);

        List<Double> countsApi1 = new ArrayList<>();
        countsApi1.add(10D);
        countsApi1.add(20D);
        countsApi1.add(50D);

        List<Double> latenciesApi1 = new ArrayList<>();
        latenciesApi1.add(1.5D);
        latenciesApi1.add(1.8D);
        latenciesApi1.add(3.4D);

        List<Double> integrationLatenciesApi1 = new ArrayList<>();
        integrationLatenciesApi1.add(1D);
        integrationLatenciesApi1.add(0.9D);
        integrationLatenciesApi1.add(2.1D);

        List<Double> clientErrorsApi1 = new ArrayList<>();
        clientErrorsApi1.add(10D);
        clientErrorsApi1.add(1D);
        clientErrorsApi1.add(9D);

        List<Double> serverErrorsApi1 = new ArrayList<>();
        serverErrorsApi1.add(5D);
        serverErrorsApi1.add(8D);
        serverErrorsApi1.add(1D);

        when(metricDataResult1Api1.values()).thenReturn(countsApi1);
        when(metricDataResult2Api1.values()).thenReturn(latenciesApi1);
        when(metricDataResult3Api1.values()).thenReturn(integrationLatenciesApi1);
        when(metricDataResult4Api1.values()).thenReturn(clientErrorsApi1);
        when(metricDataResult5Api1.values()).thenReturn(serverErrorsApi1);

        List<Double> countsApi2 = new ArrayList<>();
        countsApi2.add(12D);

        List<Double> latenciesApi2 = new ArrayList<>();
        latenciesApi2.add(5.6D);

        List<Double> integrationLatenciesApi2 = new ArrayList<>();
        integrationLatenciesApi2.add(1.2D);

        List<Double> clientErrorsApi2 = new ArrayList<>();
        clientErrorsApi2.add(23D);

        List<Double> serverErrorsApi2 = new ArrayList<>();
        serverErrorsApi2.add(68D);

        when(metricDataResult1Api2.values()).thenReturn(countsApi2);
        when(metricDataResult2Api2.values()).thenReturn(latenciesApi2);
        when(metricDataResult3Api2.values()).thenReturn(integrationLatenciesApi2);
        when(metricDataResult4Api2.values()).thenReturn(clientErrorsApi2);
        when(metricDataResult5Api2.values()).thenReturn(serverErrorsApi2);

        List<Instant> instantsApi1 = new ArrayList<>();
        instantsApi1.add(startTime);
        instantsApi1.add(startTime.plusSeconds(intervalInSeconds));
        instantsApi1.add(startTime.plusSeconds(intervalInSeconds * 3));

        when(metricDataResult1Api1.timestamps()).thenReturn(instantsApi1);

        List<Instant> instantsApi2 = new ArrayList<>();
        instantsApi2.add(startTime.plusSeconds(intervalInSeconds * 3));

        when(metricDataResult1Api2.timestamps()).thenReturn(instantsApi2);
    }

}
