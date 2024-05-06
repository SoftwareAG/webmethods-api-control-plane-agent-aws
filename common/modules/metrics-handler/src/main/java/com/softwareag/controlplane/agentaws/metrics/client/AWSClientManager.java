package com.softwareag.controlplane.agentaws.metrics.client;

import com.softwareag.controlplane.agentaws.auth.AWSCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;

/**
 * Creates AWS clients with region
 */
public class AWSClientManager {
    private static AWSClientManager awsClientManager;
    private final String region;

    private AWSClientManager(String region) {
        this.region = region;
    }

    /**
     * Returns the singleton instance of AWSClientManager, initializing it if necessary.
     *
     * @param region    The AWS region.
     * @return The singleton instance of AWSClientManager.
     */
    public static AWSClientManager getInstance(String region) {
        if(awsClientManager != null) {
            return awsClientManager;
        }
        awsClientManager = new AWSClientManager(region);
        return awsClientManager;
    }

    /**
     * Creates and configures an CloudWatch client
     *
     * @return The configured CloudWatch client.
     */
    public CloudWatchClient cloudWatchClient() {
        return CloudWatchClient.builder().region(Region.of(region)).
                credentialsProvider(AWSCredentialsProvider.getCredentialsProvider()).build();
    }

}
