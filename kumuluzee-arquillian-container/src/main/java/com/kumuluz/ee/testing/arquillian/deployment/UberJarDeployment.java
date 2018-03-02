/*
 *  Copyright (c) 2014-2017 Kumuluz and/or its affiliates
 *  and other contributors as indicated by the @author tags and
 *  the contributor list.
 *
 *  Licensed under the MIT License (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  https://opensource.org/licenses/MIT
 *
 *  The software is provided "AS IS", WITHOUT WARRANTY OF ANY KIND, express or
 *  implied, including but not limited to the warranties of merchantability,
 *  fitness for a particular purpose and noninfringement. in no event shall the
 *  authors or copyright holders be liable for any claim, damages or other
 *  liability, whether in an action of contract, tort or otherwise, arising from,
 *  out of or in connection with the software or the use or other dealings in the
 *  software. See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.kumuluz.ee.testing.arquillian.deployment;

import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Uber JAR KumuluzEE deployment.
 *
 * @author Urban Malc
 * @since 1.0.0
 */
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
