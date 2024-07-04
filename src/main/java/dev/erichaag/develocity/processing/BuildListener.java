package dev.erichaag.develocity.processing;

import dev.erichaag.develocity.api.BazelBuild;
import dev.erichaag.develocity.api.Build;
import dev.erichaag.develocity.api.BuildModel;
import dev.erichaag.develocity.api.GradleBuild;
import dev.erichaag.develocity.api.MavenBuild;
import dev.erichaag.develocity.api.SbtBuild;

import java.util.Set;

import static java.util.Collections.emptySet;

/**
 * A listener interface for receiving notifications about builds encountered
 * during processing.
 *
 * <p>Implement this interface to receive callbacks when builds are encountered
 * by a {@link BuildProcessor}. A {@link BuildListenerBuilder} provides a
 * convenient way to create custom listeners without needing to write a
 * dedicated class that implements this interface.
 *
 * <p>Note that it can sometimes be useful for an implementation to also
 * implement {@link ProcessListener} if it needs to be notified about a specific
 * processing stage. For example, when all processing has finished so that the
 * listener can perform a final action.
 *
 * <p>By default, all methods have empty implementations. Implement only the
 * methods relevant to your use case.
 *
 * @see BuildListenerBuilder
 * @see ProcessListener
 */
public interface BuildListener {

    /**
     * Creates a new {@link BuildListenerBuilder} to construct a
     * {@link BuildListener} instance.
     *
     * <p>This can be convenient to quickly construct listeners without needing
     * to write a dedicated class that implements this interface.
     *
     * @return a new builder instance
     */
    static BuildListenerBuilder builder() {
        return new BuildListenerBuilder();
    }

    /**
     * Returns the set of {@link BuildModel}s required by this listener.
     * This information is used by a {@link BuildProcessor} to know which build
     * models to request.
     *
     * <p>By default, this method returns an empty set, indicating that no
     * additional build models are required by this listener.
     *
     * @return the set of required build models
     */
    default Set<BuildModel> getRequiredBuildModels() {
        return emptySet();
    }

    /**
     * Callback method invoked when any build is encountered.
     *
     * @param build the build that was encountered
     */
    default void onBuild(Build build) {
    }

    /**
     * Callback method invoked when a Gradle build is encountered.
     *
     * @param build the Gradle build that was encountered
     */
    default void onGradleBuild(GradleBuild build) {
    }

    /**
     * Callback method invoked when a Maven build is encountered.
     *
     * @param build the Maven build that was encountered
     */
    default void onMavenBuild(MavenBuild build) {
    }

    /**
     * Callback method invoked when a Bazel build is encountered.
     *
     * @param build the Bazel build that was encountered
     */
    default void onBazelBuild(BazelBuild build) {
    }

    /**
     * Callback method invoked when an sbt build is encountered.
     *
     * @param build the Sbt build that was encountered
     */
    default void onSbtBuild(SbtBuild build) {
    }

}
