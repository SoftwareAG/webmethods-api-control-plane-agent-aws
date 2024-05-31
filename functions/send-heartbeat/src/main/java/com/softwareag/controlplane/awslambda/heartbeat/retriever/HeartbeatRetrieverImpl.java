package com.softwareag.controlplane.awslambda.heartbeat.retriever;

import com.softwareag.controlplane.agentaws.heartbeat.manager.impl.HeartbeatManagerImpl;
import com.softwareag.controlplane.awslambda.util.Utils;
import com.softwareag.controlplane.awslambda.util.constants.Constants;
import com.softwareag.controlplane.awslambda.util.provider.EnvProvider;
import com.softwareag.controlplane.agentsdk.core.handler.SendHeartbeatHandler;
import com.softwareag.controlplane.agentsdk.model.Heartbeat;

/**
 * Implementation of the HeartbeatRetriever interface for retrieving Heartbeat.
 * This implementation is intended to be used with the SendHeartbeatHandler class.
 */
public final class HeartbeatRetrieverImpl implements SendHeartbeatHandler.HeartbeatRetriever {
    /**
     * Retrieves Heartbeat from the API runtime.
     *
     * @return {@link Heartbeat}
     */
    @Override
    public Heartbeat getHeartbeat() {
        return HeartbeatManagerImpl.getInstance(EnvProvider.getEnv(Constants.AWS_REGION))
                .getHeartBeat(Utils.getRuntimeId(), EnvProvider.getEnv(Constants.AWS_STAGE));
    }
}
