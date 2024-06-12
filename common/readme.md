## Customize the Implementation Logic for Retrieval of Heartbeats, APIs, and Metrics from the AWS API Gateway

The **Common** directory contains the shared code utilized by both the Spring Boot application and Lambda functions.

Within this directory, the **Manager** classes are implemented to facilitate the retrieval of heartbeats, APIs, and metrics from the AWS API Gateway. To customize the code according to your requirement, navigate to the respective *Manager* class directory and modify the relevant implementation files as required.

[AssetsManager](../common/modules/assets-handler/src/main/java/com/softwareag/controlplane/agentaws/assets/manager/impl)

The AssetsManager class contains the following files:

| Class Name | Description                                                       |
|-------------|-------------------------------------------------------------------|
| [AssetsManagerImpl](../common/modules/assets-handler/src/main/java/com/softwareag/controlplane/agentaws/assets/manager/impl/AssetsManagerImpl.java) | Contains code required for retrieving all the assets (APIs) from Amazon API Gateway. When the Amazon API Gateway is first registered with the API Control Plane, the AWS agent retrieves the assets from the AWS API Gateway and sends them to the API Control Plane when the *Publish Assets* use case is performed. For subsequent updates, the *Sync Assets* use case is performed. This class delegates all information retrieved from the *Amazon CloudTrail* service to *CloudTrailManagerImpl*. For example, *owner info*, data required for *Sync Assets* use case.|
| [CloudTrailManagerImpl](../common/modules/assets-handler/src/main/java/com/softwareag/controlplane/agentaws/assets/manager/impl/CloudTrailManagerImpl.java) | Contains code required for retrieving assets (APIs) related information from *Amazon CloudTrail* such as *owner of an API* and so on. This class handles retrieval of *CUD* modifications to the assets within a specified time period from the *Amazon CloudTrail* service. This information is used for *Sync Assets* use case.|

[HeartbeatManager](../common/modules/heartbeat-handler/src/main/java/com/softwareag/controlplane/agentaws/heartbeat/manager/impl)

The HeartbeatManager class contains the following file:

| Class Name | Description                                                       |
|-------------|-------------------------------------------------------------------|
| [HeartbeatManagerImpl](../common/modules/heartbeat-handler/src/main/java/com/softwareag/controlplane/agentaws/heartbeat/manager/impl/HeartbeatManagerImpl.java) | Contains code required for retrieving the heartbeats from the Amazon API Gateway.|

[MetricsManager](../common/modules/metrics-handler/src/main/java/com/softwareag/controlplane/agentaws/metrics/manager/impl)

The MetricsManager class contains the following file:

| Class Name | Description                                                       |
|-------------|-------------------------------------------------------------------|
| [MetricsManagerImpl](../common/modules/metrics-handler/src/main/java/com/softwareag/controlplane/agentaws/metrics/manager/impl/MetricsManagerImpl.java) | Contains code required for retrieving the metrics from the Amazon API Gateway.|
| |
