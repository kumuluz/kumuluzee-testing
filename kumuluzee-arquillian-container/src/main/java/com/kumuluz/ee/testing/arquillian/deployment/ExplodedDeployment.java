package com.kumuluz.ee.testing.arquillian.deployment;

import com.kumuluz.ee.testing.arquillian.assets.MainWrapper;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.exporter.ExplodedExporter;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ExplodedDeployment extends AbstractDeployment {

    public ExplodedDeployment(Archive<?> archive) {
        super(archive);
    }

    @Override
    protected void exportArchive() {

        this.archive.as(ExplodedExporter.class).exportExplodedInto(tmpDir.toFile());
    }

    @Override
    protected List<String> createArguments() {
        List<String> arguments = new ArrayList<>();

        Path targetDir = tmpDir;

        arguments.add("-cp");
        arguments.add(targetDir.resolve("classes") + File.pathSeparator +
                targetDir.resolve("dependency") + File.separator +  "*");
        arguments.add(MainWrapper.class.getName());

        return arguments;
    }
}
