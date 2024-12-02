package com.softwareag.controlplane.agent.aws.configuration;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import software.amazon.awssdk.annotations.NotNull;

/**
 * AWS properties read from application.properties or from environment variables
 */
@ConfigurationProperties(prefix = "aws")
@Validated
@Getter
@Setter
public class AWSProperties {
    @NotNull
    private String region;
    @NotNull
    private String stage;
    @NotNull
    private String metricsByDataOrStatistics;
    @Min(600)
    private int metricsSyncBufferIntervalSeconds;
    private int assetsSyncBufferIntervalSeconds;
}
