package com.softwareag.controlplane.agentaws.heartbeat.manager.impl;

import com.softwareag.controlplane.agentaws.heartbeat.client.AWSClientManager;
import com.softwareag.controlplane.agentaws.heartbeat.manager.HeartbeatManager;


import com.softwareag.controlplane.agentsdk.model.Heartbeat;
import software.amazon.awssdk.services.apigateway.ApiGatewayClient;
import software.amazon.awssdk.services.apigateway.model.GetRestApisResponse;
import software.amazon.awssdk.services.apigateway.model.GetStagesRequest;
import software.amazon.awssdk.services.apigateway.model.GetStagesResponse;
import software.amazon.awssdk.services.apigateway.model.RestApi;
import software.amazon.awssdk.services.apigateway.model.Stage;

import java.util.Date;
import java.util.List;

/**
 * Implementation for the HeartbeatManager
 */
public class HeartbeatManagerImpl implements HeartbeatManager {

    private static HeartbeatManagerImpl heartbeatManager;

    private final ApiGatewayClient apiGatewayClient;

    private final AWSClientManager awsClientManager;

    private HeartbeatManagerImpl(String region) {
        this.awsClientManager = AWSClientManager.getInstance(region);
        this.apiGatewayClient = awsClientManager.apiGatewayClient();
    }

    /**
     * Returns the singleton instance of the HeartbeatManagerImpl class, initializing it if necessary.
     *
     * @param region    The AWS region.
     * @return The singleton instance of HeartbeatManagerImpl.
     */
    public static HeartbeatManagerImpl getInstance(String region) {
        if(heartbeatManager != null) {
            return heartbeatManager;
        }
        heartbeatManager = new HeartbeatManagerImpl(region);
        return heartbeatManager;
    }

    /**
     * Retrieves a heartbeat object for the specified stage.
     * Checks if stage is present in at least one of the APIs. If present, heartbeat is ACTIVE, else INACTIVE.
     *
     * @param runtimeId The runtime ID for which the heartbeat is generated.
     * @param stage     The stage for which the agent is configured.
     * @return The heartbeat object.
     */
    @Override
    public Heartbeat getHeartBeat(String runtimeId, String stage) {
        Heartbeat heartbeat = new Heartbeat.Builder(runtimeId).build();
        heartbeat.setCreated(new Date().getTime());
        if (isStageActive(stage)) {
            heartbeat.setActive(1);
        } else {
            heartbeat.setActive(0);
        }
        return heartbeat;
    }


    private boolean isStageActive(String stage) {
        GetRestApisResponse restApisResponse = apiGatewayClient.getRestApis();
        List<RestApi> restApis = restApisResponse.items();
        for (RestApi restApi : restApis) {
            GetStagesRequest stagesRequest = GetStagesRequest.builder().restApiId(restApi.id()).build();
            GetStagesResponse stagesResponse = apiGatewayClient.getStages(stagesRequest);
            List<Stage> stages = stagesResponse.item();
            for (Stage stageOfApi : stages) {
                if (stage.equals(stageOfApi.stageName())) {
                    return true;
                }
            }
        }
        return false;
    }


}
