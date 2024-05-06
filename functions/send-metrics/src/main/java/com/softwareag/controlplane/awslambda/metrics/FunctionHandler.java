package com.softwareag.controlplane.awslambda.metrics;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.logging.LogLevel;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.softwareag.controlplane.agentaws.auth.AWSCredentialsProvider;
import com.softwareag.controlplane.agentaws.util.constants.Constants;
import com.softwareag.controlplane.agentaws.util.provider.EnvProvider;
import com.softwareag.controlplane.agentsdk.api.client.ControlPlaneClient;
import com.softwareag.controlplane.agentsdk.api.client.SdkClientException;
import com.softwareag.controlplane.agentsdk.api.client.http.SdkHttpClient;
import com.softwareag.controlplane.agentsdk.api.config.AuthConfig;
import com.softwareag.controlplane.agentsdk.api.config.ControlPlaneConfig;
import com.softwareag.controlplane.agentsdk.api.config.HttpConnectionConfig;
import com.softwareag.controlplane.agentsdk.api.config.RuntimeConfig;
import com.softwareag.controlplane.agentsdk.api.config.SdkConfig;
import com.softwareag.controlplane.agentsdk.core.client.DefaultHttpClient;
import com.softwareag.controlplane.agentsdk.core.client.RestControlPlaneClient;
import com.softwareag.controlplane.agentsdk.core.handler.RuntimeRegistrationHandler;
import com.softwareag.controlplane.agentsdk.core.handler.SendMetricsHandler;
import com.softwareag.controlplane.agentsdk.core.model.RuntimeReregistrationReport;
import com.softwareag.controlplane.agentsdk.model.Capacity;
import com.softwareag.controlplane.agentsdk.model.Runtime;
import com.softwareag.controlplane.awslambda.metrics.retriever.MetricsRetrieverImpl;
import org.apache.logging.log4j.Level;
import org.springframework.util.ObjectUtils;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.GetCallerIdentityRequest;
import software.amazon.awssdk.services.sts.model.GetCallerIdentityResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

/**
 * @author CLEP
 */
@SuppressWarnings("unused")
public class FunctionHandler {

    private SendMetricsHandler sendMetricsHandler;
    private ControlPlaneClient controlPlaneClient;

    public FunctionHandler() throws SdkClientException, IOException {
        init();
    }

    public void handleEvent(Context context) {
        context.getLogger().log("Started handler invocation", LogLevel.TRACE);
        if(isControlplaneActive())
            this.sendMetricsHandler.handle();
        else
            context.getLogger().log("Controlplane is not available", LogLevel.TRACE);
        context.getLogger().log("Finished handler invocation", LogLevel.TRACE);
    }

