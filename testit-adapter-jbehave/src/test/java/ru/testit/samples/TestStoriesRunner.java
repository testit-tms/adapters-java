package ru.testit.samples;

import org.jbehave.core.ConfigurableEmbedder;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.embedder.EmbedderControls;
import org.jbehave.core.embedder.NullEmbedderMonitor;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.reporters.NullStoryReporter;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.testit.listener.BaseJbehaveListener;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class TestStoriesRunner extends ConfigurableEmbedder {
    public Embedder embedder;
    @TempDir
    Path temp;

    @Override
    @Test
    public void run() {
        embedder = new Embedder();
        embedder.useEmbedderMonitor(new NullEmbedderMonitor());
        embedder.useEmbedderControls(new EmbedderControls()
                .doGenerateViewAfterStories(false)
                .doFailOnStoryTimeout(false)
                .doBatch(false)
                .doIgnoreFailureInStories(true)
                .doIgnoreFailureInView(true)
                .doVerboseFailures(false)
                .doVerboseFiltering(false)
        );
        embedder.useConfiguration(configuration());
        embedder.useCandidateSteps(stepsFactory().createCandidateSteps());

        File dir = new File("./src/test/resources/stories");
        List<String> stories = new ArrayList<>();

        for ( File file : dir.listFiles() ){
            if ( file.isFile() )
                stories.add("stories/" + file.getName());
        }

        embedder.runStoriesAsPaths(stories);
    }

    public Configuration configuration() {
        final BaseJbehaveListener listener = new BaseJbehaveListener();

        return new MostUsefulConfiguration()
                .useStoryLoader(new LoadFromClasspath(this.getClass()))
                .useStoryReporterBuilder(
                    new TestStoryReporterBuilder(temp.toFile())
                        .withReporters(listener))
                .useDefaultStoryReporter(new NullStoryReporter());
    }

    public InjectableStepsFactory stepsFactory() {
        return new InstanceStepsFactory(embedder.configuration(), new SampleSteps());
    }

    static class TestStoryReporterBuilder extends StoryReporterBuilder {

        private final File outputDirectory;

        TestStoryReporterBuilder(final File outputDirectory) {
            this.outputDirectory = outputDirectory;
        }

        @Override
        public File outputDirectory() {
            return outputDirectory;
        }
    }
}
