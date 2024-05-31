package com.softwareag.controlplane.agentaws.heartbeat.manager.impl;

import com.softwareag.controlplane.agentaws.heartbeat.manager.HeartbeatManager;
import com.softwareag.controlplane.agentsdk.model.Heartbeat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import org.mockito.MockitoAnnotations;
import software.amazon.awssdk.services.apigateway.ApiGatewayClient;
import software.amazon.awssdk.services.apigateway.model.GetRestApisResponse;
import software.amazon.awssdk.services.apigateway.model.GetStagesRequest;
import software.amazon.awssdk.services.apigateway.model.GetStagesResponse;
import software.amazon.awssdk.services.apigateway.model.RestApi;
import software.amazon.awssdk.services.apigateway.model.Stage;


import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


class HeartbeatManagerImplTest {
    @Mock
    ApiGatewayClient apiGatewayClient;

    @Mock
    GetRestApisResponse getRestApisResponse;

    @Mock
    GetStagesResponse getStagesResponse;

    @Mock
    RestApi restApi;

    @Mock
    Stage stage;

    HeartbeatManager heartbeatManager;

    final String region = "us-east-1";

    final String stage_name = "dev";

    final String runtime_id = region + stage_name;

    @BeforeEach
    void setUp() throws IllegalAccessException, NoSuchFieldException {
        MockitoAnnotations.openMocks(this);
        heartbeatManager = HeartbeatManagerImpl.getInstance(region);

        Field privateField = HeartbeatManagerImpl.class.getDeclaredField("apiGatewayClient");
        privateField.setAccessible(true);
        privateField.set(heartbeatManager, apiGatewayClient);

        List<RestApi> restApis = new ArrayList<>();
        restApis.add(restApi);

        List<Stage> stages = new ArrayList<>();
        stages.add(stage);

        when(apiGatewayClient.getRestApis()).thenReturn(getRestApisResponse);
        when(getRestApisResponse.items()).thenReturn(restApis);
        when(apiGatewayClient.getStages((GetStagesRequest) any())).thenReturn(getStagesResponse);
        when(getStagesResponse.item()).thenReturn(stages);
    }


    //Verify a stage is active if it is present in atleast one api
    @Test
    void getActiveHeartbeatIfStageIsPresent() {
        when(stage.stageName()).thenReturn(stage_name);

        Heartbeat heartbeat = heartbeatManager.getHeartBeat(runtime_id, stage_name);
        assertEquals(1, heartbeat.getActive());
    }

    //Verify a stage is inactive if it is not present in any of the apis
    @Test
    void getInactiveHeartbeatIfStageIsNotPresent() {
        when(stage.stageName()).thenReturn("qa");

        Heartbeat heartbeat = heartbeatManager.getHeartBeat(runtime_id, stage_name);
        assertEquals(0, heartbeat.getActive());
    }

}
