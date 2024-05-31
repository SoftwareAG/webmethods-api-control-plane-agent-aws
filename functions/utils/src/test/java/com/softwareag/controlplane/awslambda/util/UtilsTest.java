package com.softwareag.controlplane.awslambda.util;

import com.softwareag.controlplane.awslambda.util.constants.Constants;
import com.softwareag.controlplane.awslambda.util.provider.EnvProvider;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.junit.jupiter.api.Assertions.assertNull;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;


class UtilsTest {

    @Test
    void testGetLastActionSyncTime() throws IOException{
        Long assetTime = 123456789L;
        Long metricTime = 234567891L;
        Long heartbeatTime = 345678912L;
        Long registrationTime = 456789123L;
        String baseResponse = "{\"lastHeartbeatTime\":\"%d\", \"lastMetricsTime\":\"%d\", \"lastAssetSyncTime\":\"%d\", \"lastRegistrationTime\":\"%d\", \"runtimeId\":\"%s\", \"name\":\"testRuntime\"}";
        String response = String.format(baseResponse,heartbeatTime,metricTime,assetTime,registrationTime,"as3ded23d2-d32dc23-d32d23d1");

        Long lastAssetSyncTime = Utils.getLastActionSyncTime(response,Constants.SYNC_ASSET_ACTION);
        assertEquals(lastAssetSyncTime,assetTime);

        Long lastMetricSyncTime = Utils.getLastActionSyncTime(response,Constants.SEND_METRIC_ACTION);
        assertEquals(lastMetricSyncTime,metricTime);

        Long lastHeartbeatSyncTime = Utils.getLastActionSyncTime(response,Constants.SEND_HEARTBEAT_ACTION);
        assertEquals(lastHeartbeatSyncTime,heartbeatTime);

        Long lastUnknownSyncTime = Utils.getLastActionSyncTime(response, "unknown_action");
        assertNull(lastUnknownSyncTime);
    }

    @Test
    void testGetRuntimeTagsFromEnv() {
        try (MockedStatic<EnvProvider> envProvider = Mockito.mockStatic(EnvProvider.class)) {
            envProvider.when(() -> EnvProvider.getEnv(Constants.APICP_RUNTIME_TAGS))
                    .thenReturn("tag1,tag2,tag3");

            Set<String> result = Utils.getRuntimeTagsFromEnv();
            assertEquals(new HashSet<>(Arrays.asList("tag1", "tag2", "tag3")), result);
        }
    }

}
