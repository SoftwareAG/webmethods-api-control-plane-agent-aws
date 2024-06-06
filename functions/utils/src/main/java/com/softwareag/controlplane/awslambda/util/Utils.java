package com.softwareag.controlplane.awslambda.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.softwareag.controlplane.agentaws.auth.AWSCredentialsProvider;
import com.softwareag.controlplane.agentsdk.api.config.AuthConfig;
import com.softwareag.controlplane.agentsdk.api.config.ControlPlaneConfig;
import com.softwareag.controlplane.agentsdk.api.config.HttpConnectionConfig;
import com.softwareag.controlplane.agentsdk.api.config.RuntimeConfig;
import com.softwareag.controlplane.agentsdk.api.config.SdkConfig;
import com.softwareag.controlplane.awslambda.util.constants.Constants;
import com.softwareag.controlplane.awslambda.util.provider.EnvProvider;
import com.softwareag.controlplane.agentsdk.api.client.ControlPlaneClient;
import com.softwareag.controlplane.agentsdk.api.client.SdkClientException;
import com.softwareag.controlplane.agentsdk.api.client.http.SdkHttpClient;
import com.softwareag.controlplane.agentsdk.core.client.DefaultHttpClient;
import com.softwareag.controlplane.agentsdk.core.client.RestControlPlaneClient;
import com.softwareag.controlplane.agentsdk.core.handler.RuntimeRegistrationHandler;
import com.softwareag.controlplane.agentsdk.core.model.RuntimeReregistrationReport;
import com.softwareag.controlplane.agentsdk.model.Capacity;
import com.softwareag.controlplane.agentsdk.model.Runtime;
import org.apache.logging.log4j.Level;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.GetCallerIdentityRequest;
import software.amazon.awssdk.services.sts.model.GetCallerIdentityResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Utilities class for Functions.
 */
public final class Utils {

    private Utils() {
    }

    /**
     * Creates and configures ControlPlane configuration.
     * @return An instance of {@link ControlPlaneConfig}
     */

    public static ControlPlaneConfig getControlplaneConfig() {
        return new ControlPlaneConfig.Builder()
                .url(EnvProvider.getEnv(Constants.APICP_URL))
                .authConfig(new AuthConfig.Builder(EnvProvider.getEnv(Constants.APICP_USERNAME),
                        EnvProvider.getEnv(Constants.APICP_PASSWORD)).build())
                .build();
    }

    /**
     * Creates and configures SdkHttpClient instance.
     *
     * @return An instance of {@link SdkHttpClient}.
     */
    public static SdkHttpClient getHttpClient() {
        return new DefaultHttpClient.Builder()
                .connectionConfig(new HttpConnectionConfig.Builder().build())
                .build();
    }

    /**
     * Creates and configures Runtime configuration.
     * @return An instance of {@link RuntimeConfig}.
     */
    public static RuntimeConfig getRuntimeConfig() {
        String runtimeHost = String.format("https://%s.console.aws.amazon.com/apigateway", System.getenv(Constants.AWS_REGION));
        return new RuntimeConfig.Builder(getRuntimeId(), System.getenv(Constants.APICP_RUNTIME_NAME),
                System.getenv(Constants.APICP_RUNTIME_TYPE_ID), Runtime.DeploymentType.PUBLIC_CLOUD)
                .description(System.getenv(Constants.APICP_RUNTIME_DESCRIPTION))
                .region(System.getenv(Constants.APICP_RUNTIME_REGION))
                .location(System.getenv(Constants.APICP_RUNTIME_LOCATION))
                .capacity(getCapacity())
                .tags(getRuntimeTagsFromEnv())
                .host(runtimeHost)
                .build();
    }

    /**
     * Creates and configures a SdkConfig object using the provided ControlPlane configuration and runtime configuration.
     *
     * @param controlPlaneConfig The {@link ControlPlaneConfig} object.
     * @param runtimeConfig      The runtime configuration object.
     * @return An {@link SdkConfig} initialized with the provided configurations.
     */
    public static SdkConfig getSDKConfig(ControlPlaneConfig controlPlaneConfig, RuntimeConfig runtimeConfig) {
        return new SdkConfig.Builder(controlPlaneConfig, runtimeConfig)
                .heartbeatInterval(Integer.parseInt(EnvProvider.getEnv(Constants.APICP_HEARTBEAT_SEND_INTERVAL_SECONDS)))
                .assetsSyncInterval(Integer.parseInt(EnvProvider.getEnv(Constants.APICP_ASSETS_SYNC_INTERVAL_SECONDS)))
                .logLevel(Level.ALL)
                .build();
    }

