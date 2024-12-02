## Differences between Spring Boot Application and Functions (AWS lambda) Flavour

The following table lists the differences between the Spring Boot Application and Functions (AWS lambda) Flavour


| **Key Points**  | **Spring Boot Application** | **Functions (AWS Lambda)** |
|------------------|-----------------------------|----------------------------|
| **Deployment** | Deployed like a regular server. | Deployed as Function as a Service (FaaS) on AWS Lambda|
| **Maintenance and Availability** | **High maintenance**<br> User must manage deployment for full availability. | **Low maintenance**<br> For FaaS flavours, cloud hosting service providers such as *AWS* handles availability once deployed. |
| **Resource Consumption and Cost** | Server runs 24x7, even with sparse utilization. | AWS provider manages and runs the code only when invoked. Charges are incurred only based on number of invocations and runtime. <br> More **cost effective** compared to regular hosted solutions.|
| **Implementation** | A *single implementation* of spring boot application handles all three use cases of an agent: *assets synchronization*, *heartbeats synchronization*, and *metrics synchronization*. | Each Lambda function handles one use case of an agent. To achieve *assets synchronization*, *heartbeats synchronization*, and *metrics synchronization*, deploy three separate Lambda functions.  |
| **Schedule** | The frequency of the synchronization of heartbeats, assets, and metrics from the runtime to API Control Plane is maintained by the agent application code. Each activity gets triggered based on the *configured synchronization intervals*. | The schedule of the Lambda function is maintained in AWS component, **Amazon EventBridge**. The Lambda function gets triggered based on the intervals configured in EventBridge.<br> The schedule configured in EventBridge should match with with the synchronization intervals configured in the Lambda environment properties for smooth functioning. |
| **Suitability** | Ideal for on-prem deployment flavour. | Suitable for hosted solutions. <br>Best in terms of maintenance and cost compared to other hosted flavours. |
