# Develocity™ Build Processor

A library for fetching, processing, and analyzing Develocity build data with a fluent API, built-in caching, and retry mechanisms.

The library aims to simplify working with the `/api/builds/**` endpoints from the Develocity API by providing a high level API with built-in convenience methods covering common use cases and proper typed classes for each build tool.
When using this library, you'll focus more on the actual processing and analysis of build data and less on the underlying Develocity API.

## Getting started

### Requirements

- Java 21 or later

### Adding to your build

#### Gradle

Add the following to your `build.gradle(.kts)` file:

```kotlin
dependencies {
    implementation("dev.erichaag:develocity-build-processor:0.0.1")
}
```

#### Maven

Add the following to your `pom.xml` file:

```xml
<dependency>
    <groupId>dev.erichaag</groupId>
    <artifactId>develocity-build-processor</artifactId>
    <version>0.0.1</version>
</dependency>
```

### Example

The following is a basic example of what typical usage looks like: 

```java
final var numberOfBuilds = new AtomicInteger();
final var numberOfGradleBuilds = new AtomicInteger();

BuildProcessor.forServer("https://develocity.example.com")
        .onBuild(__ -> numberOfBuilds.incrementAndGet())
        .onGradleBuild(__ -> numberOfGradleBuilds.incrementAndGet())
        .process(Duration.ofDays(1));

System.out.println("Processed " + numberOfBuilds.get() + " total builds");
System.out.println(numberOfGradleBuilds.get() + " were Gradle builds");
```

## Usage

The main entry point is the `BuildProcessor` class.
You can create an instance using one of the static constructors.
Which one you use depends on your authentication requirements. 

```java
// Use this if an access key is required to call the Develocity API
// Will search the environment for an appropriate access key, failing if one can't be found
BuildProcessor.forServer("https://develocity.example.com");

// Use this if you use anonymous access to call the Develocity API
BuildProcessor.forServerWithAnonymousAccess("https://develocity.example.com");
```

### Processing build data

A `BuildProcessor` works much like a Java `Stream` in that it has both *intermediate operations* and *terminal operations*.

#### Intermediate operations

A `BuildProcessor` has several _intermediate operations_ used for processing build data.
There are several operators available to use depending on your use case.

```java
// Print the ID of any build
processor.onBuild(build -> System.out.println(build.getId()));

// Print the ID of each Gradle build
processor.onGradleBuild(gradleBuild -> System.out.println(gradleBuild.getId()));

// Print the ID of each Maven build
processor.onMavenBuild(mavenBuild -> System.out.println(mavenBuild.getId()));

// "Meta" operators to report on the processing itself
processor.onProcessingStarted(e -> System.out.println("Started at " + e.triggeredAt()));
processor.onProcessingFinished(e -> System.out.println("Finished at " + e.triggeredAt()));
```

#### Terminal operations

Just like a `Stream`, a `BuildProcessor` won't begin processing data until a _terminal operation_ is used.
For a `BuildProcessor`, there is only one terminal operation named "`process`", though it has several overloaded variations for convenience.

```java
// Process builds from the last 7 days
processor.process(Duration.ofDays(7));

// Process builds since Oct 5, 2024
processor.process(LocalDate.parse("2024-10-05"));

// Process just Gradle builds since Oct 5, 2024 at 10:15:30 AM
processor.process(LocalDateTime.parse("2024-10-05T10:15:30"), "buildTool:gradle");
```

#### Build models

By default, the Develocity API returns very limited data about each build.
To get more details, you need to declare the *build models* you require.
The more build models that are required, the longer the query will take.

This library works similarly.
To use the additional data from the requested build models, you must cast the returned `Build` into a `BazelBuild`, `GradleBuild`, `MavenBuild`, or `SbtBuild`, or use one of the build tool-specific call backs.

The following example prints either the requested tasks for Gradle or requested goals for Maven, then prints the Gradle version of each encountered build:

