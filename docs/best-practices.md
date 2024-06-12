
## Best practices

**Retrieving metrics from Amazon CloudWatch**:

There are two primary methods for retrieving metrics from Amazon CloudWatch: **GetMetricStatistics** and **GetMetricData**. Each method has its own pricing structure, limitations, and recommended use cases.

| Method | Pricing | Limitations | Recommendations |
|--------------------|-------------------|-------------------|-------------------|
| **GetMetricStatistics** |	Under the free tier, the first *1 million* requests are free. Beyond that, there is a charge of *$0.01* per *1000*requests.<br><br>Each sync interval requires *five* requests. For example, with a sync interval of *300* seconds, you would need *288* requests per day. | Maximum *1,440* data points can be returned from a single call. If more data points are requested, Amazon CloudWatch returns an error.| Use this method during the following scenarios:<ul><li>For initial agent connection</li><li>When the total data points while re-registering after an agent downtime is less than 1,440.</li></ul><br>Data points computation: [(Current time - agent downtime) / (Metrics sync time)] |
| **GetMetricData** | GetMetricData does not fall under the free tier. It incurs a charge of *$0.01* for every *1000* metric requests. Similar to GetMetricStatistics, each sync interval requires *five* metric requests. | Can retrieve up to *100,800* data points in a single operation.| Use when the total data points exceed *1,440* while re-registering after agent downtime. <br>You can also re-register the agent again with GetMetricStatistics, if the free tier is still available (since the datapoints must have decreased).|

**APIs in Amazon API Gateway**

1. Deleting a REST API in Amazon API Gateway does not delete the API from the runtime (stage) in API Control Plane. However, deleting a stage from a REST API in Amazon API Gateway deletes that API from the runtime (stage) in API Control Plane. 

2. The API name must be unique. If there are two APIs with the same name, API Control Plane aggregates the metrics of both the APIs and generates visualizations and data accordingly.

