package com.softwareag.controlplane.awslambda.heartbeat.retriever;

import com.softwareag.controlplane.agentaws.auth.AWSCredentialsProvider;
import com.softwareag.controlplane.agentaws.heartbeat.manager.impl.HeartbeatManagerImpl;
import com.softwareag.controlplane.agentaws.util.constants.Constants;
import com.softwareag.controlplane.agentaws.util.provider.EnvProvider;
import com.softwareag.controlplane.agentsdk.core.handler.SendHeartbeatHandler;
import com.softwareag.controlplane.agentsdk.model.Heartbeat;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.GetCallerIdentityRequest;
import software.amazon.awssdk.services.sts.model.GetCallerIdentityResponse;

public class HeartbeatRetrieverImpl implements SendHeartbeatHandler.HeartbeatRetriever {
    @Override
    public Heartbeat getHeartbeat() {
        String runtimeId = getAccountID().concat("-")
                .concat(EnvProvider.getEnv("AWS_REGION"))
                .concat("-")
                .concat(EnvProvider.getEnv("AWS_STAGE"));

        return HeartbeatManagerImpl.getInstance(EnvProvider.getEnv(Constants.AWS_REGION))
                .getHeartBeat(runtimeId, EnvProvider.getEnv(Constants.AWS_STAGE));
    }

    public String getAccountID() {
        StsClient stsClient = StsClient.builder()
                .region(Region.of(EnvProvider.getEnv(Constants.AWS_REGION)))
                .credentialsProvider(AWSCredentialsProvider.getCredentialsProvider())
                .build();

        GetCallerIdentityRequest getCallerIdentityRequest = GetCallerIdentityRequest.builder().build();
        GetCallerIdentityResponse getCallerIdentityResponse = stsClient.getCallerIdentity(getCallerIdentityRequest);
        return getCallerIdentityResponse.account();
    }
}
