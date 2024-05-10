package com.softwareag.controlplane.awslambda.assets.retriever;

import com.softwareag.controlplane.agentaws.assets.manager.impl.AssetsManagerImpl;

import com.softwareag.controlplane.agentaws.util.constants.Constants;
import com.softwareag.controlplane.agentaws.util.provider.EnvProvider;
import com.softwareag.controlplane.agentsdk.core.handler.SyncAssetsHandler;
import com.softwareag.controlplane.agentsdk.model.*;

import java.util.ArrayList;
import java.util.List;

public class AssetsRetrieverImpl implements SyncAssetsHandler.AssetsRetriever {
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
