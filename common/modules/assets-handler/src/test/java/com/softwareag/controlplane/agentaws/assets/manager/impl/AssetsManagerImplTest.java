package com.softwareag.controlplane.agentaws.assets.manager.impl;

import com.softwareag.controlplane.agentaws.assets.constants.Constants;
import com.softwareag.controlplane.agentaws.assets.manager.AssetsManager;
import com.softwareag.controlplane.agentsdk.model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.apigateway.ApiGatewayClient;
import software.amazon.awssdk.services.apigateway.model.*;
import software.amazon.awssdk.services.cloudtrail.CloudTrailClient;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;


class AssetsManagerImplTest {

    @Mock
    CloudTrailManagerImpl cloudTrailManager;
    @Mock
    ApiGatewayClient apiGatewayClient;
    AssetsManager assetsManager;

    MockedStatic<CloudTrailManagerImpl> mockCloudTrail;

    @BeforeEach
    public void setup() throws NoSuchFieldException, IllegalAccessException {
        MockitoAnnotations.openMocks(this);
        mockCloudTrail = Mockito.mockStatic(CloudTrailManagerImpl.class);
        mockCloudTrail.when(() -> CloudTrailManagerImpl.getInstance(any(CloudTrailClient.class)))
                .thenReturn(cloudTrailManager);
        assetsManager = AssetsManagerImpl.getInstance("us-east-1");
        setPrivateFields();
        mockGetRestAPIs();
        mockOwnerInfo();
        mockGetStages();
        mockGetExport();
        mockGetDeployments();
    }

    @AfterEach
    public void cleanUp() {
        mockCloudTrail.close();
    }

    @Test
    void testGetRestAPIs() {
        List<API> apis = assetsManager.getRestAPIs("prod",false);
        assertEquals(2, apis.size());
    }

    @Test
    void testGetRestAPIsWithOwnerInfo() {
        List<API> apis = assetsManager.getRestAPIs("dev",true);
        assertEquals(3, apis.size());
        assertNotNull(apis.get(1).getOwner());
        assertEquals(2, apis.get(1).getPoliciesCount());
    }

    @Test
    void testGetModifiedRestAPIs() {
        mockGetRestAPI();
        mockGetModifiedAPIs();

        List<AssetSyncAction<Asset>> syncActions = assetsManager.getModifiedRestAPIs("prod",1715677954960L,300);
        assertEquals(9, syncActions.size());
    }

    public void setPrivateFields() throws NoSuchFieldException, IllegalAccessException {
        Field apiGatewayClientField = AssetsManagerImpl.class.getDeclaredField("apiGatewayClient");
        apiGatewayClientField.setAccessible(true);
        apiGatewayClientField.set(assetsManager, apiGatewayClient);
    }

    public void mockGetRestAPI() {
        GetRestApiResponse mockRestApiResponse = mock(GetRestApiResponse.class);
        when(apiGatewayClient.getRestApi(any(GetRestApiRequest.class))).thenAnswer(
                invocation -> {
                    GetRestApiRequest request = invocation.getArgument(0);
                    String apiId = request.restApiId();
                    when(mockRestApiResponse.id()).thenReturn(apiId);
                    when(mockRestApiResponse.name()).thenReturn("testAPI");
                    return mockRestApiResponse;
                });
    }

    public void mockGetRestAPIs() {
        GetRestApisResponse mockRestApisResponse = mock(GetRestApisResponse.class);
        when(apiGatewayClient.getRestApis()).thenReturn(mockRestApisResponse);

        //Mock RestAPIs
        RestApi restApi1 = mock(RestApi.class);
        when(restApi1.id()).thenReturn("id1");
        when(restApi1.name()).thenReturn("api1");

        RestApi restApi2 = mock(RestApi.class);
        when(restApi2.id()).thenReturn("id2");
        when(restApi2.name()).thenReturn("api2");

        RestApi restApi3 = mock(RestApi.class);
        when(restApi3.id()).thenReturn("id3");
        when(restApi3.name()).thenReturn("api3");

        RestApi restApi4 = mock(RestApi.class);
        when(restApi4.id()).thenReturn("id4");
        when(restApi4.name()).thenReturn("api4");


        List<RestApi> restApis = new ArrayList<>();
        restApis.add(restApi1);
        restApis.add(restApi2);
        restApis.add(restApi3);
        restApis.add(restApi4);

        when(mockRestApisResponse.items()).thenReturn(restApis);
    }