    private void init() throws SdkClientException, IOException {
        ControlPlaneConfig controlPlaneConfig = new ControlPlaneConfig.Builder()
                .url(EnvProvider.getEnv(Constants.APICP_CONTROLPLANE_URL))
                .authConfig(new AuthConfig.Builder(EnvProvider.getEnv(Constants.APICP_CONTROLPLANE_USERNAME),
                        EnvProvider.getEnv(Constants.APICP_CONTROLPLANE_PASSWORD)).build())
                .build();

        SdkHttpClient httpClient = new DefaultHttpClient.Builder()
                .connectionConfig(new HttpConnectionConfig.Builder().build())
                .build();

        this.controlPlaneClient = new RestControlPlaneClient.Builder()
                .controlPlaneConfig(controlPlaneConfig)
                .httpClient(httpClient)
                .build();

        Capacity capacity = new Capacity();
        capacity.setUnit(Capacity.TimeUnit.valueOf(EnvProvider.getEnv(Constants.APICP_RUNTIME_CAPACITY_UNIT)));
        capacity.setValue(Long.parseLong(EnvProvider.getEnv(Constants.APICP_RUNTIME_CAPACITY)));

        String runtimeId = getAccountID().concat("-")
                .concat(EnvProvider.getEnv(Constants.AWS_REGION))
                .concat("-")
                .concat(EnvProvider.getEnv(Constants.AWS_STAGE));

        String runtimeHost = String.format("https://%s.console.aws.amazon.com/apigateway", System.getenv(Constants.AWS_REGION));

        RuntimeConfig runtimeConfig = new RuntimeConfig.Builder(runtimeId, System.getenv(Constants.APICP_RUNTIME_NAME),
                System.getenv(Constants.APICP_RUNTIME_TYPE_ID), Runtime.DeploymentType.PUBLIC_CLOUD)
                .description(System.getenv(Constants.APICP_RUNTIME_DESCRIPTION))
                .region(System.getenv(Constants.APICP_RUNTIME_REGION))
                .location(System.getenv(Constants.APICP_RUNTIME_LOCATION))
                .capacity(capacity)
                .tags(getRuntimeTagsFromEnv())
                .host(runtimeHost)
                .build();

        SdkConfig sdkConfig = new SdkConfig.Builder(controlPlaneConfig, runtimeConfig)
                .heartbeatInterval(Integer.parseInt(EnvProvider.getEnv(Constants.APICP_HEARTBEAT_SEND_INTERVAL)))
                .metricsSendInterval(Integer.parseInt(EnvProvider.getEnv(Constants.APICP_METRICS_SEND_INTERVAL)))
                .logLevel(Level.ALL)
                .build();


        // Runtime registration
        RuntimeRegistrationHandler registrationHandler = new RuntimeRegistrationHandler.Builder(
                this.controlPlaneClient,
                sdkConfig.getRuntimeConfig(),
                sdkConfig.getHeartbeatInterval())
                .build();

        Object response = registrationHandler.handle();

        Long lastMetricSyncTime = getLastMetricSyncTime(response);
        if(!ObjectUtils.isEmpty(lastMetricSyncTime)){
            this.sendMetricsHandler = new SendMetricsHandler.Builder(this.controlPlaneClient,
                    new MetricsRetrieverImpl(),
                    runtimeId,
                    Long.parseLong(EnvProvider.getEnv(Constants.APICP_METRICS_SEND_INTERVAL)))
                    .fromTime(lastMetricSyncTime)
                    .build();
        }else {
            this.sendMetricsHandler = new SendMetricsHandler.Builder(this.controlPlaneClient,
                    new MetricsRetrieverImpl(),
                    runtimeId,
                    Long.parseLong(EnvProvider.getEnv(Constants.APICP_METRICS_SEND_INTERVAL)))
                    .build();
        }
    }

    public String getAccountID() {
        StsClient stsClient = StsClient.builder()
                .region(Region.of(EnvProvider.getEnv(Constants.AWS_REGION)))
                .credentialsProvider(AWSCredentialsProvider.getCredentialsProvider())
                .build();

        GetCallerIdentityRequest getCallerIdentityRequest = GetCallerIdentityRequest.builder().build();
        GetCallerIdentityResponse getCallerIdentityResponse = stsClient.getCallerIdentity(getCallerIdentityRequest);
        return getCallerIdentityResponse.account();
    }

    private Long getLastMetricSyncTime(Object object) throws IOException {
        if (object instanceof String response) {
            ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            RuntimeReregistrationReport reregistrationReport = mapper.readValue(response, RuntimeReregistrationReport.class);
            return reregistrationReport.getLastMetricsTime();
        }
        return null;
    }

    private boolean isControlplaneActive() {
        try {
            this.controlPlaneClient.checkHealth();
        } catch (SdkClientException e) {
            return false;
        }
        return true;
    }

    private HashSet<String> getRuntimeTagsFromEnv() {
        String runtimeTagsEnv = EnvProvider.getEnv(Constants.APICP_RUNTIME_TAGS);
        String[] substrings = runtimeTagsEnv.split(",");
        return new HashSet<>(Arrays.asList(substrings));
    }
}