```java
// These build models will be available to us during processing
processor.withRequiredBuildModels(GRADLE_ATTRIBUTES, MAVEN_ATTRIBUTES);

// Print the requested tasks for Gradle builds or requested goals for Maven builds
processor.onBuild(build -> {
    switch(build) {
        case GradleBuild b -> print(b, GradleAttributes::getRequestedTasks);
        case MavenBuild b -> print(b, MavenAttributes::getRequestedGoals);
        default -> {} // Ignore any other build tool
    }
});

// When using a build tool specific callback, no casting is required
processor.onGradleBuild(gradleBuild -> { 
    print(gradleBuild.getAttributes(), GradleAttributes::getGradleVersion);
});
```

##### The `BuildListener` interface

Depending on your use case, you may consider creating an implementation of a `BuildListener` to process build data.
This is useful if you want your solution to be portable, encapsulated, or you are potentially tracking a lot of state.

A `BuildListener` contains call back methods for each build tool with all methods being optional to implement.

[//]: # (todo create BuildListener sample and link it here)

##### Working with a `Build`

Given requesting the *attributes* build model is so common, it's possible to query for custom values, tags, and several other attributes shared between each build tool directly through the top-level `Build` object without casting.
When accessed this way, Java-idiomatic types are used where appropriate to do so.
For example, durations are returned as `Duration` and times as `Instant`.

The following example gives all CI repositories connected to Develocity by filtering on the `CI` tag and accumulating the `Git repository` custom value to a set:

```java
processor.withRequiredBuildModels(GRADLE_ATTRIBUTES, MAVEN_ATTRIBUTES);

final var gitRepositories = new HashSet<String>();

processor.onBuild(build -> {
    if (build.getTags().contains("CI")) {
        build.getFirstValue("Git repository")
            .map(this::normalize)
            .ifPresent(gitRepositories::add);
    }
});

// Ensure returned build data only contains Gradle or Maven builds
processor.process(LocalDate.parse("2024-10-05"), "buildTool:gradle or buildTool:maven");

System.out.println("Detected " + gitRepositories.size() + " repositories using Develocity");
gitRepositories.forEach(System.out::println);
```

### Caching build data for faster queries

Interacting with the Develocity API can be slow, especially when requesting many build models.
Caching the build data can significantly speed up subsequent queries that use the same build data.

By default, no caching is done on requests.
To enable caching, configure the `BuildProcessor` with an implementation of `ProcessorCache`.

There are three such implementations provided out-of-the-box by this library:

- A `FileSystemCache` caches build data on the file system, `~/.develocity-build-processor` by default.
- An `InMemoryCache` caches build data in memory, useful if you are invoking multiple `BuildProcessor` in the same program
- A `CompositeCache` composes two or more caches, such that if no build data is found in the first cache, the next cache will be checked, and so on.

The following example demonstrates using a `FileSystemCache`:

```java
final var start = Instant.now();
final var count = new AtomicInteger(0);

BuildProcessor.forServer("https://ge.solutions-team.gradle.com")
        .withRequiredBuildModels(GRADLE_BUILD_CACHE_PERFORMANCE, MAVEN_BUILD_CACHE_PERFORMANCE)
        .withProcessorCache(FileSystemCache.withDefaultStrategy())
        .onBuild(__ -> count.incrementAndGet())
        .process(Duration.ofDays(1));

final var end = Instant.now();
final var runtime = Duration.between(start, end).getSeconds();

System.out.println("Processed " + count.get() + " builds in " + runtime + " seconds.");
```

Here is one run of this program from my machine:

```shell
$ ./gradlew run --quiet --console=plain 
Processed 366 builds in 34 seconds.
$ ./gradlew run --quiet --console=plain
Processed 366 builds in 1 seconds.
```

> [!NOTE]
> Build data is only cached when at least one build model is requested for a given build.
> This is because the `/api/builds` endpoint is always called in order to know which builds to process.
> Therefore, there is no benefit to caching when there are no build models to cache.

### Configuring retries and back offs

todo

## License

Develocity Build Processor is released under the [`MIT License`](LICENSE) and is not an official Gradle, Inc. product.
