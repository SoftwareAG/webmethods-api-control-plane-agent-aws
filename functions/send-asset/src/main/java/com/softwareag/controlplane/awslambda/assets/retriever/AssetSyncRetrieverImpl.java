package com.softwareag.controlplane.awslambda.assets.retriever;

import com.softwareag.controlplane.agentaws.assets.manager.impl.AssetsManagerImpl;
import com.softwareag.controlplane.agentaws.util.constants.Constants;
import com.softwareag.controlplane.agentaws.util.provider.EnvProvider;
import com.softwareag.controlplane.agentsdk.core.handler.SyncAssetsHandler;
import com.softwareag.controlplane.agentsdk.model.Asset;
import com.softwareag.controlplane.agentsdk.model.AssetSyncAction;

import java.util.List;

public class AssetSyncRetrieverImpl implements SyncAssetsHandler.AssetsRetriever {
    @Override
    public List<AssetSyncAction<Asset>> getAssetSyncActions(long l, long l1) {
        return AssetsManagerImpl.getInstance(EnvProvider.getEnv(Constants.AWS_REGION))
                .getModifiedRestAPIs(EnvProvider.getEnv(Constants.AWS_STAGE), l,
                        Integer.parseInt(EnvProvider.getEnv(Constants.AWS_ASSETS_SYNC_BUFFER_TIME)));
    }
}
