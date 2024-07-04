# Develocity Failure Insights

A command-line tool that uses Develocity™ Build Scan® data to report insights on CI & Local build failures, specifically focusing on the time taken to resolve these failures.

## Requirements

- Java 21 or later
- Develocity API access

## Configuration

The tool is configured using a `config.properties` file located at the root of the distribution.
The following properties can be set:

- `serverUrl`: The URL of the Develocity server
- `since`: The beginning of the period to process builds from

Example `config.properties` file:

```properties
serverUrl=https://develocity-samples.gradle.com
since=Jan 1 2024 00:00
```

## Authentication

To authenticate with the Develocity API, access keys are looked up using the same environment variables and files as used by the [Gradle plugin](https://docs.gradle.com/develocity/gradle-plugin/current/#manual_access_key_configuration) and [Maven extension](https://docs.gradle.com/develocity/maven-extension/current/#manual_access_key_configuration).
If no access key is found, the tool will attempt to authenticate anonymously.

## Usage

The tool is run using the `develocity-failure-insights` shell script for Linux and macOS or `develocity-failure-insights.bat` for Windows.
