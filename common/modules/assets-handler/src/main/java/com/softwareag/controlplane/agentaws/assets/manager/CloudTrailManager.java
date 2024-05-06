package com.softwareag.controlplane.agentaws.assets.manager;

import java.util.Map;

/**
 * Interface to make calls to CloudTrail
 * We will be using CloudTrail to fetch owner and list of modified API IDs
 */
public interface CloudTrailManager {
    /**
     * Retrieves a map of REST API IDs along with their respective owners.
     * Owners are retrieved only if it is a CREATE event. Owner is null for UPDATE and DELETE events.
     *
     * @return A map containing REST API IDs as keys and their owners as values.
     */
    Map<String, String> getOwnerForRestApis();

    /**
     * Retrieves a map of operations along with a list of REST API IDs and their owners, based on the provided asset sync time and buffer time.
     * Owners are retrieved only if it is a CREATE event. Owner is null for UPDATE and DELETE events.
     *
     * @param assetSyncTime The asset sync time after which the sync actions should be retrieved.
     * @param bufferTime    The assumed time that an event takes to register to CloudTrail after it occurred.
     * @return A map containing operations as keys, with each operation mapped to a map of REST API IDs and their owners.
     */
    Map<String, Map<String, String>> getModifiedAPIs(Long assetSyncTime, int bufferTime);
}
