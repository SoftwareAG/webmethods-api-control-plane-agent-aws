package com.softwareag.controlplane.awslambda.util.provider;

import com.softwareag.controlplane.awslambda.util.constants.Constants;
import org.apache.commons.lang3.ObjectUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides default values of Environment variables for FAAS.
 */
public final class EnvProvider {
    private EnvProvider(){}

    private static final Map<String, String> DEFAULT_VALUES = new HashMap<>();

    static {
        DEFAULT_VALUES.put(Constants.AWS_METRICS_BY_DATA_OR_STATISTICS, "statistics");
        DEFAULT_VALUES.put(Constants.APICP_HEARTBEAT_SEND_INTERVAL_SECONDS, "60");
        DEFAULT_VALUES.put(Constants.APICP_METRICS_SEND_INTERVAL_SECONDS, "60");
        DEFAULT_VALUES.put(Constants.APICP_ASSETS_SYNC_INTERVAL_SECONDS, "300");
        DEFAULT_VALUES.put(Constants.AWS_METRICS_SYNC_BUFFER_INTERVAL_SECONDS, "600");
        DEFAULT_VALUES.put(Constants.AWS_ASSETS_SYNC_BUFFER_INTERVAL_SECONDS, "300");
        DEFAULT_VALUES.put(Constants.APICP_RUNTIME_DESCRIPTION, "AWS Runtime");
        DEFAULT_VALUES.put(Constants.APICP_RUNTIME_TAGS, "aws");
        DEFAULT_VALUES.put(Constants.APICP_RUNTIME_CAPACITY_VALUE, "500000");
        DEFAULT_VALUES.put(Constants.APICP_RUNTIME_CAPACITY_UNIT, "PER_YEAR");
    }

    /**
     * Retrieves the value of the specified environment variable.
     *
     * @param variableName The name of the environment variable to retrieve.
     * @return The value of the specified environment variable, or a default value if not found.
     */
    public static String getEnv(String variableName) {
        String value = System.getenv(variableName);
        if(variableName.equals(Constants.APICP_RUNTIME_REGION) && (ObjectUtils.isEmpty(value) && ObjectUtils.isNotEmpty(System.getenv(Constants.AWS_REGION)))) {
            return System.getenv(Constants.AWS_REGION);
        }
        return value != null ? value : DEFAULT_VALUES.getOrDefault(variableName, null);
    }
}
