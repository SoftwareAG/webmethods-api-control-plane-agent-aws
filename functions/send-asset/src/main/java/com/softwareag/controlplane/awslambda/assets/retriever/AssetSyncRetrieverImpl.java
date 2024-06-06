package com.softwareag.controlplane.awslambda.assets.retriever;

import com.softwareag.controlplane.agentaws.assets.manager.impl.AssetsManagerImpl;
import com.softwareag.controlplane.awslambda.util.constants.Constants;
import com.softwareag.controlplane.awslambda.util.provider.EnvProvider;
import com.softwareag.controlplane.agentsdk.core.handler.SyncAssetsHandler;
import com.softwareag.controlplane.agentsdk.model.Asset;
import com.softwareag.controlplane.agentsdk.model.AssetSyncAction;

import java.util.List;

/**
 * Implementation of the AssetsRetriever interface for retrieving assets sync actions.
 * This implementation is intended to be used with the SyncAssetsHandler class.
 */
public class AssetSyncRetrieverImpl implements SyncAssetsHandler.AssetsRetriever {

    /**
     * This method is responsible to get the list of asset sync actions to be synced to Control Plane.
     * The list of assets is collected based on the differential assets between fromTime and toTime.
     *
     * @param l -  timestamp from which the assets need to be queried. In milliseconds.
     * @param l1   -  timestamp upto which the assets need to be queried. In milliseconds.
     * @return List of {@link AssetSyncAction}
     */
    @Override
    public List<AssetSyncAction<Asset>> getAssetSyncActions(long l, long l1) {
        return AssetsManagerImpl.getInstance(EnvProvider.getEnv(Constants.AWS_REGION))
                .getModifiedRestAPIs(EnvProvider.getEnv(Constants.AWS_STAGE), l,
                        Integer.parseInt(EnvProvider.getEnv(Constants.AWS_ASSETS_SYNC_BUFFER_INTERVAL_SECONDS)));
    }
}
