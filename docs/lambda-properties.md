## Environment Variables applicable for Lambda Functions

Following are the properties that are applicable for **HeartBeat, APIs, and Metrics Lambda** Functions. 

**Amazon API Gateway configurations** let you specify Amazon API Gateway configurations required for AWS connectivity.

| Properties | Description |
|--------------------|-------------------|
| AWS_STAGE |*Mandatory*. Stage (Runtime) name in Amazon API Gateway. | 

**Agent configurations** let you specify the Agent configurations such as heart beat interval, assets sync interval, metrics sync interval, and so on.

| Properties | Description | Possible Values |
|--------------------|-------------------|-------------------|
| APICP_HEARTBEAT_SEND_INTERVAL_SECONDS   |*Optional*. The duration in seconds in which the Agent must send the health check status to API Control Plane. |Min: *15 seconds*<br>Max: *900 seconds* (5 minutes). <br> Default: *60 seconds* <br><br> If you do not specify a value for this property, the default value is considered.|


**Runtime configurations** lets you specify the metadata of Amazon Gateway that you want to administer from the API Control Plane such as Runtime name, Description, Tags, and so on.

| Properties | Description |
|--------------------|-------------------|
| APICP_RUNTIME_NAME |	*Mandatory*. The runtime name. <br> This property defines how you want to identify the runtime in API Control Plane.<br>Name must not exceed 50 characters.<br><br>When the runtime gets registered with API Control Plane for the first time, the current implementation generates the runtime ID in the format <AWS_account_ID>_<AWS_region>_<AWS_stage>. As a result, changing the runtime name does not alter the runtime ID.<br>Even if the runtime name is changed between agent restarts, a new runtime does not get created in API Control Plane. Instead, the runtime name gets updated for the existing runtime.|
| APICP_RUNTIME_DESCRIPTION | *Optional*. The runtime description. <br>Description must not exceed 300 characters.<br> Default value: *AWS Runtime*. If you do not specify a value for this property, the default value is considered. |
| APICP_RUNTIME_REGION | *Mandatory*. The region name where the runtime is hosted. <br>Example: EAST US <br>Region name must not exceed 50 characters.|
| APICP_RUNTIME_LOCATION| *Optional*. The location where the runtime is deployed. <br>Example: DENVER<br>Location name can not be empty and must not exceed 50 characters.|	 
| APICP_RUNTIME_TAGS |*Optional*. The tag name of the runtime. <br>Default: *aws* <br>If you do not specify a value for this property, the default value is considered.<br>Tags are used to organize and categorize the runtimes. Multiple tags can be specified by adding comma.<br>Example: test, local, dev<br> Tags must not exceed 50 characters. It must not contain whitespaces and the number of tags must not exceed 100.<br>|
| APICP_RUNTIME_CAPACITY_VALUE | *Optional*. The number of transaction calls that a runtime can process for the specified duration. You can configure the capacity value with any non-negative integer<br>Default value: *500000*  |
| APICP_RUNTIME_CAPACITY_UNIT| *Optional*. You can configure the capacity value with any non-negative integer and for any duration which can be in the following units:<ul><li>PER_SECOND</li><li>PER_MINUTE</li><li>PER_HOUR</li><li>PER_DAY</li><li>PER_WEEK</li><li>PER_MONTH</li><li>PER_YEAR (Default value)</li></ul><br>If you do not specify a value for this property, the default value is considered.|	 
| APICP_RUNTIME_TYPE_ID | *Mandatory*. The Id of the runtime type. <br>Pre-defined types: WEBMETHODS_DEVELOPER_PORTAL, WEBMETHODS_API_GATEWAY<br>Ensure to verify if the runtime type exists in API Control Plane. If it does not exist, use the Runtime Type Management Service REST API to add the runtime type. For details, see [How to create the runtime type?](../docs/runtime_service_mgmt_api.md)|	


**API Control Plane configurations** let you specify API Control Plane details to which Amazon API Gateway must establish the connectivity.

