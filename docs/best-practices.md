
## Best practices

**Retrieving Metrics from CloudWatch**:

There are two primary methods for retrieving metrics from CloudWatch: GetMetricStatistics and GetMetricData. Each method has its own pricing structure, limitations, and recommended use cases.

| Method | Pricing | Limitations | Recommendations |
|--------------------|-------------------|
| GetMetricStatistics |	Under the free tier, the first 1 million requests are free. Beyond that, there's a charge of $0.01 per 1000 requests.<br>Each sync interval requires 5 requests. For example, with a sync interval of 300 seconds, you'd need 288 requests per day. | Maximum 1,440 data points can be returned from a single call. If more data points are requested, CloudWatch returns an error.| Use for initial Agent connection and when the total data points while re-registering after Agent downtime is less than 1,440.<br> Data points computation: (Current time - Agent downtime) / Metrics sync time) |
| GetMetricData | GetMetricData does not fall under the free tier. It incurs a charge of $0.01 for every 1000 metric requests. Similar to GetMetricStatistics, each sync interval requires 5 metric requests. | Can retrieve up to 100,800 data points in a single operation.| Use when the total data points exceed 1,440 while re-registering after Agent downtime. You can also re-register the agent again immediately after this with GetMetricStatistics, if the free tier is still available (since the datapoints must have decreased).|


