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

import com.kumuluz.ee.testing.arquillian.KumuluzEEContainerConfig;
import com.kumuluz.ee.testing.arquillian.utils.OutputProcessor;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.arquillian.container.spi.client.protocol.metadata.HTTPContext;
import org.jboss.shrinkwrap.api.Archive;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Base for a runnable deployment.
 *
 * @author Urban Malc
 * @since 1.0.0
 */
public abstract class AbstractDeployment {

    private static final Logger LOG = Logger.getLogger(AbstractDeployment.class.getName());

    private KumuluzEEContainerConfig containerConfig;

    protected boolean shouldDelete;

    protected static final String TMP_DIR = "KumuluzEEArquillian";

    protected Path tmpDir;
    protected Archive<?> archive;

    private Process process;

    public AbstractDeployment(Archive<?> archive) {
        this.archive = archive;
        this.containerConfig = KumuluzEEContainerConfig.getInstance();
        this.shouldDelete = this.containerConfig.shouldDeleteTemporaryFiles();
    }

    public void init() throws DeploymentException {
        try {
            Path tmpDirParent = Paths.get(System.getProperty("java.io.tmpdir"), TMP_DIR);
            Files.createDirectories(tmpDirParent);

            chmod777(tmpDirParent.toFile());

            tmpDir = Files.createTempDirectory(tmpDirParent, null);
            chmod777(tmpDir.toFile());

            if (shouldDelete) {
                tmpDir.toFile().deleteOnExit();
            }
        } catch (IOException e) {
            throw new DeploymentException("Could not create temporary folder", e);
        }

        exportArchive();
    }

    protected abstract void exportArchive() throws DeploymentException;

    protected abstract List<String> createArguments();

    public HTTPContext start() throws DeploymentException {

        List<String> arguments = new ArrayList<>();

        Path javaPath;

        if (!this.containerConfig.getJavaPath().equals("")) {
            // java binary is specified in container configuration
            javaPath = Paths.get(this.containerConfig.getJavaPath());

            if (!javaPath.toFile().exists()) {
                throw new DeploymentException("Specified java binary " + this.containerConfig.getJavaPath() +
                        " could not be found.");
            }
        } else {
            // automatically discover java binary
            javaPath = javaPath();
        }

        arguments.add(javaPath.toString());
        if (!this.containerConfig.getJavaArguments().trim().isEmpty()) {
            arguments.addAll(Arrays.asList(this.containerConfig.getJavaArguments().trim().split(" ")));
        }
        arguments.addAll(createArguments());

        try {
            LOG.fine("Starting process. Working directory: " + tmpDir.toString() + ". Command: " +
                    String.join(" ", arguments));
            process = new ProcessBuilder().directory(tmpDir.toFile()).command(arguments).start();

            CountDownLatch serverReady = new CountDownLatch(1);

            OutputProcessor stdoutProcessor = new OutputProcessor(process.getInputStream(), serverReady);
            OutputProcessor stderrProcessor = new OutputProcessor(process.getErrorStream(), serverReady);
            Thread outThread = new Thread(stdoutProcessor);
            Thread errThread = new Thread(stderrProcessor);

            outThread.start();
            errThread.start();

            boolean isReady;
            try {
                isReady = serverReady.await(containerConfig.getContainerStartTimeoutMs(), TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                isReady = false;
            }

            if (!isReady) {
                throw new DeploymentException("Deployment failed to start in time");
            }

            if (stdoutProcessor.getProcessingError() != null) {
                throw new DeploymentException("Error while processing server stdout", stdoutProcessor.getProcessingError());
            }
            if (stderrProcessor.getProcessingError() != null) {
                throw new DeploymentException("Error while processing server stderr", stderrProcessor.getProcessingError());
            }
            if (stdoutProcessor.getDeploymentError() != null) {
                throw new DeploymentException("Exception thrown during deployment", stdoutProcessor.getDeploymentError());
            }

            HTTPContext context = stdoutProcessor.getHttpContext();

            if (context == null) {
                throw new DeploymentException("Could not retrieve HTTP context from server");
            }

            LOG.fine("Deployment started, context: " + context.toString());

            return context;
        } catch (IOException e) {
            throw new DeploymentException("Could not start deployment", e);
        }
    }

    public void stop() {
        if (process != null) {
            process.destroy();
        }

        // clean up tmp files
        if (shouldDelete) {
            if (tmpDir.toFile().exists()) {
                deleteDirectory(tmpDir.toFile());
            }
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    protected void chmod777(File file) {
        file.setReadable(true, false);
        file.setWritable(true, false);
        file.setExecutable(true, false); // Unix: allow content for dir, redundant for file
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        directoryToBeDeleted.delete();
    }

    private static Path javaPath() throws DeploymentException {
        String javaHome = System.getProperty("java.home");
        if (javaHome == null) {
            throw new DeploymentException("Unable to locate java binary");
        }

        Path binDir = FileSystems.getDefault().getPath(javaHome, "bin");

        Path java = binDir.resolve("java.exe");
        if (java.toFile().exists()) {
            return java;
        }

        java = binDir.resolve("java");
        if (java.toFile().exists()) {
            return java;
        }

        throw new DeploymentException("Unable to locate java binary");
    }
}
