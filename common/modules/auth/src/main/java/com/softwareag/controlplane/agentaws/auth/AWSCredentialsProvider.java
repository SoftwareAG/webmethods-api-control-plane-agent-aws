package com.softwareag.controlplane.agentaws.auth;

import software.amazon.awssdk.auth.credentials.*;

public class AWSCredentialsProvider {

    private static AwsCredentialsProvider credentialsProvider = null;

    public static final String ENV_AWS_CREDENTIAL_PROVIDER_TYPE = "AWS_CREDENTIALS_PROVIDER";

    public static AwsCredentialsProvider getCredentialsProvider() {
        if (credentialsProvider == null) {
            //Return the singleton if it is already initialized. If not create it.
            credentialsProvider = create();
        }
        return credentialsProvider;
    }

    private static AwsCredentialsProvider create() {
        String providerType = readValue(ENV_AWS_CREDENTIAL_PROVIDER_TYPE);

        if(providerType == null) {
            return EnvironmentVariableCredentialsProvider.create();
        }

        switch(ProviderType.valueOf(providerType.toUpperCase())) {
            case SYSTEM_PROPERTY:
                return SystemPropertyCredentialsProvider.create();
            case PROFILE:
                return ProfileCredentialsProvider.create();
            case CONTAINER:
                return ContainerCredentialsProvider.builder().build();
            case INSTANCE_PROFILE:
                return InstanceProfileCredentialsProvider.create();
            case PROCESS:
                return ProcessCredentialsProvider.builder().build();
            case WEB_IDENTITY_TOKEN_FILE, DEFAULT:
                return WebIdentityTokenFileCredentialsProvider.create();
            case ENV_VARIABLE:
            default:
                return EnvironmentVariableCredentialsProvider.create();
        }
    }

    public static String readValue(String key) {
        String property = System.getenv(key);
        if(property == null) {
            property = System.getProperty(key);
        }
        return property;
    }

    public enum ProviderType {
        SYSTEM_PROPERTY,
        ENV_VARIABLE,
        PROFILE,
        WEB_IDENTITY_TOKEN_FILE,
        CONTAINER,
        INSTANCE_PROFILE,
        PROCESS,
        DEFAULT
    }
}
