package com.softwareag.controlplane.agent.aws.configuration;

import com.softwareag.controlplane.agentaws.auth.AWSCredentialsProvider;
import com.softwareag.controlplane.agentaws.heartbeat.manager.impl.HeartbeatManagerImpl;
import com.softwareag.controlplane.agentaws.assets.manager.impl.AssetsManagerImpl;
import com.softwareag.controlplane.agentaws.metrics.manager.impl.MetricsManagerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sts.StsClient;

/**
 * Configuration class responsible for managing AWS agent specific beans and configurations.
 */
@Configuration
public class AWSAgentConfigManager {

    @Autowired
    private AWSProperties awsProperties;

    /**
     * Creates and provides an instance of HeartbeatManagerImpl, configured with AWS credentials and region.
     *
     * @return The configured HeartbeatManagerImpl instance.
     */
    @Bean
    public HeartbeatManagerImpl heartbeatManager() {
        return HeartbeatManagerImpl.getInstance(awsProperties.getRegion());
    }

    /**
     * Creates and provides an instance of MetricsManagerImpl, configured with AWS credentials, region, stage, and metrics preferences.
     *
     * @return The configured MetricsManagerImpl instance.
     */
    @Bean
    public MetricsManagerImpl metricsManager() {
        return MetricsManagerImpl.getInstance(awsProperties.getRegion(), awsProperties.getStage(), awsProperties.getMetricsByDataOrStatistics());
    }

    /**
     * Creates and provides an instance of AssetsManagerImpl, configured with AWS credentials and region.
     *
     * @return The configured AssetsManagerImpl instance.
     */
    @Bean
    public AssetsManagerImpl assetsManager() {
        return AssetsManagerImpl.getInstance(awsProperties.getRegion());
    }

    /**
     * Creates and configures an AWS Security Token Service (STS) client for accessing AWS resources.
     *
     * @return The configured STS client.
     */
    @Bean
    public StsClient stsClient() {
        return StsClient.builder()
                .region(Region.of(awsProperties.getRegion()))
                .credentialsProvider(AWSCredentialsProvider.getCredentialsProvider())
                .build();
    }
}
