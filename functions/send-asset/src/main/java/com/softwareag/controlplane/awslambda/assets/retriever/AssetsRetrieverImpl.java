package com.softwareag.controlplane.awslambda.assets.retriever;

import com.softwareag.controlplane.agentaws.assets.manager.impl.AssetsManagerImpl;

import com.softwareag.controlplane.agentsdk.model.API;
import com.softwareag.controlplane.agentsdk.model.APISyncAction;
import com.softwareag.controlplane.agentsdk.model.Asset;
import com.softwareag.controlplane.agentsdk.model.AssetType;
import com.softwareag.controlplane.agentsdk.model.AssetSyncAction;
import com.softwareag.controlplane.awslambda.util.constants.Constants;
import com.softwareag.controlplane.awslambda.util.provider.EnvProvider;
import com.softwareag.controlplane.agentsdk.core.handler.SyncAssetsHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the AssetsRetriever interface for retrieving assets.
 * This implementation is intended to be used with the SyncAssetsHandler class.
 */
public class AssetsRetrieverImpl implements SyncAssetsHandler.AssetsRetriever {

    /**
     * This method is responsible to get the list of create asset sync actions to be published to Control Plane.
     *
     * @param l -  timestamp from which the assets need to be queried. In milliseconds.
     * @param l1   -  timestamp upto which the assets need to be queried. In milliseconds.
     * @return List of {@link AssetSyncAction}
     */
    @Override
    public List<AssetSyncAction<Asset>> getAssetSyncActions(long l, long l1) {
        return convertAPIsToAPISyncAction();
    }

    private List<AssetSyncAction<Asset>> convertAPIsToAPISyncAction() {
        List<AssetSyncAction<Asset>> assetSyncActions = new ArrayList<>();
        List<API> apis = AssetsManagerImpl.getInstance(EnvProvider.getEnv(Constants.AWS_REGION)).getRestAPIs(EnvProvider.getEnv(Constants.AWS_STAGE), true);
        for (API api : apis) {
            APISyncAction createAction = new APISyncAction(AssetType.API,
                    AssetSyncAction.SyncType.valueOf(com.softwareag.controlplane.agentaws.assets.constants.Constants.CREATE), api, System.currentTimeMillis());
            assetSyncActions.add((AssetSyncAction) createAction);
        }
        return assetSyncActions;
    }
}
