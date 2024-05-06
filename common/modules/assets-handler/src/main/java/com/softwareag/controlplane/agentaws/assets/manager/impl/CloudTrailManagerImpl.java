package com.softwareag.controlplane.agentaws.assets.manager.impl;

import com.softwareag.controlplane.agentaws.assets.constants.Constants;
import com.softwareag.controlplane.agentaws.assets.manager.CloudTrailManager;
import com.softwareag.controlplane.agentaws.assets.utils.CloudTrailUtil;
import com.softwareag.controlplane.agentaws.assets.utils.Utility;
import com.softwareag.controlplane.agentsdk.core.log.DefaultAgentLogger;
import org.json.JSONObject;

import software.amazon.awssdk.services.cloudtrail.CloudTrailClient;
import software.amazon.awssdk.services.cloudtrail.model.LookupAttribute;
import software.amazon.awssdk.services.cloudtrail.model.LookupAttributeKey;
import software.amazon.awssdk.services.cloudtrail.model.LookupEventsRequest;
import software.amazon.awssdk.services.cloudtrail.model.LookupEventsResponse;
import software.amazon.awssdk.services.cloudtrail.model.Event;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;
import java.util.LinkedHashMap;


public class CloudTrailManagerImpl implements CloudTrailManager {
    private static CloudTrailManagerImpl cloudTrailManager;
    CloudTrailClient cloudTrailClient;
    DefaultAgentLogger agentLogger;

    private CloudTrailManagerImpl(CloudTrailClient cloudTrailClient) {
        this.cloudTrailClient = cloudTrailClient;
        this.agentLogger = DefaultAgentLogger.getInstance(CloudTrailManager.class);
    }

    public static CloudTrailManagerImpl getInstance(CloudTrailClient cloudTrailClient) {
        if(cloudTrailManager != null) {
            return cloudTrailManager;
        }
        cloudTrailManager = new CloudTrailManagerImpl(cloudTrailClient);
        return cloudTrailManager;
    }


    /**
     * Retrieves a map of REST API IDs along with their respective owners.
     * Owners are retrieved only if it is a CREATE event. Owner is null for UPDATE and DELETE events.
     *
     * @return A map containing REST API IDs as keys and their owners as values.
     */
    @Override
    public Map<String, String> getOwnerForRestApis() {
        Map<String, Map<String, String>> createdApis = new HashMap<>();
        createdApis.put(Constants.CREATE, new HashMap<>());

        List<String> apiEvents = new ArrayList<>();
        apiEvents.add(Constants.CREATE_DEPLOYMENT_EVENT);

        Instant endTime = Instant.now();
        Instant startTime = endTime.minus(90, ChronoUnit.DAYS);
        setIdAndOwner(apiEvents, startTime, endTime, createdApis);
        return createdApis.get(Constants.CREATE);
    }

    /**
     * Retrieves a map of operations along with a list of REST API IDs and their owners, based on the provided asset sync time and buffer time.
     * Events are fetched from the AWS CloudTrail(from provided asset sync timestamp to current timestamp) and is synced with the webMethods API Control Plane.
     * Owners are retrieved only if it is a CREATE event. Owner is null for UPDATE and DELETE events.
     *
     * @param assetSyncTime The asset sync time after which the sync actions should be retrieved.
     * @param bufferSeconds The time that an event takes to register to CloudTrail after it occurred.
     * @return A map containing operations as keys, with each operation mapped to a map of REST API IDs and their owners.
     */
    @Override
    public Map<String, Map<String, String>> getModifiedAPIs(Long assetSyncTime, int bufferSeconds) {
        Map<String, Map<String, String>> modifiedAPIs = new LinkedHashMap<>();
        modifiedAPIs.put(Constants.CREATE, new HashMap<>());
        modifiedAPIs.put(Constants.UPDATE, new HashMap<>());
        modifiedAPIs.put(Constants.DELETE, new HashMap<>());
        List<String> apiEvents = new ArrayList<>();
        apiEvents.addAll(CloudTrailUtil.getApiCreateEvents());// created API
        apiEvents.addAll(CloudTrailUtil.getApiUpdateEvents());// updated API
        apiEvents.addAll(CloudTrailUtil.getApiDeleteEvents());// deleted API
        Instant endTime = Instant.now().minusSeconds(bufferSeconds);
        Instant startTime = Utility.getInstant(assetSyncTime).minusSeconds(bufferSeconds);
        setIdAndOwner(apiEvents, startTime, endTime, modifiedAPIs);
        return modifiedAPIs;
    }

    private void setIdAndOwner(List<String> apiEvents, Instant startTime, Instant endTime,
                               Map<String, Map<String, String>> modifiedApis) {
        boolean setOwner;
        String operation;
        for (String apiEvent : apiEvents) {
            if (CloudTrailUtil.getApiCreateEvents().contains(apiEvent)) {
                operation = Constants.CREATE;
                setOwner = true;
            } else if (CloudTrailUtil.getApiUpdateEvents().contains(apiEvent)) {
                operation = Constants.UPDATE;
                setOwner = false;
            } else {
                operation = Constants.DELETE;
                setOwner = false;
            }

            LookupAttribute attribute = LookupAttribute.builder().attributeKey(LookupAttributeKey.EVENT_NAME).
                    attributeValue(apiEvent).build();
            LookupEventsRequest eventsRequest;
            LookupEventsResponse response;
            boolean done = false;
            String nextToken = null;

            while (!done) {
                if (nextToken == null) {
                    eventsRequest = LookupEventsRequest.builder()
                            .startTime(startTime)
                            .endTime(endTime)
                            .lookupAttributes(attribute)
                            .build();

                } else {
                    eventsRequest = LookupEventsRequest.builder()
                            .startTime(startTime)
                            .endTime(endTime)
                            .lookupAttributes(attribute)
                            .nextToken(nextToken)
                            .build();
                }

                //adding wait time to ensure less than 2 calls are made to CloudTrail
                try {
                    Thread.sleep(1000);
                }
                catch (InterruptedException e) {
                    agentLogger.error("Thread interrupted during sleep", e);
                }

                response = cloudTrailClient.lookupEvents(eventsRequest);
                for (Event event : response.events()) {
                    getOwnerFromResponse(modifiedApis.get(operation), setOwner, event);
                }
                if (response.nextToken() == null) {
                    done = true;
                } else {
                    nextToken = response.nextToken();
                }
            }
        }

    }

    private void getOwnerFromResponse(Map<String, String> restApisOwner, boolean setOwner, Event event) {
        Optional<String> cloudTrailEvent = event.getValueForField(Constants.CLOUD_TRAIL_EVENT_FIELD, String.class);
        JSONObject cloudTrailEventJson = new JSONObject(cloudTrailEvent.get());
        if (!cloudTrailEventJson.isNull(Constants.CLOUD_TRAIL_REQUEST_PARAMETERS)) {
            JSONObject response = (JSONObject) cloudTrailEventJson.get(Constants.CLOUD_TRAIL_REQUEST_PARAMETERS);
            String owner = setOwner ? event.username() : null;
            if(event.eventName().equals(Constants.TAG_RESOURCE_EVENT) || event.eventName().equals(Constants.UNTAG_RESOURCE_EVENT)) {
                String resourceArn = response.getString(Constants.RESOURCE_ARN);
                String[] split = resourceArn.split("/");
                String restApiId = split[2];
                restApisOwner.put(restApiId, owner);
            }
            else if (response.has(Constants.REST_API_ID)) {
                String restApiId = response.getString(Constants.REST_API_ID);
                restApisOwner.put(restApiId, owner);
            }
        }
    }


}
