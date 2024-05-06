package com.softwareag.controlplane.agent.aws.configuration;


import com.softwareag.controlplane.agentsdk.api.config.AuthConfig;
import com.softwareag.controlplane.agentsdk.api.config.ControlPlaneConfig;
import com.softwareag.controlplane.agentsdk.api.config.RuntimeConfig;
import com.softwareag.controlplane.agentsdk.api.config.SdkConfig;
import com.softwareag.controlplane.agentsdk.api.config.TlsConfig;
import com.softwareag.controlplane.agentsdk.model.AssetSyncMethod;
import com.softwareag.controlplane.agentsdk.model.Capacity;
import com.softwareag.controlplane.agentsdk.model.Runtime;
import org.apache.logging.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.GetCallerIdentityRequest;
import software.amazon.awssdk.services.sts.model.GetCallerIdentityResponse;

/**
 * Config manager to create SdkConfig with ControlPlaneProperties, AgentProperties, RuntimeProperties and AWSProperties
 */
@Configuration
public class SDKConfigManager {
    @Autowired
    ControlPlaneProperties cpProperties;

    @Autowired
    AgentProperties agentProperties;

    @Autowired
    RuntimeProperties runtimeProperties;

    @Autowired
    private AWSProperties awsProperties;

    @Autowired
    private StsClient stsClient;

    /**
     * Creates and configures an SDK configuration with ControlPlaneProperties, AgentProperties, RuntimeProperties and AWSProperties.
     *
     * @return The configured SDK configuration.
     */
    @Bean
    public SdkConfig sdkConfig() {
        TlsConfig tlsConfig = new TlsConfig
                .Builder(cpProperties.getTrustStorePath(), cpProperties.getTrustStoreType())
                .truststorePassword(cpProperties.getTrustStorePassword())
                .keystorePath(!cpProperties.getKeyStorePath().isEmpty() ? cpProperties.getKeyStorePath() : null)
                .keystorePassword(!cpProperties.getKeyStorePassword().isEmpty() ? cpProperties.getKeyStorePassword() : null)
                .keyAlias(!cpProperties.getKeyAlias().isEmpty() ? cpProperties.getKeyAlias() : null)
                .keyPassword(!cpProperties.getKeyPassword().isEmpty() ? cpProperties.getKeyPassword() : null)
                .keystoreType(!cpProperties.getKeyStoreType().isEmpty() ? cpProperties.getKeyStoreType() : null)
                .build();

        AuthConfig authConfig = new AuthConfig
                .Builder(cpProperties.getUsername(), cpProperties.getPassword())
                .build();

        ControlPlaneConfig controlPlaneConfig = new ControlPlaneConfig.Builder()
                .url(cpProperties.getUrl())
                .authConfig(authConfig)
                .tlsConfig(cpProperties.isSslEnabled() && !cpProperties.getTrustStorePath().isEmpty() && !cpProperties.getTrustStorePassword().isEmpty() ? tlsConfig : null)
                .build();

        Capacity capacity = new Capacity();
        capacity.setUnit(Capacity.TimeUnit.valueOf(runtimeProperties.getCapacityUnit()));
        capacity.setValue(runtimeProperties.getCapacityValue());

        // runtime ID = accountId-region-stage
        String runtimeId = getAccountID().concat("-")
                .concat(awsProperties.getRegion())
                .concat("-")
                .concat(awsProperties.getStage());

        String runtimeHost = String.format("https://%s.console.aws.amazon.com/apigateway", awsProperties.getRegion());

        RuntimeConfig runtimeConfig = new RuntimeConfig.Builder(runtimeId, runtimeProperties.getName(), runtimeProperties.getTypeId(),
                Runtime.DeploymentType.PUBLIC_CLOUD)
                .description(runtimeProperties.getDescription())
                .region(runtimeProperties.getRegion())
                .location(runtimeProperties.getLocation())
                .tags(runtimeProperties.getTags())
                .capacity(capacity)
                .host(runtimeHost)
                .build();

        return new SdkConfig.Builder(controlPlaneConfig, runtimeConfig)
                .publishAssets(agentProperties.isPublishAssets())
                .syncAssets(agentProperties.isSyncAssets())
                .sendMetrics(agentProperties.isSendMetrics())
                .heartbeatInterval(agentProperties.getHeartbeatSendIntervalSeconds())
                .assetsSyncInterval(alignSecondsWith60(agentProperties.getAssetsSyncIntervalSeconds()))
                .metricsSendInterval(alignSecondsWith60(agentProperties.getMetricsSendIntervalSeconds()))
                .assetSyncMethod(AssetSyncMethod.POLLING)
                .logLevel(Level.ERROR)
                .build();
    }

    private static int alignSecondsWith60(int seconds) {
        return seconds - (seconds % 60);
    }

    /**
     * Retrieves the AWS account ID associated
     *
     * @return The AWS account ID.
     */
    private String getAccountID() {
        GetCallerIdentityRequest getCallerIdentityRequest = GetCallerIdentityRequest.builder().build();
        GetCallerIdentityResponse getCallerIdentityResponse = stsClient.getCallerIdentity(getCallerIdentityRequest);
        return getCallerIdentityResponse.account();
    }

}
