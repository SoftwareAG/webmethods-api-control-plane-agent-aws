package com.softwareag.controlplane.agentaws.assets.constants;

public final class Constants {
    private Constants() {
    }

    public static final String CREATE_STAGE_EVENT = "CreateStage";
    public static final String UPDATE_STAGE_EVENT = "UpdateStage";
    public static final String DELETE_STAGE_EVENT = "DeleteStage";
    public static final String DELETE_REST_API_EVENT = "DeleteRestApi";
    public static final String CREATE_DEPLOYMENT_EVENT = "CreateDeployment";
    public static final String CLOUD_TRAIL_EVENT_FIELD = "CloudTrailEvent";
    public static final String TAG_RESOURCE_EVENT = "TagResource";
    public static final String UNTAG_RESOURCE_EVENT = "UntagResource";

    public static final String CREATE = "CREATE";
    public static final String UPDATE = "UPDATE";
    public static final String DELETE = "DELETE";
    public static final String REST_API_ID = "restApiId";
    public static final String CLOUD_TRAIL_REQUEST_PARAMETERS = "requestParameters";
    public static final String REST_API_OPEN_API_3_0 = "oas30";
    public static final String OPEN_API_INFO = "info";
    public static final String OPEN_API_TITLE = "title";
    public static final String REST_API_POLICY = "x-amazon-apigateway-policy";
    public static final String REST_API_POLICY_STATEMENT = "Statement";
    public static final String RESOURCE_ARN = "resourceArn";

}

