package com.softwareag.controlplane.agent.aws.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import software.amazon.awssdk.annotations.NotNull;

/**
 * ControlPlane properties read from application.properties or from environment variables
 */
@ConfigurationProperties(prefix = "apicp.control-plane")
@Getter
@Setter
public class ControlPlaneProperties {
    @NotNull
    private String url;
    @NotNull
    private String username;
    @NotNull
    private String password;
    private boolean sslEnabled;
    private String trustStorePath;
    private String trustStorePassword;
    private String trustStoreType;
    private String keyStorePath;
    private String keyStorePassword;
    private String keyStoreType;
    private String keyAlias;
    private String keyPassword;
}
