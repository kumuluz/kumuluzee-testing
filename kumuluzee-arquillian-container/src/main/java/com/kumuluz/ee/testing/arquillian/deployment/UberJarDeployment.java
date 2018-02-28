package com.kumuluz.ee.testing.arquillian.deployment;

import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class UberJarDeployment extends AbstractDeployment {

    private static final String JAR_SUFFIX = ".jar";

    private File archiveFile;

    public UberJarDeployment(Archive<?> archive) {
        super(archive);
    }

    @Override
    protected void exportArchive() throws DeploymentException {
        try {

            archiveFile = Files.createTempFile(tmpDir, null, JAR_SUFFIX).toFile();
            chmod777(archiveFile);

            if (shouldDelete) {
                archiveFile.deleteOnExit();
            }

            this.archive.as(ZipExporter.class).exportTo(archiveFile, true);

        } catch (IOException e) {
            throw new DeploymentException("Could not initialize deployment", e);
        }
    }

    @Override
    protected List<String> createArguments() {
        List<String> arguments = new ArrayList<>();
        arguments.add("-jar");
        arguments.add(archiveFile.getPath());
        return arguments;
    }
}
