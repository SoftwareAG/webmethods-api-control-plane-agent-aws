
## Authentication

**Note**: ``` This section is applicable only when you deploy the AWS agent as a Spring Boot application. ```

The agent implementation for Amazon API Gateway supports various implementations of **AwsCredentialsProvider** interface provided by **AWS SDK**. For details, see [AWS documentation](https://sdk.amazonaws.com/java/api/latest/software/amazon/awssdk/auth/credentials/AwsCredentialsProvider.html).

Ensure that you have created an IAM user in AWS with the following roles assigned:

- **AmazonAPIGatewayAdministrator** (for publishing heartbeats and APIs from Amazon API Gateway to API Control Plane)
- **AWSCloudTrail_ReadOnlyAccess** (for synchronizing the APIs from Amazon API Gateway to API Control Plane)
- **CloudWatchReadOnlyAccess** (for synchronizing the metrics from Amazon API Gateway to API Control Plane)

You can toggle between the various credential provider choices by using the environment variable, **AWS_CREDENTIALS_PROVIDER**. 

The agent implementation supports the following credential providers:

1. **Environment Variables**

    To choose *Environment Variables* as the credential provider, set the env variable, **AWS_CREDENTIALS_PROVIDER** with value, **ENV_VARIABLE**. If *AWS_CREDENTIALS_PROVIDER* is not set, the *ENV_VARIABLE* is used by 
    default.

    To use this credential provider, create an IAM user in AWS Console with aforementioned roles and generate *access key* and *secret key*. You must specify the generated access key and secret key for 
   **AWS_ACCESS_KEY_ID**, **AWS_SECRET_ACCESS_KEY** environment variables respectively.

2. **Java System Properties**

    To choose *Java System Properties* as the credential provider, set the env variable, *AWS_CREDENTIALS_PROVIDER* with value, **SYSTEM_PROPERTY**. 

    To use this credential provider, create a IAM user in AWS Console with aforementioned roles and generate *access key* and *secret key*. You must specify the generated access key and secret key for **aws.accessKeyId** 
    and **aws.secretKey** system properties respectively. 


3. **Web Identity Token** 

    Web Identity Token credentials provider reads *web identity token file path*, *aws role arn*, and *aws session name* from the system properties or environment variables for using web identity token credentials with 
    STS.

    To use this credential provider, the *sts* module must be on the *classpath*. To choose Web Identity Token as a credential provider, set the env variable, **AWS_CREDENTIALS_PROVIDER** with value, 
    **WEB_IDENTITY_TOKEN_FILE**. 

    For details, see [AWS documentation](https://sdk.amazonaws.com/java/api/latest/software/amazon/awssdk/auth/credentials/WebIdentityTokenFileCredentialsProvider.html).

    If the agent implementation is running  in Kubernetes in the same AWS environment, you need not mention the secrets in the properties. Instead, you can provide the necessary permissions directly to the Agent running 
    in Kubernetes (STS). For details, see [AWS documentation](https://docs.aws.amazon.com/eks/latest/userguide/iam-roles-for-service-accounts.html).

4. **Process credentials provider** 

    Process credentials provider loads credentials from an external process. If you have a method of sourcing credentials that is not built into the AWS CLI, you can integrate it by using *credential_process* in the 
    config file.  

    To choose this provider, set the env variable, **AWS_CREDENTIALS_PROVIDER** with value, **PROCESS**. For details, see [AWS documentation](https://sdk.amazonaws.com/java/api/latest/software/amazon/awssdk/auth/credentials/ProcessCredentialsProvider.html).
     
5. **InstanceProfileCredentialsProvider**

    InstanceProfileCredentialsProvider loads credentials from the Amazon EC2 Instance Metadata Service. For details, see [AWS documentation](https://sdk.amazonaws.com/java/api/latest/software/amazon/awssdk/auth/credentials/InstanceProfileCredentialsProvider.html).

    To choose this provider, set the env variable, **AWS_CREDENTIALS_PROVIDER** with value, **INSTANCE_PROFILE**.

6. **ProfileCredentialsProvider**

    ProfileCredentialsProvider is based on AWS configuration profiles. This provider loads credentials from a *ProfileFile*, allowing you to share multiple sets of AWS security credentials between different tools like 
    the AWS SDK for Java and the AWS CLI. For details, see [AWS documentation](https://sdk.amazonaws.com/java/api/latest/software/amazon/awssdk/auth/credentials/ProfileCredentialsProvider.html).

    To choose this provider, set the env variable, **AWS_CREDENTIALS_PROVIDER** with value, **PROFILE**.




