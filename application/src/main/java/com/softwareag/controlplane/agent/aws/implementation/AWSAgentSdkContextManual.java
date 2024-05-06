package com.softwareag.controlplane.agent.aws.implementation;

import com.softwareag.controlplane.agent.aws.configuration.AWSProperties;
import com.softwareag.controlplane.agentaws.assets.manager.impl.AssetsManagerImpl;
import com.softwareag.controlplane.agentaws.heartbeat.manager.impl.HeartbeatManagerImpl;
import com.softwareag.controlplane.agentaws.metrics.manager.impl.MetricsManagerImpl;

import com.softwareag.controlplane.agentsdk.api.AgentSDKContextManual;
import com.softwareag.controlplane.agentsdk.api.SdkLogger;
import com.softwareag.controlplane.agentsdk.api.client.http.SdkHttpClient;
import com.softwareag.controlplane.agentsdk.api.config.SdkConfig;

import com.softwareag.controlplane.agentsdk.model.API;
import com.softwareag.controlplane.agentsdk.model.Asset;
import com.softwareag.controlplane.agentsdk.model.Heartbeat;
import com.softwareag.controlplane.agentsdk.model.Metrics;
import com.softwareag.controlplane.agentsdk.model.AssetSyncAction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Implementation of the AgentSDKContextManual interface
 */
@Component
public class AWSAgentSdkContextManual implements AgentSDKContextManual {

    @Autowired
    private SdkConfig sdkConfig;

    @Autowired
    private AWSProperties awsProperties;

    @Autowired
    private HeartbeatManagerImpl heartbeatManager;

    @Autowired
    private MetricsManagerImpl metricsManager;

    @Autowired
    private AssetsManagerImpl assetsManager;

    /**
     * Retrieves the heartbeat for the AWS API Gateway at the specified heartbeat interval.
     *
     * @return The heartbeat.
     */
    @Override
    public Heartbeat getHeartbeat() {
        return heartbeatManager.getHeartBeat(sdkConfig.getRuntimeConfig().getId(), awsProperties.getStage());
    }

    /**
     * Publishes the list of APIs present in the AWS API Gateway
     *
     * @return The list of APIs.
     */
    @Override
    public List<API> getAPIs() {
        return assetsManager.getRestAPIs(awsProperties.getStage(), true);
    }

    /**
     * Retrieves the metrics within the specified time range from the AWS CloudWatch and syncs it to the webMethods API Control Plane.
     *
     * @param fromTimestamp The start timestamp.
     * @param toTimestamp   The end timestamp.
     * @param interval      The interval between timestamps.
     * @return The list of metrics.
     */
    @Override
    public List<Metrics> getMetrics(long fromTimestamp, long toTimestamp, long interval) {
        return metricsManager.getMetrics(fromTimestamp, toTimestamp, interval, awsProperties.getMetricsSyncBufferIntervalSeconds());
    }

    /**
     * Retrieves the asset sync actions from the AWS CloudTrail and syncs it to the webMethods API Control Plane from the last asset sync timestamp to the current timestamp..
     *
     * @param l The last asset sync timestamp.
     * @return The list of asset sync actions from the last asset sync timestamp to the current timestamp.
     */
    @Override
    public List<AssetSyncAction<Asset>> getAssetSyncActions(long l) {
        return assetsManager.getModifiedRestAPIs(awsProperties.getStage(), l, awsProperties.getAssetsSyncBufferIntervalSeconds());
    }

    /**
     * Uses the SDK configuration defined for the AWS Agent.
     *
     * @return The SDK configuration.
     */
    @Override
    public SdkConfig getSdkConfig() {
        return sdkConfig;
    }

    /**
     * Uses the default Logger defined in the AgentSDK.
     *
     * @return null
     */
    @Override
    public SdkLogger getLogger() {
        return null;
    }

    /**
     * Uses the default HTTP Client defined in the AgentSDK.
     *
     * @return null
     */
    @Override
    public SdkHttpClient getHttpClient() {
        return null;
    }
}
