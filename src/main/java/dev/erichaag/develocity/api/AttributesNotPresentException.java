package dev.erichaag.develocity.api;

import java.util.function.Supplier;

public class AttributesNotPresentException extends RuntimeException {

    AttributesNotPresentException(String methodName) {
        super("'" + methodName + "' may only be called when attributes are present.");
    }

    static Supplier<AttributesNotPresentException> attributesNotPresent(String methodName) {
        return () -> new AttributesNotPresentException(methodName);
    }

}
