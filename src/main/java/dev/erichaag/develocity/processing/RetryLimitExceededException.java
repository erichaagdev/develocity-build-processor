package dev.erichaag.develocity.processing;

public class RetryLimitExceededException extends RuntimeException {

    RetryLimitExceededException(int retryLimit, Throwable cause) {
        super("Retry limit exceeded. Retried " + retryLimit + " times.", cause);
    }

}
