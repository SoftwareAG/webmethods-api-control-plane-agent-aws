package com.softwareag.controlplane.agentaws.heartbeat.client;

import com.softwareag.controlplane.agentaws.auth.AWSCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.apigateway.ApiGatewayClient;

/**
 * Creates AWS clients with region
 */
public final class AWSClientManager {
    private final String region;
    private static AWSClientManager awsClientManager;

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
        return ApiGatewayClient.builder().region(Region.of(region)).
                credentialsProvider(AWSCredentialsProvider.getCredentialsProvider()).build();
    }

}