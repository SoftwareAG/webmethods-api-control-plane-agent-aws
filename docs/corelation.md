
## Co-relation between Amazon API Gateway and API Control Plane terminologies

| Amazon API Gateway | API Control Plane | Description |
|--------------------|-------------------|-------------|
| Stage |Runtime | A stage in Amazon API Gateway is considered as a runtime in API Control Plane. |
| Deployment | API Versions | A deployment in Amazon API Gateway is a snapshot that tracks an API's version. An API in Amazon API Gateway can have multiple deployments, but only one can be active per stage at a time. <br><br> In the API Control Plane, each deployment is called an API version. For example, if an API has two deployments published to two different stages, it is treated as two different versions of the same API in API Control Plane.<br>When there are multiple deployments of an API to a stage, only the active deployment is synchronized with API Control Plane.   |

A *Stage* in Amazon API Gateway created within a *region* and belonging to an *AWS account* is treated as an *individual runtime* in API Control Plane. As per the current implementation of the AWS agent, it requires one agent to be deployed for every stage-runtime mapping.
