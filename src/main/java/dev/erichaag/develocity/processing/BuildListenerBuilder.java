package dev.erichaag.develocity.processing;

import dev.erichaag.develocity.api.BazelBuild;
import dev.erichaag.develocity.api.Build;
import dev.erichaag.develocity.api.BuildModel;
import dev.erichaag.develocity.api.GradleBuild;
import dev.erichaag.develocity.api.MavenBuild;
import dev.erichaag.develocity.api.SbtBuild;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static java.util.Set.copyOf;

/**
 * A builder class for constructing a {@link BuildListener} instance.
 *
 * <p>An instance of this builder can be obtained by calling
 * {@link BuildListener#builder()}.
 *
 * <p>This builder allows you to create a {@link BuildListener} by specifying
 * the desired callback methods and the required build models. This builder can
 * be convenient to quickly construct listeners without needing to write a
 * dedicated class that implements {@link BuildListener}.
 *
 * <p>This builder provides a fluent interface, allowing method calls to be
 * chained together to register multiple callbacks for different builds.
 *
 * @see BuildListener
 */
public final class BuildListenerBuilder {

    private final List<BuildListener> listeners = new ArrayList<>();
    private final Set<BuildModel> requiredBuildModels = new HashSet<>();

    BuildListenerBuilder() {
    }

    /**
     * Constructs a new {@link BuildListener} instance with the configured
     * callbacks and required build models.
     *
     * <p>The returned listener will invoke all registered listener methods
     * when builds are encountered.
     *
     * @return a new {@link BuildListener} instance
     */
    public BuildListener build() {
        return new BuildListener() {

            @Override
            public Set<BuildModel> getRequiredBuildModels() {
                return copyOf(requiredBuildModels);
            }

            @Override
            public void onBuild(Build build) {
                listeners.forEach(it -> it.onBuild(build));
            }

            @Override
            public void onGradleBuild(GradleBuild build) {
                listeners.forEach(it -> it.onGradleBuild(build));
            }

            @Override
            public void onMavenBuild(MavenBuild build) {
                listeners.forEach(it -> it.onMavenBuild(build));
            }

            @Override
            public void onBazelBuild(BazelBuild build) {
                listeners.forEach(it -> it.onBazelBuild(build));
            }

            @Override
            public void onSbtBuild(SbtBuild build) {
                listeners.forEach(it -> it.onSbtBuild(build));
            }

        };
    }

    /**
     * Adds the specified build models required by the constructed
     * {@link BuildListener}. This information is used by a
     * {@link BuildProcessor} to know which build models to request.
     *
     * <p>Calling this method more than once will add the supplied build models
     * to the set of required build models. Duplicate build models are ignored.
     *
     * @param buildModels the build models required by the constructed listener
     * @return this builder instance for fluent configuration
     */
    public BuildListenerBuilder requiredBuildModels(BuildModel... buildModels) {
        requiredBuildModels.addAll(Set.of(buildModels));
        return this;
    }

    /**
     * Registers a callback function to be invoked when any build is encountered.
     *
     * @param onBuild the callback function to execute
     * @return this builder instance for fluent configuration
     */
    public BuildListenerBuilder onBuild(Consumer<Build> onBuild) {
        listeners.add(new BuildListener() {
            @Override
            public void onBuild(Build build) {
                onBuild.accept(build);
            }
        });
        return this;
    }

    /**
     * Registers a callback function to be invoked when a Gradle build is
     * encountered.
     *
     * @param onGradleBuild the callback function to execute
     * @return this builder instance for fluent configuration
     */
    public BuildListenerBuilder onGradleBuild(Consumer<GradleBuild> onGradleBuild) {
        listeners.add(new BuildListener() {
            @Override
            public void onGradleBuild(GradleBuild build) {
                onGradleBuild.accept(build);
            }
        });
        return this;
    }

    /**
     * Registers a callback function to be invoked when a Maven build is
     * encountered.
     *
     * @param onMavenBuild the callback function to execute
     * @return this builder instance for fluent configuration
     */
    public BuildListenerBuilder onMavenBuild(Consumer<MavenBuild> onMavenBuild) {
        listeners.add(new BuildListener() {
            @Override
            public void onMavenBuild(MavenBuild build) {
                onMavenBuild.accept(build);
            }
        });
        return this;
    }

    /**
     * Registers a callback function to be invoked when a Bazel build is
     * encountered.
     *
     * @param onBazelBuild the callback function to execute
     * @return this builder instance for fluent configuration
     */
    public BuildListenerBuilder onBazelBuild(Consumer<BazelBuild> onBazelBuild) {
        listeners.add(new BuildListener() {
            @Override
            public void onBazelBuild(BazelBuild build) {
                onBazelBuild.accept(build);
            }
        });
        return this;
    }

    /**
     * Registers a callback function to be invoked when an sbt build is
     * encountered.
     *
     * @param onSbtBuild the callback function to execute
     * @return this builder instance for fluent configuration
     */
    public BuildListenerBuilder onSbtBuild(Consumer<SbtBuild> onSbtBuild) {
        listeners.add(new BuildListener() {
            @Override
            public void onSbtBuild(SbtBuild build) {
                onSbtBuild.accept(build);
            }
        });
        return this;
    }

}
