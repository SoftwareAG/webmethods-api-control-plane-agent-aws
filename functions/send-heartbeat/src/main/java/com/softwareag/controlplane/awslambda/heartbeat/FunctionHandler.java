package com.softwareag.controlplane.awslambda.heartbeat;

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
import com.softwareag.controlplane.agentsdk.core.handler.SendHeartbeatHandler;
import com.softwareag.controlplane.agentsdk.core.log.DefaultAgentLogger;
import com.softwareag.controlplane.agentsdk.core.model.RuntimeReregistrationReport;
import com.softwareag.controlplane.agentsdk.model.Capacity;
import com.softwareag.controlplane.agentsdk.model.Heartbeat;
import com.softwareag.controlplane.agentsdk.model.Runtime;
import com.softwareag.controlplane.awslambda.heartbeat.retriever.HeartbeatRetrieverImpl;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.logging.log4j.Level;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.GetCallerIdentityRequest;
import software.amazon.awssdk.services.sts.model.GetCallerIdentityResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * @author CLEP
 */
@SuppressWarnings("unused")
public class FunctionHandler {

    private ControlPlaneClient controlPlaneClient;
    private SendHeartbeatHandler sendHeartbeatHandler;
    private RuntimeConfig runtimeConfig;
    private SdkConfig sdkConfig;
    private DefaultAgentLogger logger;

    public FunctionHandler() throws SdkClientException, IOException {
        init();
    }

    public void handleEvent(Context context) {
        context.getLogger().log("Started handler invocation", LogLevel.TRACE);
        if(isControlplaneActive())
            this.sendHeartbeatHandler.handle();
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

        this.runtimeConfig = new RuntimeConfig.Builder(runtimeId, System.getenv(Constants.APICP_RUNTIME_NAME),
                    System.getenv(Constants.APICP_RUNTIME_TYPE_ID), Runtime.DeploymentType.PUBLIC_CLOUD)
                .description(System.getenv(Constants.APICP_RUNTIME_DESCRIPTION))
                .region(System.getenv(Constants.APICP_RUNTIME_REGION))
                .location(System.getenv(Constants.APICP_RUNTIME_LOCATION))
                .capacity(capacity)
                .tags(getRuntimeTagsFromEnv())
                .host(runtimeHost)
                .build();

        this.sdkConfig = new SdkConfig.Builder(controlPlaneConfig, this.runtimeConfig)
                .heartbeatInterval(Integer.parseInt(EnvProvider.getEnv(Constants.APICP_HEARTBEAT_SEND_INTERVAL)))
                .logLevel(Level.ALL)
                .build();

        this.logger = DefaultAgentLogger.getInstance(getClass());

        this.sendHeartbeatHandler = new SendHeartbeatHandler.Builder(
                this.controlPlaneClient,
                new HeartbeatRetrieverImpl())
                .build();

        // Runtime registration
        RuntimeRegistrationHandler registrationHandler = new RuntimeRegistrationHandler.Builder(
                controlPlaneClient,
                this.sdkConfig.getRuntimeConfig(),
                this.sdkConfig.getHeartbeatInterval())
                .build();

        Object response = registrationHandler.handle();

        Long lastHeartbeatSyncTime = getLastHeartbeatSyncTime(response);
        if(ObjectUtils.isNotEmpty(lastHeartbeatSyncTime)) {
            Long currentTime = System.currentTimeMillis();
            // If currentTime - interval is far greater than the lastHeartbeatSyncTime, we send inactive heartbeats for the missed intervals.
            if(lastHeartbeatSyncTime < (currentTime - Long.parseLong(EnvProvider.getEnv(Constants.APICP_HEARTBEAT_SEND_INTERVAL))*2))
                sendMissingHeartbeats(lastHeartbeatSyncTime, currentTime);
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

    private Long getLastHeartbeatSyncTime(Object object) throws IOException {
        if (object instanceof String response) {
            ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            RuntimeReregistrationReport reregistrationReport = mapper.readValue(response, RuntimeReregistrationReport.class);
            return reregistrationReport.getLastHeartbeatTime();
        }
        return null;
    }

    private void sendMissingHeartbeats(Long lastSyncTime, Long currentTime) {
        List<Heartbeat> heartbeats = getInactiveHeartbeats(lastSyncTime, currentTime,
                this.sdkConfig.getHeartbeatInterval());
        try {
            this.controlPlaneClient.sendHeartbeats(heartbeats);
        } catch (SdkClientException e) {
            this.logger.error("Error occurred while sending missing heartbeats " + e.getMessage(), e);
        }
    }

    private List<Heartbeat> getInactiveHeartbeats(long fromTime, long toTime, long syncInterval) {
        List<Heartbeat> heartbeats = new ArrayList<>();

        long currentTime = fromTime;
        while (currentTime < toTime) {
            Heartbeat heartbeat = new Heartbeat.Builder(this.runtimeConfig.getId())
                    .active(Heartbeat.Status.INACTIVE)
                    .created(currentTime)
                    .build();
            heartbeats.add(heartbeat);
            currentTime += (syncInterval*1000);
        }
        return heartbeats;
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
