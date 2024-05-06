package com.softwareag.controlplane.agentaws.assets.utils;

import com.softwareag.controlplane.agentaws.assets.constants.Constants;
import com.softwareag.controlplane.agentsdk.model.API;
import com.softwareag.controlplane.agentsdk.model.Owner;
import com.softwareag.controlplane.agentsdk.model.Status;
import org.json.JSONArray;
import org.json.JSONObject;
import software.amazon.awssdk.services.apigateway.model.Deployment;
import software.amazon.awssdk.services.apigateway.model.Stage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * API Model converter to convert AWS API Gateway model to Agent SDK API model
 */
public class APIModelConverter {

    /**
     * Converts the provided parameters into an Agent SDK API model.
     *
     * @param apiId           The REST API ID.
     * @param tags            The tags at the parent REST API level.
     * @param stage           The stage of the API matching the configured stage.
     * @param openAPISpecJson The exported API definition from a stage in JSON format.
     * @param owner           The owner for the REST API ID fetched from AWS CloudTrail.
     * @param deployment      The deployment(version) information.
     * @return An Agent SDK API model representing the provided parameters.
     */
    public static API convertToAPIModel(String apiId, Map<String, String> tags, Stage stage,
                                        JSONObject openAPISpecJson, String owner, Deployment deployment) {
        return (API) new API.Builder(generateApiId(apiId, stage.deploymentId()), API.Type.REST)
                .version(stage.deploymentId())
                .versionSetId(apiId)
                .status(Status.ACTIVE)
                .policiesCount(getPoliciesCount(openAPISpecJson))
                .owner(getOwner(owner))
                .name(getApiName(openAPISpecJson))
                .description(deployment.description())
                .tags(getTagsForRestAPI(tags, stage.tags()))
                .created(stage.createdDate().toEpochMilli())
                .lastModified(stage.lastUpdatedDate().toEpochMilli())
                .build();

    }


    /**
     * Combines tags from the parent REST API level and stage level.
     *
     * @param apiTags   Tags at the parent REST API level.
     * @param stageTags Tags at the stage level.
     * @return A set of tags in the format key:value, combined from the stage and parent REST API.
     */
    private static Set<String> getTagsForRestAPI(Map<String, String> apiTags, Map<String, String> stageTags) {
        Map<String, String> tags = new HashMap<>(apiTags);
        tags.putAll(stageTags);
        Set<String> tagsSet = new HashSet<>();
        tags.forEach((key, value) -> tagsSet.add(key.concat(":").concat(value)));
        return tagsSet;
    }

    /**
     * Creates an Agent SDK Owner model using the provided username received from CloudTrail for an API.
     *
     * @param userName The username received from CloudTrail for an API.
     * @return An Agent SDK Owner model representing the provided username.
     */
    private static Owner getOwner(String userName) {
        Owner owner = null;
        if (userName != null) {
            owner = new Owner();
            owner.setId("");
            owner.setName(userName);
        }

        return owner;
    }

    /**
     * Calculates the number of policy statements based on the exported API definition from a stage.
     *
     * @param openAPISpecJson The exported API definition from a stage in JSON format.
     * @return The number of policy statements.
     */
    private static int getPoliciesCount(JSONObject openAPISpecJson) {
        int policiesCount = 0;
        if (openAPISpecJson.has(Constants.REST_API_POLICY)) {
            policiesCount = ((JSONArray) ((JSONObject) openAPISpecJson
                    .get(Constants.REST_API_POLICY))
                    .get(Constants.REST_API_POLICY_STATEMENT)).length();
        }

        return policiesCount;
    }

    /**
     * Retrieves the API name from the exported API definition obtained from a stage.
     *
     * @param openAPISpecJson The exported API definition from a stage in JSON format.
     * @return The name of the API.
     */
    private static String getApiName(JSONObject openAPISpecJson) {
        String name = null;
        if (openAPISpecJson.has(Constants.OPEN_API_INFO)) {
            JSONObject info = (JSONObject) openAPISpecJson.get(Constants.OPEN_API_INFO);
            if (info.has(Constants.OPEN_API_TITLE)) {
                name = info.get(Constants.OPEN_API_TITLE).toString();
            }
        }
        return name;
    }


    /**
     * Generates an API ID of the format "apiId-deploymentId" based on the provided AWS REST API ID and deployment ID.
     *
     * @param apiId        The AWS REST API ID.
     * @param deploymentId The AWS deployment ID of an API.
     * @return The generated API ID in the format "apiId-deploymentId".
     *         AWS doesn't generate a new API when a new version is created.
     */
    public static String generateApiId(String apiId, String deploymentId) {
        return apiId.concat("-").concat(deploymentId);
    }


    /**
     * Updates the status of an API and returns the updated API.
     *
     * @param api         The API for which the status is to be set.
     * @param versionId   The version ID of the API.
     * @param description The description of the API.
     * @param apiId       The API ID of the API.
     * @param isActive    The status of the API.
     * @return The updated API after setting its status.
     */
    public static API setApiStatus(API api, String versionId, String description,String apiId, boolean isActive) {
        return (API) new API.Builder(apiId, api.getType())
                .version(versionId)
                .versionSetId(api.getVersionSetId())
                .status(isActive ? Status.ACTIVE : Status.INACTIVE)
                .policiesCount(api.getPoliciesCount())
                .owner(api.getOwner())
                .name(api.getName())
                .description(description)
                .tags(api.getTags())
                .created(api.getCreated())
                .lastModified(api.getLastModified())
                .build();
    }
}