    public void mockOwnerInfo() {
        Map<String, String> ownerInfo = new HashMap<>();
        ownerInfo.put("id1","owner1");
        ownerInfo.put("id2","owner2");
        ownerInfo.put("id3","owner3");
        when(cloudTrailManager.getOwnerForRestApis()).thenReturn(ownerInfo);
    }

    public void mockGetStages() {
        GetStagesResponse mockStageResponse = mock(GetStagesResponse.class);


        Stage devStage = mock(Stage.class);
        when(devStage.stageName()).thenReturn("dev");
        when(devStage.deploymentId()).thenReturn("deployment-1");
        when(devStage.createdDate()).thenReturn(Instant.parse("2024-05-01T10:15:30Z"));
        when(devStage.lastUpdatedDate()).thenReturn(Instant.parse("2024-05-05T10:15:30Z"));

        Stage testStage = mock(Stage.class);
        when(testStage.stageName()).thenReturn("stage");
        when(testStage.deploymentId()).thenReturn("deployment-2");
        when(testStage.createdDate()).thenReturn(Instant.parse("2024-05-01T10:15:30Z"));
        when(testStage.lastUpdatedDate()).thenReturn(Instant.parse("2024-05-05T10:15:30Z"));

        Stage preProdStage = mock(Stage.class);
        when(preProdStage.stageName()).thenReturn("preprod");
        when(preProdStage.deploymentId()).thenReturn("deployment-3");
        when(preProdStage.createdDate()).thenReturn(Instant.parse("2024-05-01T10:15:30Z"));
        when(preProdStage.lastUpdatedDate()).thenReturn(Instant.parse("2024-05-05T10:15:30Z"));

        Stage prodStage = mock(Stage.class);
        when(prodStage.stageName()).thenReturn("prod");
        when(prodStage.deploymentId()).thenReturn("deployment-4");
        when(prodStage.createdDate()).thenReturn(Instant.parse("2024-05-01T10:15:30Z"));
        when(prodStage.lastUpdatedDate()).thenReturn(Instant.parse("2024-05-05T10:15:30Z"));

        when(apiGatewayClient.getStages(any(GetStagesRequest.class))).thenAnswer(
                invocation -> {
                    GetStagesRequest request = invocation.getArgument(0);
                    String apiId = request.restApiId();
                    if(apiId.equals("id1")) {
                        List<Stage> stages = new ArrayList<>();
                        stages.add(devStage);
                        stages.add(prodStage);
                        when(mockStageResponse.item()).thenReturn(stages);
                    }
                    else if(apiId.equals("id2")) {
                        List<Stage> stages = new ArrayList<>();
                        stages.add(testStage);
                        stages.add(devStage);
                        when(mockStageResponse.item()).thenReturn(stages);
                    }
                    else if(apiId.equals("id3")) {
                        List<Stage> stages = new ArrayList<>();
                        stages.add(preProdStage);
                        when(mockStageResponse.item()).thenReturn(stages);
                    }
                    else if(apiId.equals("id4")) {
                        List<Stage> stages = new ArrayList<>();
                        stages.add(testStage);
                        stages.add(devStage);
                        stages.add(preProdStage);
                        stages.add(prodStage);
                        when(mockStageResponse.item()).thenReturn(stages);
                    }
                    return mockStageResponse;
                }
        );
    }

