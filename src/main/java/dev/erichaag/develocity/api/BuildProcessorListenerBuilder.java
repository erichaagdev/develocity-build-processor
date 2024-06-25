package dev.erichaag.develocity.api;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static java.util.Set.copyOf;

public final class BuildProcessorListenerBuilder {

    private final List<BuildProcessorListener> listeners = new ArrayList<>();
    private final Set<BuildModel> requiredBuildModels = new HashSet<>();

    BuildProcessorListenerBuilder() {
    }

    public BuildProcessorListener build() {
        return new BuildProcessorListener() {

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

    public BuildProcessorListenerBuilder requiredBuildModels(BuildModel... buildModels) {
        requiredBuildModels.addAll(Set.of(buildModels));
        return this;
    }

    public BuildProcessorListenerBuilder onBuild(Consumer<Build> consumer) {
        listeners.add(new BuildProcessorListener() {
            @Override
            public void onBuild(Build build) {
                consumer.accept(build);
            }
        });
        return this;
    }

    public BuildProcessorListenerBuilder onGradleBuild(Consumer<GradleBuild> consumer) {
        listeners.add(new BuildProcessorListener() {
            @Override
            public void onGradleBuild(GradleBuild build) {
                consumer.accept(build);
            }
        });
        return this;
    }

    public BuildProcessorListenerBuilder onMavenBuild(Consumer<MavenBuild> consumer) {
        listeners.add(new BuildProcessorListener() {
            @Override
            public void onMavenBuild(MavenBuild build) {
                consumer.accept(build);
            }
        });
        return this;
    }

    public BuildProcessorListenerBuilder onBazelBuild(Consumer<BazelBuild> consumer) {
        listeners.add(new BuildProcessorListener() {
            @Override
            public void onBazelBuild(BazelBuild build) {
                consumer.accept(build);
            }
        });
        return this;
    }

    public BuildProcessorListenerBuilder onSbtBuild(Consumer<SbtBuild> consumer) {
        listeners.add(new BuildProcessorListener() {
            @Override
            public void onSbtBuild(SbtBuild build) {
                consumer.accept(build);
            }
        });
        return this;
    }

}
