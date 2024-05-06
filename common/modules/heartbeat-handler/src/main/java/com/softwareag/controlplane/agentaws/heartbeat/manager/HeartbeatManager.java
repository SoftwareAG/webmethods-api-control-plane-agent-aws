package com.softwareag.controlplane.agentaws.heartbeat.manager;

import com.softwareag.controlplane.agentsdk.model.Heartbeat;

/**
 * Interface for managing heartbeat operations using Api Gateway client
 */
public interface HeartbeatManager {

    /**
     * Retrieves a heartbeat object for the specified stage.
     *
     * @param runtimeId The runtime ID of the heartbeat object.
     * @param stage     The stage for which the agent is configured.
     * @return The heartbeat object.
     */
    Heartbeat getHeartBeat(String runtimeId, String stage);

}
