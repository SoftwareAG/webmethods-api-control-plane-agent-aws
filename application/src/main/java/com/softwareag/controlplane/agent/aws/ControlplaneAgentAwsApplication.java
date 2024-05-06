package com.softwareag.controlplane.agent.aws;

import com.softwareag.controlplane.agent.aws.configuration.AWSProperties;
import com.softwareag.controlplane.agent.aws.configuration.AgentProperties;
import com.softwareag.controlplane.agent.aws.configuration.ControlPlaneProperties;
import com.softwareag.controlplane.agent.aws.configuration.RuntimeProperties;
import com.softwareag.controlplane.agentsdk.api.AgentSdkContext;
import com.softwareag.controlplane.agentsdk.core.AgentSdk;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({AgentProperties.class, AWSProperties.class,
        ControlPlaneProperties.class, RuntimeProperties.class})
public class ControlplaneAgentAwsApplication implements CommandLineRunner {

    @Autowired
    private AgentSdkContext agentSdkContext;

    public static void main(String[] args) {
        SpringApplication.run(ControlplaneAgentAwsApplication.class, args);
    }

    /**
     * Entry point for the AWS Agent application.
     *
     * @param args Command-line arguments.
     * @throws Exception If an error occurs during application startup.
     */
    @Override
    public void run(String... args) throws Exception {
        AgentSdk.initialize(agentSdkContext);
    }
}
