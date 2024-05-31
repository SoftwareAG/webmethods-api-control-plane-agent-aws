package com.softwareag.controlplane.agentaws.assets.manager.impl;

import com.softwareag.controlplane.agentaws.assets.constants.Constants;
import com.softwareag.controlplane.agentaws.assets.utils.CloudTrailUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import software.amazon.awssdk.services.cloudtrail.CloudTrailClient;
import software.amazon.awssdk.services.cloudtrail.model.Event;
import software.amazon.awssdk.services.cloudtrail.model.LookupAttribute;
import software.amazon.awssdk.services.cloudtrail.model.LookupEventsRequest;
import software.amazon.awssdk.services.cloudtrail.model.LookupEventsResponse;
import java.util.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CloudTrailManagerImplTest {

    @Mock
    CloudTrailClient cloudTrailClient;
    CloudTrailManagerImpl cloudTrailManager;

    @BeforeEach
    public void setup(){
        MockitoAnnotations.openMocks(this);
        cloudTrailManager = CloudTrailManagerImpl.getInstance(cloudTrailClient);
    }

    @Test
    void testGetOwnerForRestApis() {
        mockLookupEvents();
        Map<String, String> ownerList = cloudTrailManager.getOwnerForRestApis();

        assertEquals(2, ownerList.size());
        assertEquals("aws-user-1@softwareag.com", ownerList.get("id1"));
        assertEquals("aws-user-2@softwareag.com", ownerList.get("id2"));
    }

    @Test
    void testGetModifiedAPIs() {
        mockLookupEvents();
        Map<String, Map<String,String>> ownerList = cloudTrailManager.getModifiedAPIs(1200010020L,300);

        assertEquals(3, ownerList.size());

        assertEquals(2, ownerList.get(Constants.CREATE).size());
        assert(ownerList.get(Constants.CREATE).containsKey("id1"));
        assertEquals("aws-user-1@softwareag.com", ownerList.get(Constants.CREATE).get("id1"));
        assertEquals(2, ownerList.get(Constants.CREATE).size());
        assert(ownerList.get(Constants.CREATE).containsKey("id2"));
        assertEquals("aws-user-2@softwareag.com", ownerList.get(Constants.CREATE).get("id2"));

        assertEquals(1, ownerList.get(Constants.UPDATE).size());
        assert(ownerList.get(Constants.UPDATE).containsKey("id3"));
        assertNull(ownerList.get(Constants.UPDATE).get("id3"));

        assertEquals(1,ownerList.get(Constants.DELETE).size());
        assert(ownerList.get(Constants.DELETE).containsKey("id4"));
        assertNull(ownerList.get(Constants.DELETE).get("id4"));

    }

    public void mockLookupEvents() {
        LookupEventsResponse mockLookupEventResponse = mock(LookupEventsResponse.class);

        Event createEvent1 = mock(Event.class);
        String cloudTrialEvent1 = "{\n"
                + "  \"requestParameters\": {\n"
                + "    \"restApiId\": \"id1\",\n"
                + "    \"createDeploymentInput\": {\n"
                + "      \"description\": \"\",\n"
                + "      \"stageName\": \"prod\"\n"
                + "    },\n"
                + "    \"template\": false\n"
                + "  }\n"
                + "}";
        when(createEvent1.getValueForField(Constants.CLOUD_TRAIL_EVENT_FIELD, String.class)).thenReturn(Optional.of(cloudTrialEvent1));
        when(createEvent1.username()).thenReturn("aws-user-1@softwareag.com");
        when(createEvent1.eventName()).thenReturn(Constants.CREATE_DEPLOYMENT_EVENT);

        Event createEvent2 = mock(Event.class);
        String cloudTrialEvent2 = "{\n"
                + "  \"requestParameters\": {\n"
                + "    \"restApiId\": \"id2\",\n"
                + "    \"createDeploymentInput\": {\n"
                + "      \"description\": \"\",\n"
                + "      \"stageName\": \"prod\"\n"
                + "    },\n"
                + "    \"template\": false\n"
                + "  }\n"
                + "}";
        when(createEvent2.getValueForField(Constants.CLOUD_TRAIL_EVENT_FIELD, String.class)).thenReturn(Optional.of(cloudTrialEvent2));
        when(createEvent2.username()).thenReturn("aws-user-2@softwareag.com");
        when(createEvent2.eventName()).thenReturn(Constants.CREATE_STAGE_EVENT);

        Event updateEvent = mock(Event.class);
        String cloudTrialEvent3 = "{\n"
                + "  \"requestParameters\": {\n"
                + "    \"createDeploymentInput\": {\n"
                + "      \"description\": \"\",\n"
                + "      \"stageName\": \"prod\"\n"
                + "    },\n"
                + "    \"template\": false,\n"
                + "  \"resourceArn\": \"arn:aws:apigateway:us-east-1::/restapis/id3\"\n"
                + "  },\n"
                + "}";
        when(updateEvent.getValueForField(Constants.CLOUD_TRAIL_EVENT_FIELD, String.class)).thenReturn(Optional.of(cloudTrialEvent3));
        when(updateEvent.username()).thenReturn("aws-user-3@softwareag.com");
        when(updateEvent.eventName()).thenReturn(Constants.TAG_RESOURCE_EVENT);

        Event deleteEvent = mock(Event.class);
        String cloudTrialEvent4= "{\n"
                + "  \"requestParameters\": {\n"
                + "    \"restApiId\": \"id4\",\n"
                + "    \"createDeploymentInput\": {\n"
                + "      \"description\": \"\",\n"
                + "      \"stageName\": \"prod\"\n"
                + "    },\n"
                + "    \"template\": false\n"
                + "  }\n"
                + "}";
        when(deleteEvent.getValueForField(Constants.CLOUD_TRAIL_EVENT_FIELD, String.class)).thenReturn(Optional.of(cloudTrialEvent4));
        when(deleteEvent.username()).thenReturn("aws-user-4@softwareag.com");
        when(deleteEvent.eventName()).thenReturn(Constants.DELETE_STAGE_EVENT);


        when(mockLookupEventResponse.nextToken()).thenReturn(null);
        when(cloudTrailClient.lookupEvents(any(LookupEventsRequest.class))).thenAnswer(
                invocation -> {
                    LookupEventsRequest request = invocation.getArgument(0);
                    List<LookupAttribute> attributes = request.lookupAttributes();
                    String nextToken = request.nextToken();
                    if(nextToken != null) {
                        List<Event> eventList = new ArrayList<>();
                        when(mockLookupEventResponse.events()).thenReturn(eventList);
                        when(mockLookupEventResponse.nextToken()).thenReturn(null);
                    }
                    else if(CloudTrailUtil.getApiCreateEvents().contains(attributes.get(0).attributeValue())) {
                        List<Event> eventList = new ArrayList<>();
                        eventList.add(createEvent1);
                        eventList.add(createEvent2);
                        when(mockLookupEventResponse.events()).thenReturn(eventList);
                        when(mockLookupEventResponse.nextToken()).thenReturn("dummy-token");
                    }
                    else if(CloudTrailUtil.getApiUpdateEvents().contains(attributes.get(0).attributeValue())) {
                        List<Event> eventList = new ArrayList<>();
                        eventList.add(updateEvent);
                        when(mockLookupEventResponse.events()).thenReturn(eventList);
                    }
                    else {
                        List<Event> eventList = new ArrayList<>();
                        eventList.add(deleteEvent);
                        when(mockLookupEventResponse.events()).thenReturn(eventList);
                    }
                    return mockLookupEventResponse;
                });
    }
}