| Properties | Description |
|--------------------|-------------------|
| APICP_URL |	*Mandatory*. The valid URL that is used to access API Control Plane.|
| APICP_USERNAME | *Mandatory*. User name that is used to log in to API Control Plane. |
| APICP_PASSWORD | *Mandatory*. Password of the corresponding user name, which is used for logging into the API Control Plane through basic authentication.|
| APICP_SSL_ENABLED | *Optional*. The SSL certification of API Control Plane.<br>Possible values: *true* or *false*|	 
| APICP_TRUSTSTORE_PATH |*Optional*. Location of the truststore file.<br>If APICP_SSL_ENABLED is set to *true*, you must specify a value for this property.|
| APICP_TRUSTSTORE_PASSWORD |*Optional*. Password to access the truststore file. <br>If APICP_SSL_ENABLED is set to *true*, you must specify a value for this property. |
| APICP_TRUSTSTORE_TYPE| *Optional*. Type of the truststore.<br>If APICP_SSL_ENABLED is set to *true*, you must specify a value for this property.|	
| APICP_KEYSTORE_PATH | *Optional*. Location of the keystore file.<br>If APICP_SSL_ENABLED is set to *true*, you must specify a value for this property.|	 
| APICP_KEYSTORE_PASSWORD |*Optional*. Password to access the keystore file.<br>If APICP_SSL_ENABLED is set to *true*, you must specify a value for this property.|
| APICP_KEYSTORE_TYPE | *Optional*. Type of the keystore.<br>If APICP_SSL_ENABLED is set to *true*, you must specify a value for this property. |
| APICP_KEY_ALIAS | *Optional*. Alias of key in the keystore.<br>If APICP_SSL_ENABLED is set to *true*, you must specify a value for this property.|	
| APICP_KEY_PASSWORD | *Optional*. Password of key in the keystore.<br>If APICP_SSL_ENABLED is set to *true*, you must specify a value for this property.|

Following are the properties that are specific to **Metrics Lambda Function**. 

| Properties | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                |
|--------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| AWS_METRICS_BY_DATA_OR_STATISTICS | *Optional*. Method in which the metrics are retrieved from CloudWatch.<br>Values:<ul><li>Data</li><li>Statistics</li></ul><br>The default value is *Statistics*. If you do not specify a value for this property, the default value is considered.                                                                                                                                                                                                                                                                                         |
| AWS_METRICS_SYNC_BUFFER_INTERVAL_SECONDS| *Optional*. By default, Amazon API Gateway metric data is automatically sent to Amazon CloudWatch in one-minute interval. That is, most Amazon API Gateway metrics will be available in Amazon CloudWatch within 1 minute of the original data point.<br><br>But to ensure proper metrics sync, set CloudWatch buffer time interval as more than 600 seconds (10 minutes).<br> The default value is *600 seconds*. If you do not specify a value for this property, the default value is considered.                                       |
| APICP_METRICS_SEND_INTERVAL_SECONDS   | *Optional*. The duration in seconds in which the Agent must retrieve the metrics from Amazon CloudWatch and send metrics to API Control Plane. <br><br>Supported metric synchronization values: <br> <ul><li>*60 seconds* (1 minute)(Default value)</li><li>*300 seconds* (5 minutes)</li><li>*600 seconds* (10 minutes)</li><li>*1800 seconds* (30 minutes)</li><li>*3600 seconds* (60 minutes)</li><li>*7200 seconds* (120 minutes)</li></ul> <br><br> If you do not specify a value for this property, the default value is considered. | 


Following are the properties that are specific to **Assets Lambda Function**. 

| Properties | Description |
|--------------------|-------------------|
| APICP_ASSETS_SYNC_INTERVAL_SECONDS   |*Optional*. The duration in seconds in which the Agent must synchronize the changes made to the assets from the Amazon CloudTrail to API Control Plane.<br><br> Min:*60 seconds*<br>Max: *21600 seconds* (6 hours). <br> Default: *300 seconds* <br><br> If you do not specify a value for this property, the default value is considered. |
| AWS_ASSETS_SYNC_BUFFER_INTERVAL_SECONDS |*Mandatory*. CloudTrail typically delivers API Gateway management events within about 5 minutes of the API call being made. This is the standard delivery time for CloudTrail events.<br><br>Therefore, set CloudTrail event time interval as more than 300 seconds (5 minutes). <br> The default value is 300 seconds. If you do not specify a value for this property, the default value is considered.|	

