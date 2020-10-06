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
package com.kumuluz.ee.testing.arquillian.utils;

import com.kumuluz.ee.testing.arquillian.KumuluzEEContainerConfig;
import com.kumuluz.ee.testing.arquillian.spi.MavenDependencyAppender;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.resolver.api.maven.ConfigurableMavenResolverSystem;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.repository.MavenRemoteRepositories;
import org.jboss.shrinkwrap.resolver.api.maven.repository.MavenRemoteRepository;
import org.jboss.shrinkwrap.resolver.api.maven.repository.MavenUpdatePolicy;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Includes methods used for collecting libraries, required for each deployment.
 *
 * @author Urban Malc
 * @since 1.0.0
 */
public class RequiredLibraries {

    private static final Logger LOG = Logger.getLogger(RequiredLibraries.class.getName());

    private static final String[] LIBS_DEFAULT = {
            "com.kumuluz.ee:kumuluzee-core:",
            "com.kumuluz.ee:kumuluzee-servlet-jetty:",
            "com.kumuluz.ee:kumuluzee-cdi-weld:"
    };

    private static final String[] LIBS_MP1_0 = {
            "com.kumuluz.ee:kumuluzee-microProfile-1.0:",
    };

    private static final String[] LIBS_MP1_1 = {
            "com.kumuluz.ee:kumuluzee-microProfile-1.1:",
    };

    private static final String[] LIBS_MP1_2 = {
            "com.kumuluz.ee:kumuluzee-microProfile-1.2:",
    };

    /**
     * Returns libraries, required for each deployment (specified either using {@link MavenDependencyAppender} or
     * includeRequiredLibraries configuration property).
     *
     * @param deploymentLibs Additional libraries required by deployment.
     * @return Array of required libraries.
     */
    public static Archive<?>[] getRequiredLibraries(List<String> deploymentLibs) {
        List<String> libraries = MavenDependencyAppender.getDeclaredLibraries();
        libraries.addAll(deploymentLibs);

        KumuluzEEContainerConfig config = KumuluzEEContainerConfig.getInstance();
        String kumuluzVersion = config.getKumuluzVersion();

        List<String> includedLibs = getIncludedLibraries(config.getIncludeRequiredLibraries());
        LOG.fine("Adding libraries based on the includeRequiredLibraries config parameter (value: " +
                config.getIncludeRequiredLibraries() + "): " + String.join(", ", includedLibs));
        libraries.addAll(includedLibs);

        // append kumuluz version, if version not included
        libraries = libraries.stream().map(s -> (s.endsWith(":")) ? s + kumuluzVersion : s)
                .collect(Collectors.toList());

        ConfigurableMavenResolverSystem resolver = Maven.configureResolver();

        if (kumuluzVersion.contains("SNAPSHOT")) {
            LOG.fine("KumuluzEE version is snapshot, adding snapshot repository to Maven resolver");
            MavenRemoteRepository sonatypeSnapshots = MavenRemoteRepositories
                    .createRemoteRepository("sonatype-snapshots",
                            "https://oss.sonatype.org/content/repositories/snapshots", "default");
            sonatypeSnapshots.setUpdatePolicy(MavenUpdatePolicy.UPDATE_POLICY_DAILY);
            resolver = resolver.withRemoteRepo(sonatypeSnapshots);
        }

        resolver = MavenDependencyAppender.runResolverConfigurations(resolver);

        Archive<?>[] resolvedLibs = (libraries.isEmpty()) ? new Archive[0] :
                resolver.resolve(libraries).withTransitivity().as(JavaArchive.class);

        if (config.getIncludeRequiredLibraries().equals(KumuluzEEContainerConfig.INCLUDE_LIBS_FROM_POM)) {
            LOG.fine("Resolving compile, runtime and test dependencies from pom.xml");
            Archive<?>[] pomLibs = resolver.loadPomFromFile("pom.xml")
                    .importCompileAndRuntimeDependencies()
                    .importTestDependencies()
                    .resolve().withTransitivity().as(JavaArchive.class);

            // merge arrays
            resolvedLibs = Stream.concat(Arrays.stream(resolvedLibs), Arrays.stream(pomLibs)).toArray(Archive[]::new);
        }

        return resolvedLibs;
    }

    /**
     * Returns a list of required dependencies, specified with includeRequiredLibraries configuration property.
     *
     * @param includeRequiredLibraries includeRequiredLibraries configuration property
     * @return A list of dependencies.
     */
    private static List<String> getIncludedLibraries(String includeRequiredLibraries) {
        switch (includeRequiredLibraries) {
            case KumuluzEEContainerConfig.INCLUDE_LIBS_FALSE:
            case KumuluzEEContainerConfig.INCLUDE_LIBS_FROM_POM:
                return Collections.emptyList();
            case KumuluzEEContainerConfig.INCLUDE_LIBS_DEFAULT:
                return Arrays.asList(LIBS_DEFAULT);
            case KumuluzEEContainerConfig.INCLUDE_LIBS_MP1_0:
                return Arrays.asList(LIBS_MP1_0);
            case KumuluzEEContainerConfig.INCLUDE_LIBS_MP1_1:
                return Arrays.asList(LIBS_MP1_1);
            case KumuluzEEContainerConfig.INCLUDE_LIBS_MP1_2:
                return Arrays.asList(LIBS_MP1_2);
            default:
                throw new RuntimeException("Could not determine includeRequiredLibraries parameter: " +
                        includeRequiredLibraries);
        }
    }
}
