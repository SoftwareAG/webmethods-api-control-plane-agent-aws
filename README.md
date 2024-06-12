# webMethods API Control Plane Agent for Amazon API Gateway

![Version 1.0.0](https://img.shields.io/badge/Version-1.0.0-blue)
![Agent SDK Version 1.0.0](https://img.shields.io/badge/Agent_SDK-1.0.0-green)
![API Control Plane Version 11.0.3](https://img.shields.io/badge/API_Control_Plane-11.0.3-purple)
![Amazon API Gateway](https://img.shields.io/badge/Amazon-API_Gateway-blue)
<br>
![Java 17](https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=java&logoColor=white)
![Gradle 7.4.2](https://img.shields.io/badge/Gradle-7.4.2-DD0031?style=for-the-badge&logo=java&logoColor=white)
![Spring](https://img.shields.io/badge/Spring-6DB33F?style=for-the-badge&logo=spring&logoColor=white)

This repository holds an agent implementation Java project for connecting Amazon API Gateway with API Control Plane, utilizing the **Agent SDK**. The key functionalities include:

1. Registering Amazon API Gateway with API Control Plane.
2. Retrieving Amazon API Gateway’s health status and sending it to API Control Plane.
3. Publishing Amazon API Gateway’s assets to API Control Plane.
4. Synchronizing assets between Amazon API Gateway and API Control Plane.
5. Retrieving metrics from Amazon API Gateway to API Control Plane.
  
This project is developed using **Java 17** and **Gradle 7.4.2**<br>
If you plan to upgrade *Gradle*, ensure that you also upgrade the supported *Java version* accordingly. For details about the compatibility between *Java* and *Gradle* versions, see [Compatibility Matrix](https://docs.gradle.org/current/userguide/compatibility.html).

## Table of Contents
- [Implementation Overview](#implementation-overview)
  - [Core Implementation Logic](docs/core-logic.md)
  - [Customize the Core Implementation Logic](common/)
- [How is this Repository Structured?](docs/repo-structure.md)
- [Co-relation Between Amazon API Gateway and API Control Plane Terminologies](docs/corelation.md)
- [How to Build the Gradle Project?](devops/)
- [How to Deploy and Run the AWS Agent as a Spring Boot application in Docker?](application/)
  - [Authentication](docs/authentication.md)
  - [How to Create the Runtime Type in API Control Plane?](docs/runtime_service_mgmt_api.md)
- [How to Deploy and Run the AWS Agent in AWS Lambda?](functions/)
- [Best Practices](docs/best-practices.md)
  

## Implementation Overview

The implementation utilizes the **Manual** approach of Agent SDK and provides options for deploying the AWS agent as a stand-alone application. For details about the approaches and deployment modes, see [Agent SDK](https://docs.webmethods.io/apicontrolplane/agent_sdk/chapter2wco/#gsc.tab=0) documentation.
The implementation leverages the following Amazon services:
- **AWS SDK** for connection management and authentication. 
- **Amazon CloudWatch** service for retrieving API metrics.
- **Amazon CloudTrail** service for retrieving  API activity.

**Note:** ``` The agent implementation is compatible with API Control Plane version, 11.0.3 and currently supports only the REST APIs of Amazon API Gateway. ```

The AWS agent can be deployed in the following ways:

- **Spring Boot application**
- **AWS Lambda**<br>

For a detailed understanding of how the agent for Amazon API Gateway is implemented, see [Core Implementation Logic](docs/core-logic.md).

The AWS agent developer can utilize this repository in the following ways:
 
- Use the repository directly to build and deploy the AWS agent.
- Fork the repository, customize the code as required, and then build and deploy the AWS agent.

## How is this Repository Structured?

This section outlines the Git repository's structure, highlighting the purpose of each directory. For details, see [Repository structure](docs/repo-structure.md).


## Co-relation Between Amazon API Gateway and API Control Plane Terminologies

This section details the relationship and equivalence between the terminologies used in *Amazon API Gateway* and the *API Control Plane*.  For details, see [Co-relation](docs/corelation.md).


## How to Build the Gradle Project?

For details, see [How to build?](devops/)

Once the Gradle project is built, the Jars are created at the following locations for both the deployment modes:

**Spring Boot application**: **application-<*version*>-SNAPSHOT** is created at *application / build / libs* <br>

**AWS Lambda:** 
- **lambda-layer.zip** is created at *functions / build / libs*, which includes all dependencies to run lambda function.
- **send-asset.jar** is created at *functions / send–asset  / build / libs*
- **send-heartbeat.jar** is created at *functions / send-heartbeat / build / libs*
- **send-metrics.jar** is created at *functions / send-metrics / build / libs*


## How to Deploy and Run the AWS Agent as a Spring Boot Application in Docker?

This section details how to deploy the AWS agent as a stand-alone application. For details, see [How to deploy?](application/)


## How to Deploy and Run the AWS Agent in AWS Lambda?

AWS Lambda is a function as a service provided by AWS that lets you deploy and run your application without provisioning or managing servers. You are responsible only for the application code that you provide Lambda and the conﬁguration of how Lambda runs that code on your behalf. For details about AWS Lambda, see [AWS Lambda documentation](https://docs.aws.amazon.com/lambda/latest/dg/welcome.html).

Deploying an AWS agent in AWS Lambda lets you run the agent virtually without the need for administration of the underlying infrastructure. For details about the benefits of AWS Lambda, see [AWS Lambda documentation](https://docs.aws.amazon.com/whitepapers/latest/security-overview-aws-lambda/benefits-of-lambda.html).

For details about how to deploy and run the AWS agent in AWS Lambda, see [How to deploy?](functions/)


## Best Practices

This section outlines the essential best practices for using this implementation and deploying the AWS agent. For details, see [Best Practices](docs/best-practices.md).


## References
- To learn about the authentication for a Spring boot application, see [Authentication](docs/authentication.md) section.<br>
- To learn how to create the *runtime type* in the API Control Plane, see [Runtime Type Management Service API](docs/runtime_service_mgmt_api.md).

