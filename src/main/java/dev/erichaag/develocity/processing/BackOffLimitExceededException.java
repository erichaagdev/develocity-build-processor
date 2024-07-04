package dev.erichaag.develocity.processing;

public class BackOffLimitExceededException extends RuntimeException {

    BackOffLimitExceededException(int backOffLimit) {
        super("Back off limit of " + backOffLimit + "exceeded. Try reducing the number of builds per request or increasing the back off limit.");
    }

}
