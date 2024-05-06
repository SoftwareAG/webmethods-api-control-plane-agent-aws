package com.softwareag.controlplane.agentaws.assets.manager.impl;

import com.softwareag.controlplane.agentaws.assets.client.AWSClientManager;
import com.softwareag.controlplane.agentaws.assets.constants.Constants;
import com.softwareag.controlplane.agentaws.assets.manager.AssetsManager;
import com.softwareag.controlplane.agentaws.assets.manager.CloudTrailManager;
import com.softwareag.controlplane.agentaws.assets.utils.APIModelConverter;
import com.softwareag.controlplane.agentsdk.model.API;
import com.softwareag.controlplane.agentsdk.model.APISyncAction;
import com.softwareag.controlplane.agentsdk.model.Asset;
import com.softwareag.controlplane.agentsdk.model.AssetSyncAction;
import com.softwareag.controlplane.agentsdk.model.AssetType;
import org.json.JSONObject;
import software.amazon.awssdk.services.apigateway.ApiGatewayClient;
import software.amazon.awssdk.services.apigateway.model.Deployment;
import software.amazon.awssdk.services.apigateway.model.GetDeploymentsRequest;
import software.amazon.awssdk.services.apigateway.model.GetDeploymentsResponse;
import software.amazon.awssdk.services.apigateway.model.GetExportRequest;
import software.amazon.awssdk.services.apigateway.model.GetExportResponse;
import software.amazon.awssdk.services.apigateway.model.GetRestApiRequest;
import software.amazon.awssdk.services.apigateway.model.GetRestApiResponse;
import software.amazon.awssdk.services.apigateway.model.GetRestApisResponse;
import software.amazon.awssdk.services.apigateway.model.GetStagesRequest;
import software.amazon.awssdk.services.apigateway.model.GetStagesResponse;
import software.amazon.awssdk.services.apigateway.model.RestApi;
import software.amazon.awssdk.services.apigateway.model.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class AssetsManagerImpl implements AssetsManager {

    private static AssetsManagerImpl assetsManager;
    ApiGatewayClient apiGatewayClient;
    CloudTrailManager cloudTrailManager;

    private AssetsManagerImpl(String region) {
        AWSClientManager awsClientManager = AWSClientManager.getInstance(region);
        this.cloudTrailManager = CloudTrailManagerImpl.getInstance(awsClientManager.cloudTrailClient());
        this.apiGatewayClient = awsClientManager.apiGatewayClient();
    }

    /**
     * Returns the singleton instance of the AssetsManagerImpl class, initializing it if necessary.
     *
     * @param region    The AWS region.
     * @return The singleton instance of AssetsManagerImpl.
     */
    public static AssetsManagerImpl getInstance(String region) {
        if(assetsManager != null) {
            return assetsManager;
        }
        assetsManager = new AssetsManagerImpl(region);
        return assetsManager;
    }

    /**
     * Retrieves list of APIs in the provided stage and sets the respective owner for the APIs
     * @param stage The stage for which the agent is configured.
     * @return The list of APIs belonging to the stage
     */
    @Override
    public List<API> getRestAPIs(String stage, boolean isOwnerRequired) {
        GetRestApisResponse restApisResponse = apiGatewayClient.getRestApis();
        List<RestApi> restApis = restApisResponse.items();
        List<API> apis = new ArrayList<>();
        Map<String, String> restApisOwner = null;
        if(isOwnerRequired)
            restApisOwner = cloudTrailManager.getOwnerForRestApis();

        for (RestApi restApi : restApis) {
            GetStagesRequest stagesRequest = GetStagesRequest.builder().restApiId(restApi.id()).build();
            GetStagesResponse stagesResponse = apiGatewayClient.getStages(stagesRequest);
            List<Stage> stages = stagesResponse.item();
            API api = getAPIFromStage(stage, stages, restApi.id(), restApi.tags(), restApisOwner != null ? restApisOwner.get(restApi.id()) : null);
            if (api != null) {
                apis.add(api);
            }
        }
        return apis;

    }

    /**
     * Retrieves a list of API sync actions for APIs modified in the provided stage after the asset sync time.
     *
     * @param stage          The stage for which the agent is configured.
     * @param assetSyncTime  The last time when assets were synced.
     * @param bufferTime     The time it takes for an event to register to AWS CloudTrail after it occurred.
     * @return A list of AssetSyncAction objects for APIs modified after the assetSyncTime.
     */
    @Override
    public List<AssetSyncAction<Asset>> getModifiedRestAPIs(String stage, Long assetSyncTime, int bufferTime) {
        List<AssetSyncAction<Asset>> assetSyncActions = new ArrayList<>();
        Map<String, Map<String, String>> modifiedApis = cloudTrailManager.getModifiedAPIs(assetSyncTime, bufferTime);
        for (String operation : modifiedApis.keySet()) {
            Map<String, String> apis = modifiedApis.get(operation);
            for (String restApiID : apis.keySet()) {
                if (Constants.DELETE.equals(operation)) {
                    assetSyncActions.addAll(getDeleteSyncActions(restApiID, assetSyncTime, null));
                }
                else {
                    GetRestApiRequest restApiRequest = GetRestApiRequest.builder().restApiId(restApiID).build();
                    GetRestApiResponse restApiResponse = apiGatewayClient.getRestApi(restApiRequest);
                    GetStagesRequest stagesRequest = GetStagesRequest.builder().restApiId(restApiID).build();
                    GetStagesResponse stagesResponse = apiGatewayClient.getStages(stagesRequest);
                    List<Stage> stages = stagesResponse.item();
                    API api = getAPIFromStage(stage, stages, restApiID, restApiResponse.tags(), apis.get(restApiID));
                    if (api != null) {
                        if(Constants.CREATE.equals(operation)) {
                            //delete previous deployment
                            assetSyncActions.addAll(getDeleteSyncActions(restApiID, assetSyncTime, api.getVersion()));
                        }
                        assetSyncActions.add((AssetSyncAction) new APISyncAction(AssetType.API,
                                AssetSyncAction.SyncType.valueOf(operation), api, assetSyncTime));
                    }
                }
            }
        }
        return assetSyncActions;
    }

    /**
     * Delete all the deployments(versions) of the provided REST API, except the activeVersion, and set the provided assets sync time for these delete asset sync actions.
     * @param restApiId The rest api id for which all the deployments should be deleted.
     * @param assetSyncTime The sync time to be set for the delete asset sync actions.
     * @return A list of AssetSyncAction objects with delete sync type, for APIs modified after the assetSyncTime.
     */
    private List<AssetSyncAction<Asset>> getDeleteSyncActions(String restApiId, Long assetSyncTime, String activeVersion) {
        List<AssetSyncAction<Asset>> deleteStatusSyncActions = new ArrayList<>();
        GetDeploymentsRequest deploymentsRequest = GetDeploymentsRequest.builder().restApiId(restApiId).build();
        GetDeploymentsResponse deploymentResponse = apiGatewayClient.getDeployments(deploymentsRequest);

        for(Deployment d : deploymentResponse.items()) {
            if(d.id().equals(activeVersion)) {
                continue;
            }
            String apiId = restApiId + "-" + d.id();
            deleteStatusSyncActions.add((AssetSyncAction) new APISyncAction(AssetType.API,
                    AssetSyncAction.SyncType.valueOf(Constants.DELETE), apiId, assetSyncTime));
        }

        return deleteStatusSyncActions;
    }


    /**
     * Retrieves API if it is present in the provided stage
     *
     * @param stage   stage for which agent is configured
     * @param stages  list of stages present for an API
     * @param apiId   apiId
     * @param apiTags Tags at Parent Rest API level
     * @param owner   owner of the rest API fetched from cloudtrail
     * @return Agent SDK API model if the API belongs to configured stage
     */
    private API getAPIFromStage(String stage, List<Stage> stages, String apiId, Map<String, String> apiTags,
                                String owner) {
        API api = null;
        for (Stage stageOfApi : stages) {
            if (stage.equals(stageOfApi.stageName())) {
                GetExportRequest exportRequest = GetExportRequest.builder()
                        .restApiId(apiId)
                        .stageName(stage)
                        .exportType(Constants.REST_API_OPEN_API_3_0)
                        .build();
                GetExportResponse exportResponse = apiGatewayClient.getExport(exportRequest);
                JSONObject openAPISpecJson = new JSONObject(exportResponse.body().asUtf8String());
                GetDeploymentsRequest deploymentsRequest = GetDeploymentsRequest.builder().restApiId(apiId).build();
                GetDeploymentsResponse deploymentResponse = apiGatewayClient.getDeployments(deploymentsRequest);

                Deployment deployment = null;
                for(Deployment d : deploymentResponse.items()) {
                    if(stageOfApi.deploymentId().equals(d.id())) {
                        deployment = d;
                        break;
                    }
                }
                api = APIModelConverter.convertToAPIModel(apiId, apiTags, stageOfApi, openAPISpecJson, owner, deployment);
                break;
            }
        }
        return api;
    }

}
