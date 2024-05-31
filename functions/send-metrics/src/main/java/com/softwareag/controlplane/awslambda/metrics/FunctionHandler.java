package com.softwareag.controlplane.awslambda.metrics;

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
import com.softwareag.controlplane.agentsdk.core.handler.SendMetricsHandler;
import com.softwareag.controlplane.awslambda.metrics.retriever.MetricsRetrieverImpl;
import org.springframework.util.ObjectUtils;

import java.io.IOException;

/**
 * @author CLEP
 *
 * This FunctionHandler class serves the Metrics send action for the AWS Lambda function.
 * This class contains the method that will be invoked by AWS Lambda.
 */
@SuppressWarnings("unused")
public class FunctionHandler {

    private SendMetricsHandler sendMetricsHandler;
    private ControlPlaneClient controlPlaneClient;

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
            this.sendMetricsHandler.handle();
        else
            context.getLogger().log("Controlplane is not available", LogLevel.TRACE);
        context.getLogger().log("Finished handler invocation", LogLevel.TRACE);
    }

    private void init() throws SdkClientException, IOException {
        // Setup necessary configurations.
        ControlPlaneConfig controlPlaneConfig = Utils.getControlplaneConfig();
        SdkHttpClient httpClient = Utils.getHttpClient();
        RuntimeConfig runtimeConfig = Utils.getRuntimeConfig();
        this.controlPlaneClient = Utils.getControlplaneClient(controlPlaneConfig,runtimeConfig,httpClient);
        SdkConfig sdkConfig = Utils.getSDKConfig(controlPlaneConfig,runtimeConfig);

        // Runtime registration
        RuntimeRegistrationHandler registrationHandler = Utils.getRuntimeRegistrationHandler(controlPlaneClient,sdkConfig);
        Object response = registrationHandler.handle();

        Long lastMetricSyncTime = Utils.getLastActionSyncTime(response, Constants.SEND_METRIC_ACTION);
        if(!ObjectUtils.isEmpty(lastMetricSyncTime)){
            this.sendMetricsHandler = new SendMetricsHandler.Builder(this.controlPlaneClient,
                    new MetricsRetrieverImpl(),
                    Utils.getRuntimeId(),
                    Long.parseLong(EnvProvider.getEnv(Constants.APICP_METRICS_SEND_INTERVAL_SECONDS)))
                    .fromTime(lastMetricSyncTime)
                    .build();
        }else {
            this.sendMetricsHandler = new SendMetricsHandler.Builder(this.controlPlaneClient,
                    new MetricsRetrieverImpl(),
                    Utils.getRuntimeId(),
                    Long.parseLong(EnvProvider.getEnv(Constants.APICP_METRICS_SEND_INTERVAL_SECONDS)))
                    .build();
        }
    }
}
