package com.softwareag.controlplane.agent.aws.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

/**
 * Runtime properties read from application.properties or from environment variables
 */
@ConfigurationProperties(prefix = "apicp.runtime")
@Getter
@Setter
public class RuntimeProperties {
    private String name;
    private String description;
    private String region;
    private String location;
    private Set<String> tags;
    private long capacityValue;
    private String capacityUnit;
    private String typeId;
}