    public void mockGetExport() {
        GetExportResponse mockGetExportResponse = mock(GetExportResponse.class);
        byte[] jsonBytes = ("{\n" +
                        "    \"openapi\" : \"3.0.1\",\n" +
                        "    \"info\" : {\n" +
                        "        \"title\" : \"test-api\",\n" +
                        "        \"description\" : \"Test api for Unit test\",\n" +
                        "        \"termsOfService\" : \"https://softwareag/Pets\"\n" +
                        "    },\n" +
                        "    \"x-amazon-apigateway-policy\": {\n" +
                        "       \"Statement\": \n" +
                        "           [\n" +
                        "               {\n" +
                        "                   \"action\": \"execute-api:Invoke\",\n" +
                        "                   \"effect\": \"Allow\",\n" +
                        "                   \"resource\": \"*\"\n" +
                        "               },\n" +
                        "               {\n" +
                        "                   \"action\": \"execute-api:Manage\",\n" +
                        "                   \"effect\": \"Deny\",\n" +
                        "                   \"resource\": \"*\"\n" +
                        "               }\n" +
                        "           ]\n" +
                        "      }\n" +
                        "}").getBytes();

        when(mockGetExportResponse.body()).thenReturn(SdkBytes.fromByteArray(jsonBytes));
        when(apiGatewayClient.getExport(any(GetExportRequest.class))).thenReturn(mockGetExportResponse);
    }

    public void mockGetDeployments() {
        GetDeploymentsResponse mockDeploymentsResponse = mock(GetDeploymentsResponse.class);

        // Set Mock deployments.
        Deployment deployment1 = mock(Deployment.class);
        when(deployment1.id()).thenReturn("deployment-1");
        when(deployment1.description()).thenReturn("Description for Deployment-1");
        when(deployment1.createdDate()).thenReturn(Instant.parse("2024-05-01T10:15:30Z"));

        Deployment deployment2 = mock(Deployment.class);
        when(deployment2.id()).thenReturn("deployment-2");
        when(deployment2.description()).thenReturn("Description for Deployment-2");
        when(deployment2.createdDate()).thenReturn(Instant.parse("2024-05-05T11:25:50Z"));

        Deployment deployment3 = mock(Deployment.class);
        when(deployment3.id()).thenReturn("deployment-3");
        when(deployment3.description()).thenReturn("Description for Deployment-3");
        when(deployment3.createdDate()).thenReturn(Instant.parse("2024-05-13T07:55:20Z"));

        Deployment deployment4 = mock(Deployment.class);
        when(deployment4.id()).thenReturn("deployment-4");
        when(deployment4.description()).thenReturn("Description for Deployment-4");
        when(deployment4.createdDate()).thenReturn(Instant.parse("2024-05-13T07:55:20Z"));

        List<Deployment> deployments = new ArrayList<>();
        deployments.add(deployment1);
        deployments.add(deployment2);
        deployments.add(deployment3);
        deployments.add(deployment4);

        when(mockDeploymentsResponse.items()).thenReturn(deployments);
        when(apiGatewayClient.getDeployments(any(GetDeploymentsRequest.class))).thenReturn(mockDeploymentsResponse);
    }

    public void mockGetModifiedAPIs() {
        Map<String, Map<String, String>> mockModifiedAPIs = new HashMap<>();
        Map<String, String> apiInfo = new HashMap<>();
        apiInfo.put("id1","owner1");
        mockModifiedAPIs.put(Constants.CREATE, apiInfo);
        mockModifiedAPIs.put(Constants.UPDATE, apiInfo);

        Map<String, String> apiInfoDeleteAction = new HashMap<>();
        apiInfoDeleteAction.put("id2","owner2");
        mockModifiedAPIs.put(Constants.DELETE, apiInfoDeleteAction);
        when(cloudTrailManager.getModifiedAPIs(any(Long.class), anyInt())).thenReturn(mockModifiedAPIs);
    }
}
