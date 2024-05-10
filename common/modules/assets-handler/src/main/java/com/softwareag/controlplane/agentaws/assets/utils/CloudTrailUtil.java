package com.softwareag.controlplane.agentaws.assets.utils;

import com.softwareag.controlplane.agentaws.assets.constants.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for CloudTrail
 */
public class CloudTrailUtil {

    private CloudTrailUtil() {

    }

    /**
     * Retrieves a list of API create event names under which it is recorded in AWS CloudTrail.
     *
     * @return A list containing the API create event names.
     */
    public static List<String> getApiCreateEvents() {
        List<String> apiEvents = new ArrayList<>();
        apiEvents.add(Constants.CREATE_STAGE_EVENT);
        apiEvents.add(Constants.CREATE_DEPLOYMENT_EVENT);
        apiEvents.add(Constants.UPDATE_STAGE_EVENT);
        return apiEvents;
    }

    /**
     * Retrieves a list of API update event names under which it is recorded in AWS CloudTrail.
     *
     * @return A list containing the API update event names.
     */
    public static List<String> getApiUpdateEvents() {
        List<String> apiEvents = new ArrayList<>();
        apiEvents.add(Constants.TAG_RESOURCE_EVENT);
        apiEvents.add(Constants.UNTAG_RESOURCE_EVENT);
        return apiEvents;
    }

    /**
     * Retrieves a list of API delete event names under which it is recorded in AWS CloudTrail.
     *
     * @return A list containing the API delete event names.
     */
    public static List<String> getApiDeleteEvents() {
        List<String> apiEvents = new ArrayList<>();
        apiEvents.add(Constants.DELETE_STAGE_EVENT);
        return apiEvents;
    }
}
