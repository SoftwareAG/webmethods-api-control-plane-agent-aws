package com.softwareag.controlplane.agent.aws.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Agent properties read from application.properties or from environment variables
 */
@ConfigurationProperties(prefix = "apicp")
@Getter
@Setter
public class AgentProperties {

    private boolean publishAssets;
    private boolean syncAssets;
    private boolean sendMetrics;
    private int heartbeatSendIntervalSeconds;
    private int metricsSendIntervalSeconds;
    private int assetsSyncIntervalSeconds;
}
