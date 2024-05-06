
## Authentication

AWS Roles required:

- AmazonAPIGatewayAdministrator (for heartbeats and APIs publish)
- AWSCloudTrail_ReadOnlyAccess (for APIs sync)
- CloudWatch_ReadOnlyAccess (for Metrics sync)

AWS Agent supports various implementations of AwsCredentialsProvider interface by AWS SDK. For details, see  [AWS documentation](https://sdk.amazonaws.com/java/api/latest/software/amazon/awssdk/auth/credentials/AwsCredentialsProvider.html)

You can toggle between the various credential provider choices by using the environment variable **AWS_CREDENTIALS_PROVIDER**. 

Agent implementation supports the following credential providers:

1. **Environment Variables**

To choose this provider, you can set the env variable *AWS_CREDENTIALS_PROVIDER* with value *ENV_VARIABLE*. If AWS_CREDENTIALS_PROVIDER is not set, by default ENV_VARIABLE is chosen.

If you want to use this credential provider, create a IAM user in AWS Console with aforementioned roles and generate access key and secret key. You need to pass those access key and secret key through **AWS_ACCESS_KEY_ID**, **AWS_SECRET_ACCESS_KEY** environment variables.

2. **Java System Properties**
To choose this provider, you can set the env variable *AWS_CREDENTIALS_PROVIDER* with value *SYSTEM_PROPERTY*. 

If you want to use this credential provider, create a IAM user in AWS Console with aforementioned roles and generate access key and secret key. You need to pass those access key and secret key through **aws.accessKeyId** and **aws.secretKey** system properties. 

3. **Web Identity Token** credentials from system properties or environment variables
A credential provider that will read web identity token file path, aws role arn and aws session name from system properties or environment variables for using web identity token credentials with STS.
This credentials provider requires the 'sts' module to be on the classpath.
To choose this provider, you can set the env variable *AWS_CREDENTIALS_PROVIDER* with value **WEB_IDENTITY_TOKEN_FILE**. 

For more details see,[AWS documentation](https://sdk.amazonaws.com/java/api/latest/software/amazon/awssdk/auth/credentials/WebIdentityTokenFileCredentialsProvider.html)
If the agent implementation is running  in Kubernetes in the same AWS environment, you do not need to pass the secrets. With this option, you can provide the necessary permissions directly to the agent running in Kubernetes (STS). For more details, see https://docs.aws.amazon.com/eks/latest/userguide/iam-roles-for-service-accounts.html 

4. **Process credentials provider** 
A credentials provider that can load credentials from an external process. This is used to support the credential_process setting in the profile credentials file. See https://sdk.amazonaws.com/java/api/latest/software/amazon/awssdk/auth/credentials/ProcessCredentialsProvider.html for more information.

To choose this provider, you can set the env variable *AWS_CREDENTIALS_PROVIDER* with value **PROCESS**. 

5. **InstanceProfileCredentialsProvider**
Credentials provider implementation that loads credentials from the Amazon EC2 Instance Metadata Service.
https://sdk.amazonaws.com/java/api/latest/software/amazon/awssdk/auth/credentials/InstanceProfileCredentialsProvider.html 

To choose this provider, you can set the env variable *AWS_CREDENTIALS_PROVIDER* with value **INSTANCE_PROFILE**.

6. **ProfileCredentialsProvider**
Credentials provider based on AWS configuration profiles. This loads credentials from a ProfileFile, allowing you to share multiple sets of AWS security credentials between different tools like the AWS SDK for Java and the AWS CLI.
https://sdk.amazonaws.com/java/api/latest/software/amazon/awssdk/auth/credentials/ProfileCredentialsProvider.html 

To choose this provider, you can set the env variable *AWS_CREDENTIALS_PROVIDER* with value **PROFILE**.



