package dev.erichaag.develocity.api;

public class MethodNotSupportedException extends RuntimeException {

    MethodNotSupportedException(String methodName, String buildTool) {
        super("'" + methodName + "' is not supported for " + buildTool + " builds.");
    }

    static MethodNotSupportedException methodNotSupportedForBazel(String methodName) {
        return new MethodNotSupportedException(methodName, "Bazel");
    }

    static MethodNotSupportedException methodNotSupportedForSbt(String methodName) {
        return new MethodNotSupportedException(methodName, "sbt");
    }

}
