package com.softwareag.controlplane.agentaws.assets.manager;

import com.softwareag.controlplane.agentsdk.model.API;
import com.softwareag.controlplane.agentsdk.model.Asset;
import com.softwareag.controlplane.agentsdk.model.AssetSyncAction;

import java.util.List;

/**
 * Interface to managing assets operation using Api Gateway client
 */
public interface AssetsManager {
    /**
     * Retrieves list of APIs in the provided stage
     *
     * @param stage The stage for which the agent is configured.
     * @return The list of APIs belonging to the stage
     */
    List<API> getRestAPIs(String stage, boolean isOwnerRequired);


    /**
     * Retrieves a list of API sync actions for APIs modified in the provided stage after the asset sync time.
     *
     * @param stage          The stage for which the agent is configured.
     * @param assetSyncTime  The last time when assets were synced.
     * @param bufferTime     The assumed time,in seconds, it takes for an event to register to AWS CloudTrail after it occurred.
     * @return A list of AssetSyncAction objects for APIs modified after the assetSyncTime.
     */
    List<AssetSyncAction<Asset>> getModifiedRestAPIs(String stage, Long assetSyncTime, int bufferTime);
}
