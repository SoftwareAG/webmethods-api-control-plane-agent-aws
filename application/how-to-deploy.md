
## How to deploy a Spring boot application in Docker

Let’s look at a sample scenario through which you can deploy a spring boot application in Docker using Visual Studio Code editor.

**Pre-requisites**: 

Ensure that you have:

- Started the Docker client.
- API Control Plane, version 11.0.3
- Verified if AWS API Gateway and API Control Plane for which you want to establish connectivity using the Agent are up and running.
- Created Runtime Type in API Control Plane to represent AWS API Gateway. For details, see [Runtime Type Management REST API](https://github.com/SoftwareAG/webmethods-api-control-plane/blob/main/apis/openapi-specifications/runtime-type.yaml). 
- Created an IAM user in AWS with the following roles assigned:
	 - AmazonAPIGatewayAdministrator
	 - AWSCloudTrail_ReadOnlyAccess
	 - CloudWatch ReadOnlyAccess
	 
**To build a Docker image**

1.	Go to webmethods-api-control-plane-agent-aws\application using the following command in the Visual Studio Terminal:
	 ``` cd application ```

2.	Run the following command to build the Docker image:
	 ``` docker build . --tag=<image-name> ```

	 For example: docker build . --tag=aws-agent-appln
	 
	 ![image](https://github.com/SoftwareAG/webmethods-api-control-plane-agent-aws/docs/docker_build)
	 
	 Verify if *aws-agent-appln* image is listed in the docker client.
	 
	 
## How to run the Spring boot application in Docker?

Let’s look at a sample scenario through which you can run the spring boot application in Docker using Visual Studio Code editor.

**To run the Spring boot application in Docker**

1.	Configure API Control Plane agent with a set of properties.

	 Go to *webmethods-api-control-plane-agent-aws\devops\docker-compose* using the following commands in the Visual Studio Terminal:

	 ``` cd devops ```
	 
	 ``` cd docker-compose ```

.env and docker-compose.yml file contains properties related to:

**AWS API Gateway configurations** let you specify AWS Gateway configurations required for AWS connectivity.

| Properties | Description |
|--------------------|-------------------|
| AWS_AGENT_IMAGE | Docker Image name. |
| AWS_REGION | The region name in which the AWS API Gateway service is hosted. |
| AWS_STAGE | Stage (Runtime) name in AWS Gateway. |
| AWS_ACCESS_KEY_ID| Access key of the IAM user.<br>Use the property only if you have specified ENV_VARIABLE as the AwsCredentialsProvider. For details, see [Authentication] (docs/authentication). |	 
| AWS_SECRET_ACCESS_KEY |Secret key of the IAM user. <br>Use the property only if you have specified ENV_VARIABLE as the AwsCredentialsProvider. For details, see [Authentication] (docs/authentication). |
| AWS_METRICS_BY_DATA_OR_STATISTICS | Method in which the metrics are retrieved from CloudWatch.<br>Values:<ul><li>Data</li><li>Statistics</li></ul> |
| AWS_METRICS_SYNC_BUFFER_INTERVAL_SECONDS| The duration in seconds in which AWS API Gateway must synchronize API metrics to AWS CloudWatch. <br>By default, API Gateway metric data is automatically sent to CloudWatch in one-minute interval. This means that most API Gateway metrics will be available in CloudWatch within 1 minute of the original data point.<br>Therefore, set CloudWatch buffer time interval as more than 60 seconds (1 minute). |	 
| AWS_ASSETS_SYNC_BUFFER_INTERVAL_SECONDS | The duration in seconds in which AWS API Gateway must synchronize API events to AWS CloudTrail.<br> CloudTrail typically delivers API Gateway management events within about 5 minutes of the API call being made. This is the standard delivery time for CloudTrail events.<br>Therefore, set CloudTrail event time interval as more than 300 seconds (5 minutes).|	 

**AWS Agent configurations** let you specify the Agent configurations such as heart beat interval, assets sync interval, metrics sync interval, and so on.

| Properties | Description | Possible Values |
|--------------------|-------------------|-------------------|
| APICP_PUBLISH_ASSETS | Enable or disable the publishing of assets to API Control Plane. | <ul><li>true</li><li>false</li></ul><br>Assets are published to API Control Plane whenever the Agent starts, provided that **publishAssets** is set to true |
| APICP_SYNC_ASSETS  | Enable or disable syncing of assets to API Control Plane. | <ul><li>true</li><li>false</li></ul><br>Assets are synchronized periodically according to the configured synchronization values. Within each synchronization interval, only the assets that are newly created, updated, or deleted are synchronized with API Control Plane.|
| APICP_SEND_METRICS   | Enable or disable sending API metrics to API Control Plane. | <ul><li>true</li><li>false</li></ul>|
| APICP_HEARTBEAT_SEND_INTERVAL_SECONDS   | The duration in seconds in which the Agent must send health check status to API Control Plane. | |
| APICP_ASSETS_SYNC_INTERVAL_SECONDS   | The duration in seconds in which the Agent must synchronize the changes made to the assets from CloudTrail to API Control Plane. | |
| APICP_METRICS_SEND_INTERVAL_SECONDS   | The duration in seconds in which the Agent must retrieve the metrics from CloudWatch and send metrics to API Control Plane. | |


**Runtime configurations** lets you specify the metadata of AWS Gateway that you want to administer from the API Control Plane such as Runtime name, Description, Tags, and so on.

| Properties | Description |
|--------------------|-------------------|
| APICP_RUNTIME_NAME |	*Mandatory*. The runtime name. This property defines how you want to identify the runtime in API Control Plane.<br>Name must not exceed 50 characters.|
| APICP_RUNTIME_DESCRIPTION | *Optional*. The runtime description. <br>Description must not exceed 300 characters. |
| APICP_RUNTIME_REGION | *Mandatory*. The region name where the runtime is hosted. <br>Example: EAST US <br>Region name must not exceed 50 characters.|
| APICP_RUNTIME_LOCATION| *Optional*. The location where the runtime is deployed. <br>Example: DENVER<br>Location name can not be empty and must not exceed 50 characters.|	 
| APICP_RUNTIME_TAGS |*Mandatory*. The tag name of the runtime. Tags are used to organize and categorize the runtimes. Multiple tags can be specified by adding comma.<br>Example: test, local, dev<br> Tags must not exceed 50 characters. It must not contain whitespaces and the number of tags must not exceed 100.|
| APICP_RUNTIME_CAPACITY_VALUE | *Optional*. The number of transaction calls that a runtime can process for the specified duration. You can configure the capacity value with any non-negative integer  |
| APICP_RUNTIME_CAPACITY_UNIT| You can configure the capacity value with any non-negative integer and for any duration which can be in the following units:<ul><li>per second</li><li>per minute</li><li>per hour</li><li>per day</li><li>per week</li><li>per month</li><li>per year</li></ul>|	 
| APICP_RUNTIME_TYPE_ID | *Mandatory*. <br>Pre-defined types: WEBMETHODS_DEVELOPER_PORTAL, WEBMETHODS_API_GATEWAY<br>Ensure to verify if the runtime type exists in API Control Plane. If it does not exist, use the Runtime Type Management Service REST API to add the runtime type. For details, see [Runtime Type Management REST API](https://github.com/SoftwareAG/webmethods-api-control-plane/blob/main/apis/openapi-specifications/runtime-type.yaml)|	


**API Control Plane configurations** let you specify API Control Plane details to which AWS API Gateway must establish the connectivity.

| Properties | Description |
|--------------------|-------------------|
| APICP_URL |	*Mandatory*. The valid URL that is used to access API Control Plane.|
| APICP_USERNAME | *Mandatory*. User name that is used to log in to API Control Plane. |
| APICP_PASSWORD | *Mandatory*. Password of the corresponding user name, which is used for logging into the API Control Plane through basic authentication.|
| APICP_SSL_ENABLED | Possible values: true or false|	 
| APICP_TRUSTSTORE_PATH |Location of truststore file.|
| APICP_TRUSTSTORE_PASSWORD | Password to access truststore file. |
| APICP_TRUSTSTORE_TYPE| Type of truststore.|	
| APICP_KEYSTORE_PATH | Location of keystore file.|	 
| APICP_KEYSTORE_PASSWORD |Password to access keystore file.|
| APICP_KEYSTORE_TYPE | Type of keystore. |
| APICP_KEY_ALIAS | Alias of key in the keystore.|	
| APICP_KEY_PASSWORD | Password of key in the keystore.|	


2. Run the following command:

``` docker-compose up -d ```

The output must be as follows:

![image](https://github.com/SoftwareAG/webmethods-api-control-plane-agent-aws/docs/docker_compose)


