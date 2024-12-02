## Core Implementation Logic 
Let’s understand the logic of how the agent for Amazon API Gateway is implemented for different use cases.

1. **Registering Amazon API Gateway with API Control Plane**.

    1. The agent reads Amazon API Gateway and API Control Plane configurations from the Environment properties.
    2. The agent utilizes the configurations to register Amazon API Gateway with API Control Plane. <br><br>

2. **Retrieving Amazon API Gateway’s health status and sending it to API Control Plane**.

    1. The agent retrieves all REST APIs from the Amazon API Gateway.
    2. The agent verifies if at least one API is deployed to the specified stage in the given region (as per the properties). If an API is deployed, the agent sends          the runtime (stage) health status as *active* to the API Control Plane.<br><br>

3. **Publishing Amazon API Gateway’s assets to API Control Plane**. 

    1. The agent retrieves all REST APIs from the Amazon API Gateway.
    2. For each API, the agent verifies if the API is associated with the specified stage (from the properties).
    3. The agent publishes all APIs associated with the runtime (stage) to the API Control Plane.<br><br>

4. **Synchronizing assets between Amazon API Gateway and API Control Plane**.

    1. The agent retrieves all the events of the APIs associated with the stage from the **Amazon CloudTrail** service.
    2. The agent categorizes the events based on *CREATE*, *UPDATE*, and *DELETE* actions, and synchronizes the updates with the API Control Plane.<br><br>

5. **Retrieving metrics from Amazon API Gateway to API Control Plane**.

    For each API associated with the stage, the agent retrieves the following metrics from **Amazon CloudWatch** using the *AWS Java SDK*: *4XXError*, *5XXError*, *Count*, *IntegrationLatency*, and *Latency*. 

    1. The metrics are aggregated based on the synchronization interval set in the Environment properties.
    2. The metrics are retrieved using either *GetMetricData* or *GetMetricStatistic* methods from the *AWS Java SDK* based on the Environment properties. For details about the properties, see [How to Run the AWS Agent as 
       a Spring Boot Application in Docker?](../application)

