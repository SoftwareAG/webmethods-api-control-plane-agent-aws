package com.softwareag.controlplane.awslambda.heartbeat;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.logging.LogLevel;
import com.softwareag.controlplane.awslambda.util.Utils;
import com.softwareag.controlplane.awslambda.util.constants.Constants;
import com.softwareag.controlplane.awslambda.util.provider.EnvProvider;
import com.softwareag.controlplane.agentsdk.api.client.ControlPlaneClient;
import com.softwareag.controlplane.agentsdk.api.client.SdkClientException;
import com.softwareag.controlplane.agentsdk.api.client.http.SdkHttpClient;
import com.softwareag.controlplane.agentsdk.api.config.ControlPlaneConfig;
import com.softwareag.controlplane.agentsdk.api.config.RuntimeConfig;
import com.softwareag.controlplane.agentsdk.api.config.SdkConfig;
import com.softwareag.controlplane.agentsdk.core.handler.RuntimeRegistrationHandler;
import com.softwareag.controlplane.agentsdk.core.handler.SendHeartbeatHandler;
import com.softwareag.controlplane.agentsdk.core.log.DefaultAgentLogger;
import com.softwareag.controlplane.agentsdk.model.Heartbeat;
import com.softwareag.controlplane.awslambda.heartbeat.retriever.HeartbeatRetrieverImpl;
import org.apache.commons.lang3.ObjectUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author CLEP
 *
 * This FunctionHandler class serves the Heartbeat send action for the AWS Lambda function.
 * This class contains the method that will be invoked by AWS Lambda.
 */
@SuppressWarnings("unused")
public class FunctionHandler {

    private ControlPlaneClient controlPlaneClient;
    private SendHeartbeatHandler sendHeartbeatHandler;
    private RuntimeConfig runtimeConfig;
    private SdkConfig sdkConfig;
    private DefaultAgentLogger logger;

    /**
     * Initializes Configurations for the FunctionHandler.
     *
     * @throws SdkClientException if there is an error in the AWS SDK client.
     * @throws IOException if an I/O error occurs during initialization.
     */
    public FunctionHandler() throws SdkClientException, IOException {
        init();
    }

    /**
     * This handleEvent method is the entry point for the AWS Lambda function.
     * @param context The context object provides information about the invocation, function, and execution environment.
     */
    public void handleEvent(Context context) {
        context.getLogger().log("Started handler invocation", LogLevel.TRACE);
        if(Utils.isControlplaneActive(controlPlaneClient))
            this.sendHeartbeatHandler.handle();
        else
            context.getLogger().log("Controlplane is not available", LogLevel.TRACE);
        context.getLogger().log("Finished handler invocation", LogLevel.TRACE);
    }

    private void init() throws SdkClientException, IOException {
        // Setup necessary configurations.
        ControlPlaneConfig controlPlaneConfig = Utils.getControlplaneConfig();
        SdkHttpClient httpClient = Utils.getHttpClient();
        runtimeConfig = Utils.getRuntimeConfig();
        this.controlPlaneClient = Utils.getControlplaneClient(controlPlaneConfig,runtimeConfig,httpClient);
        sdkConfig = Utils.getSDKConfig(controlPlaneConfig,runtimeConfig);
        this.logger = DefaultAgentLogger.getInstance(getClass());

        this.sendHeartbeatHandler = new SendHeartbeatHandler.Builder(
                this.controlPlaneClient,
                new HeartbeatRetrieverImpl())
                .build();

        // Runtime registration
        RuntimeRegistrationHandler registrationHandler = Utils.getRuntimeRegistrationHandler(controlPlaneClient, sdkConfig);
        Object response = registrationHandler.handle();

        Long lastHeartbeatSyncTime = Utils.getLastActionSyncTime(response, Constants.SEND_HEARTBEAT_ACTION);
        if(ObjectUtils.isNotEmpty(lastHeartbeatSyncTime)) {
            Long currentTime = System.currentTimeMillis();
            // If currentTime - interval is far greater than the lastHeartbeatSyncTime, we send inactive heartbeats for the missed intervals.
            if(lastHeartbeatSyncTime < (currentTime - Long.parseLong(EnvProvider.getEnv(Constants.APICP_HEARTBEAT_SEND_INTERVAL_SECONDS))*2))
                sendMissingHeartbeats(lastHeartbeatSyncTime, currentTime);
        }
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
}
