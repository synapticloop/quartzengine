# A simple annotation Quartz annotation library

Two annotations:

```java
@QuartzEngineJobRunNow
@QuartzEngineJob(cronExpression = "0/10 * * * * ?")
public void track() {
    runCount++;
    System.out.println("This job has run " + runCount + " times.");
}
```

## @QuartzEngineJob

Schedule a job with a cron expression

- `group = "optional group name"` 
- `cronExpression = "0/10 * * * * ?")`
- `parameters = {"Prod", "v1"}`

## @QuartzEngineJobRunNow

Will run the job now as well - useful when you want to run it now and 
scheduled for the future

no parameters

## Usage

```java
QuartzEngine engine = QuartzEngine.getInstance("synapticloop.quartzengine");
for (JobDetailRecord listScheduledJob : engine.listScheduledJobs()) {
    System.out.println(listScheduledJob);
}
// Check the console output here!
System.out.println("Quartz Engine Singleton is running.");
Thread.currentThread().join();
```

You can ignore the `Thread.currentThread().join();` if you are running it in 
your own project.