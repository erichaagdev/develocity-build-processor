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

public final class BuildListenerBuilder {

    private final List<BuildListener> listeners = new ArrayList<>();
    private final Set<BuildModel> requiredBuildModels = new HashSet<>();

    BuildListenerBuilder() {
    }

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

    public BuildListenerBuilder requiredBuildModels(BuildModel... buildModels) {
        requiredBuildModels.addAll(Set.of(buildModels));
        return this;
    }

    public BuildListenerBuilder onBuild(Consumer<Build> onBuild) {
        listeners.add(new BuildListener() {
            @Override
            public void onBuild(Build build) {
                onBuild.accept(build);
            }
        });
        return this;
    }

    public BuildListenerBuilder onGradleBuild(Consumer<GradleBuild> onGradleBuild) {
        listeners.add(new BuildListener() {
            @Override
            public void onGradleBuild(GradleBuild build) {
                onGradleBuild.accept(build);
            }
        });
        return this;
    }

    public BuildListenerBuilder onMavenBuild(Consumer<MavenBuild> onMavenBuild) {
        listeners.add(new BuildListener() {
            @Override
            public void onMavenBuild(MavenBuild build) {
                onMavenBuild.accept(build);
            }
        });
        return this;
    }

    public BuildListenerBuilder onBazelBuild(Consumer<BazelBuild> onBazelBuild) {
        listeners.add(new BuildListener() {
            @Override
            public void onBazelBuild(BazelBuild build) {
                onBazelBuild.accept(build);
            }
        });
        return this;
    }

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
