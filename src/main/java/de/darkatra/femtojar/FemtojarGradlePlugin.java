package de.darkatra.femtojar;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskContainer;

@SuppressWarnings("unused") // used via build.gradle.kts
public class FemtojarGradlePlugin implements Plugin<Project> {

    @Override
    public void apply(final Project project) {

        final FemtojarExtension extension = project.getExtensions().create(
            "femtojar",
            FemtojarExtension.class
        );

        final TaskContainer tasks = project.getTasks();
        tasks.register("reencodeJars", ReencodeJarsTask.class, task -> {
            task.setGroup("femtojar");
            task.setDescription("Re-encode JAR files with custom class loader and compression.");

            // Configure task with extension values
            task.getSkip().set(extension.getSkip());
            task.getCompressionMode().set(extension.getCompressionMode());
            task.getBundleResources().set(extension.getBundleResources());
        });
    }
}
