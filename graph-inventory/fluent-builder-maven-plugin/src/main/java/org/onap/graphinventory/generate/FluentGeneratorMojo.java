package org.onap.graphinventory.generate;

import java.io.IOException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import com.fasterxml.jackson.core.JsonProcessingException;

@Mojo(name = "generate-builders", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class FluentGeneratorMojo extends AbstractMojo {

    @Parameter
    private String destination;
    @Parameter
    private String destinationClasspath;
    @Parameter
    private String builderName;
    @Parameter
    private String swaggerLocation;
    @Parameter
    private String singularBuilderClass;
    @Parameter
    private String pluralBuilderClass;
    @Parameter
    private String topLevelBuilderClass;
    @Parameter
    private String baseBuilderClass;
    @Parameter
    private String singularClass;
    @Parameter
    private String pluralClass;
    @Parameter
    private String nameClass;

    public void execute() throws MojoExecutionException, MojoFailureException {

        try {
            new FluentGenerator(getLog(), destination, destinationClasspath, swaggerLocation, builderName,
                    singularBuilderClass, pluralBuilderClass, topLevelBuilderClass, baseBuilderClass, singularClass,
                    pluralClass, nameClass).run();
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
