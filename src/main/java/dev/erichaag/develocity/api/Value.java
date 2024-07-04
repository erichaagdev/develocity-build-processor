package dev.erichaag.develocity.api;

public record Value(String name, String value) {

    Value(BuildAttributesValue buildAttributesValue) {
        this(buildAttributesValue.getName(), buildAttributesValue.getValue());
    }

}
