package com.softwareag.controlplane.agentaws.util.provider;





import com.softwareag.controlplane.agentaws.util.constants.Constants;

import java.util.HashMap;
import java.util.Map;

public class EnvProvider {
    private static final Map<String, String> DEFAULT_VALUES = new HashMap<>();

    static {
        DEFAULT_VALUES.put(Constants.AWS_REGION, "us-east-1");
        DEFAULT_VALUES.put(Constants.AWS_STAGE, "dev");
        DEFAULT_VALUES.put(Constants.AWS_METRICS_BY_DATA_OR_STATISTICS, "data");
        DEFAULT_VALUES.put(Constants.APICP_CONTROLPLANE_URL, "http://localhost:8080");
        DEFAULT_VALUES.put(Constants.APICP_CONTROLPLANE_USERNAME, "administrator");
        DEFAULT_VALUES.put(Constants.APICP_CONTROLPLANE_PASSWORD, "MyPassword@123");
        DEFAULT_VALUES.put(Constants.APICP_HEARTBEAT_SEND_INTERVAL, "60");
        DEFAULT_VALUES.put(Constants.APICP_METRICS_SEND_INTERVAL, "180");
        DEFAULT_VALUES.put(Constants.APICP_ASSETS_SYNC_INTERVAL, "120");
        DEFAULT_VALUES.put(Constants.AWS_METRICS_SYNC_BUFFER_TIME, "60");
        DEFAULT_VALUES.put(Constants.AWS_ASSETS_SYNC_BUFFER_TIME, "60");
        DEFAULT_VALUES.put(Constants.APICP_RUNTIME_NAME, "AWS_DEV_RUNTIME");
        DEFAULT_VALUES.put(Constants.APICP_RUNTIME_TYPE_ID, "aws");
        DEFAULT_VALUES.put(Constants.APICP_RUNTIME_DESCRIPTION, "Runtime for AWS Dev Stage");
        DEFAULT_VALUES.put(Constants.APICP_RUNTIME_REGION, "east-us");
        DEFAULT_VALUES.put(Constants.APICP_RUNTIME_LOCATION, "Chennai");
        DEFAULT_VALUES.put(Constants.APICP_RUNTIME_TAGS, "aws,dev");
        DEFAULT_VALUES.put(Constants.APICP_RUNTIME_CAPACITY, "1000");
        DEFAULT_VALUES.put(Constants.APICP_RUNTIME_CAPACITY_UNIT, "PER_YEAR");
        DEFAULT_VALUES.put(Constants.AWS_CREDENTIALS_PROVIDER,"ENV_VARIABLE");
    }

    public static String getEnv(String variableName) {
        String value = System.getenv(variableName);
        return value != null ? value : DEFAULT_VALUES.getOrDefault(variableName, null);
    }
}
