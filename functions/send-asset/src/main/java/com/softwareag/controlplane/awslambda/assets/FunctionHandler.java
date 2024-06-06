package com.softwareag.controlplane.awslambda.assets;

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
import com.softwareag.controlplane.agentsdk.core.assetsync.dispatcher.AssetSyncDispatcherProvider;
import com.softwareag.controlplane.agentsdk.core.handler.RuntimeRegistrationHandler;
import com.softwareag.controlplane.agentsdk.core.handler.SyncAssetsHandler;
import com.softwareag.controlplane.agentsdk.core.log.DefaultAgentLogger;
import com.softwareag.controlplane.awslambda.assets.retriever.AssetSyncRetrieverImpl;
import com.softwareag.controlplane.awslambda.assets.retriever.AssetsRetrieverImpl;
import org.apache.commons.lang3.ObjectUtils;

import java.io.IOException;

/**
 * This FunctionHandler class serves the Asset Sync action for the AWS Lambda function.
 * This class contains the method that will be invoked by AWS Lambda.
 */
public class FunctionHandler {

    private SyncAssetsHandler syncAssetsHandler;
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
        if(Utils.isControlplaneActive(this.controlPlaneClient))
            this.syncAssetsHandler.handle();
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
        DefaultAgentLogger logger = DefaultAgentLogger.getInstance(getClass());

        // Runtime registration
        RuntimeRegistrationHandler registrationHandler = Utils.getRuntimeRegistrationHandler(controlPlaneClient,sdkConfig);
        Object response = registrationHandler.handle();

        // This handler will be used to publish assets for the first time.
        this.syncAssetsHandler = new SyncAssetsHandler.Builder(new AssetsRetrieverImpl(),
                Long.parseLong(EnvProvider.getEnv(Constants.APICP_ASSETS_SYNC_INTERVAL_SECONDS)))
                .assetSyncDispatcher(AssetSyncDispatcherProvider.createAssetSyncDispatcher(this.controlPlaneClient,logger))
                .build();

        // Publish asset only if the lastAssetSyncTime is null (as it could be the first time registration)
        Long lastAssetSyncTime = Utils.getLastActionSyncTime(response, Constants.SYNC_ASSET_ACTION);
        if(ObjectUtils.isEmpty(lastAssetSyncTime)){
            this.syncAssetsHandler.handle();
            this.syncAssetsHandler = new SyncAssetsHandler.Builder(new AssetSyncRetrieverImpl(),
                    Long.parseLong(EnvProvider.getEnv(Constants.APICP_ASSETS_SYNC_INTERVAL_SECONDS)))
                    .assetSyncDispatcher(AssetSyncDispatcherProvider.createAssetSyncDispatcher(this.controlPlaneClient,logger))
                    .build();
        } else {
            this.syncAssetsHandler = new SyncAssetsHandler.Builder(new AssetSyncRetrieverImpl(),
                    Long.parseLong(EnvProvider.getEnv(Constants.APICP_ASSETS_SYNC_INTERVAL_SECONDS)))
                    .assetSyncDispatcher(AssetSyncDispatcherProvider.createAssetSyncDispatcher(this.controlPlaneClient,logger))
                    .fromTime(lastAssetSyncTime)
                    .build();
        }
    }

}
