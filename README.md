# Develocity™ Build Processor

A library for fetching, processing, and analyzing Develocity build data with a fluent API, built-in caching, and retry mechanisms.

The library aims to simplify working with the `/api/builds/**` endpoints from the Develocity API by providing a high level API with built-in convenience methods covering common use cases and proper typed classes for each build tool.
When using this library, you'll focus more on the actual processing and analysis of build data and less on the underlying Develocity API.

## Getting started

### Gradle

Add the following to your `build.gradle(.kts)` file.

```kotlin
dependencies {
    implementation("dev.erichaag:develocity-build-processor:0.0.1")
}
```

### Example

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
To use the additional data from the requested build models, cast the returned `Build` into a `BazelBuild`, `GradleBuild`, `MavenBuild`, or `SbtBuild`.

```java
processor.withRequiredBuildModels(GRADLE_ATTRIBUTES, MAVEN_ATTRIBUTES);

processor.onBuild(build -> {
    switch(build) {
        case GradleBuild b -> b.getAttributes()
                .map(GradleAttributes::getRequestedTasks)
                .ifPresent(System.out::println);
        case MavenBuild b -> b.getAttributes()
                .map(MavenAttributes::getRequestedGoals)
                .ifPresent(System.out::println);
        default -> {}
    }
});

// When using a build tool specific callback, no casting is required
processor.onGradleBuild(gradleBuild -> {
    gradleBuild.getAttributes()
        .map(GradleAttributes::getGradleVersion)
        .ifPresent(System.out::println);
});
```

Given requesting the *attributes* build models is so common, it's possible to query for custom values, tags, and several other attributes shared between each build tool directly through the top-level `Build` object without casting.

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

todo

### Configuring retries and backoffs

todo

## License

Develocity Build Processor is released under the [`MIT License`](LICENSE) and is not an official Gradle, Inc. product.
