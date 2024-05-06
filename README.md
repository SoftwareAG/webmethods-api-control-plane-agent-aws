# webMethods API Control Plane Agent for Amazon API Gateway

This repository holds a sample stand-alone Agent implementation Gradle project for Amazon API Gateway, utilizing the Agent SDK. Key functionalities include:

1. Registering AWS API Gateway with API Control Plane.
2. Retrieving AWS API Gateway’s health status and sending it to API Control Plane.
3. Publishing AWS API Gateway’s assets to API Control Plane.
4. Synchronizing assets between AWS API Gateway and API Control Plane.
5. Retrieving metrics from AWS API Gateway to API Control Plane.
  

## Implementation Overview

The implementation utilizes the Manual approach, leveraging the AWS SDK for connection management and authentication. For details about the approaches, see [Agent SDK](https://docs.webmethods.io/apicontrolplane/agent_sdk/chapter2wco/#gsc.tab=0) documentation.

Additionally, **AWS CloudWatch** and **CloudTrail** services are leveraged to monitor API metrics and API activity respectively.

**Note:** The agent implementation is compatible with API Control Plane version, 11.0.3. The current implementation supports only the REST APIs of AWS API Gateway.

API Control Plane Agent for Amazon API Gateway offers two distinct deployment modes:

- Spring Boot application
- AWS Lambda


## How is this repository structured?

To know how the repository is structured, see [Repository structure](docs/repo-structure.md).


## Co-relation between Amazon API Gateway and API Control Plane terminologies

To understand the co-relation between Amazon API Gateway and API Control Plane terminologies, see [Co-relation](docs/corelation.md).


## How to build the Gradle project?

To build the Gradle project, see [How to build?](devops/how-to-build.md)

The Jars are created at the following locations for both the deployment modes:

**Spring boot application:** application-0.0.1-SNAPSHOT is created at application / build / libs <br><br>
**AWS Lambda:** 
- lambda-layer.zip is created at functions / build / libs, which includes all dependencies to run lambda function.
- send-asset.jar is created at Functions / send–asset  / build / libs
- send-heartbeat.jar is created at Functions / send-heartbeat / build / libs
- send-metrics.jar is created at Functions / send-metrics / build / libs


## How to deploy a Spring boot application in Docker?

To deploy a Spring boot application in Docker, see [How to deploy?](application/how-to-deploy.md)


## Best Practices

[Best Practices](docs/best-practices.md)


## References
[Authentication](docs/authentication.md) section.

