    /**
     * Creates and configures a ControlPlaneClient Object.
     *
     * @param controlPlaneConfig The {@link ControlPlaneConfig} object.
     * @param runtimeConfig      The {@link RuntimeConfig} object.
     * @param httpClient         The {@link SdkHttpClient} object.
     * @return {@link ControlPlaneClient} A client which communicates to Control Plane APIs.
     */
    public static ControlPlaneClient getControlplaneClient(ControlPlaneConfig controlPlaneConfig, RuntimeConfig runtimeConfig, SdkHttpClient httpClient) {
        return new RestControlPlaneClient.Builder()
                .controlPlaneConfig(controlPlaneConfig)
                .runtimeConfig(runtimeConfig)
                .httpClient(httpClient)
                .build();
    }

    /**
     * Creates and configures a RuntimeRegistrationHandler Object
     *
     * @param controlPlaneClient The ControlPlaneClient object which communicates to Control Plane APIs.
     * @param sdkConfig          The {@link SdkConfig} object.
     * @return {@link RuntimeRegistrationHandler} which registers the runtime to ControlPlane.
     */
    public static RuntimeRegistrationHandler getRuntimeRegistrationHandler(ControlPlaneClient controlPlaneClient, SdkConfig sdkConfig) {
        return new RuntimeRegistrationHandler.Builder(
                controlPlaneClient,
                sdkConfig.getRuntimeConfig(),
                sdkConfig.getHeartbeatInterval())
                .build();
    }


    /**
     * Checks if the ControlPlane is active and healthy.
     *
     * @param controlPlaneClient The ControlPlaneClient object which communicates to Control Plane APIs.
     * @return {@code true} if the ControlPlane is active and healthy; {@code false} otherwise.
     */
    public static boolean isControlplaneActive(ControlPlaneClient controlPlaneClient) {
        try {
            controlPlaneClient.checkHealth();
        } catch (SdkClientException e) {
            return false;
        }
        return true;
    }

    /**
     * Generates a runtime identifier string.
     *
     * @return A string representing the runtime ID.
     */
    public static String getRuntimeId() {
        return getAccountID().concat("-")
                .concat(EnvProvider.getEnv(Constants.AWS_REGION))
                .concat("-")
                .concat(EnvProvider.getEnv(Constants.AWS_STAGE));
    }

    private static Capacity getCapacity() {
        Capacity capacity = new Capacity();
        capacity.setUnit(Capacity.TimeUnit.valueOf(EnvProvider.getEnv(Constants.APICP_RUNTIME_CAPACITY_UNIT)));
        capacity.setValue(Long.parseLong(EnvProvider.getEnv(Constants.APICP_RUNTIME_CAPACITY_VALUE)));
        return capacity;
    }

    /**
     * Retrieves runtime tags from environment variables and constructs a HashSet of strings.
     *
     * @return A HashSet of strings representing the runtime tags.
     */
    public static Set<String> getRuntimeTagsFromEnv() {
        String runtimeTagsEnv = EnvProvider.getEnv(Constants.APICP_RUNTIME_TAGS);
        String[] substrings = runtimeTagsEnv.split(",");
        return new HashSet<>(Arrays.asList(substrings));
    }

    private static String getAccountID() {
        StsClient stsClient = StsClient.builder()
                .region(Region.of(EnvProvider.getEnv(Constants.AWS_REGION)))
                .credentialsProvider(AWSCredentialsProvider.getCredentialsProvider())
                .build();

        GetCallerIdentityRequest getCallerIdentityRequest = GetCallerIdentityRequest.builder().build();
        GetCallerIdentityResponse getCallerIdentityResponse = stsClient.getCallerIdentity(getCallerIdentityRequest);
        return getCallerIdentityResponse.account();
    }

    /**
     * Retrieves the timestamp of the last synchronization action from ControlPlane.
     *
     * @param responseObject     The responseObject returned while registering runtime.
     * @param actionType         The type of action for which to retrieve the sync time.
     * @return The timestamp of the last synchronization action.
     * @throws IOException If an error occurs during JSON deserialization.
     */
    public static Long getLastActionSyncTime(Object responseObject, String actionType) throws IOException {
        if (responseObject instanceof String response) {
            ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            RuntimeReregistrationReport reregistrationReport = mapper.readValue(response, RuntimeReregistrationReport.class);
            switch (actionType){
                case Constants.SYNC_ASSET_ACTION -> {
                    return reregistrationReport.getLastAssetSyncTime();
                }
                case Constants.SEND_HEARTBEAT_ACTION -> {
                    return reregistrationReport.getLastHeartbeatTime();
                }
                case Constants.SEND_METRIC_ACTION -> {
                    return reregistrationReport.getLastMetricsTime();
                }
                default -> {
                    return null;
                }
            }
        }
        return null;
    }
}
