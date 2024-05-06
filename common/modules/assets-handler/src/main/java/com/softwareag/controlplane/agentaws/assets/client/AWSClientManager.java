package com.softwareag.controlplane.agentaws.assets.client;

import com.softwareag.controlplane.agentaws.auth.AWSCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.apigateway.ApiGatewayClient;
import software.amazon.awssdk.services.cloudtrail.CloudTrailClient;

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
     * Creates and configures an API Gateway client
     *
     * @return The configured API Gateway client.
     */
    public ApiGatewayClient apiGatewayClient() {
        return ApiGatewayClient.builder()
                .region(Region.of(region))
                .credentialsProvider(AWSCredentialsProvider.getCredentialsProvider())
                .build();
    }

    /**
     * Creates and configures a CloudTrail client
     *
     * @return The configured CloudTrail client.
     */
    public CloudTrailClient cloudTrailClient() {
        return CloudTrailClient.builder()
                .region(Region.of(region))
                .credentialsProvider(AWSCredentialsProvider.getCredentialsProvider())
                .build();
    }

}