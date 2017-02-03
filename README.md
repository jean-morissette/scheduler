# Scheduler
[![javadoc](http://www.javadoc.io/badge/au.id.ajlane.scheduler/scheduler.svg)](http://www.javadoc.io/doc/au.id.ajlane.scheduler/scheduler)
[![build-status](https://travis-ci.org/ajlane/scheduler.svg?branch=master)](https://travis-ci.org/ajlane/scheduler) [![codecov](https://codecov.io/gh/ajlane/scheduler/branch/master/graph/badge.svg)](https://codecov.io/gh/ajlane/scheduler)

A clock-based ExecutorService using Java 8 Time. For when Quartz is just too heavy.

Scheduler is provided under the [Apache License Version 2.0](https://www.apache.org/licenses/LICENSE-2.0).

## Examples

### Using java.time

```java
CalendarExecutorService executor = CalendarExecutorService.threadPool();

executor.schedule(
    () -> System.out.println("Remember me."),
    // Midnight (local time), 23 September 2017
    ZonedDateTime.of(2017, 09, 23, 13, 0, 0, ZoneId.systemDefault()).toInstant()
);
```

### Using Cron

```java
CalendarExecutorService executor = CalendarExecutorService.threadPool();

CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));

executor.schedule(
    () -> System.out.println("Ding dong."),
    // Every hour, on the hour, Monday to Friday, 9am to 5pm
    parser.parse("0 0 9-17 * * * 1-5")
);
```

## Getting Started

Scheduler is available in Maven Central, using the following coordinates:
```xml
<project>
  ...
  <dependencies>
    <dependency>
      <groupId>au.id.ajlane.scheduler</groupId>
      <artifactId>scheduler</artifactId>
      <version>0.0.3</version>
    </dependency>
  </dependencies>
  ...
</project>
```
