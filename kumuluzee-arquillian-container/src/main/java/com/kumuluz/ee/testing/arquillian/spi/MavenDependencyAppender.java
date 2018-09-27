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
package com.kumuluz.ee.testing.arquillian.spi;

import org.jboss.shrinkwrap.resolver.api.maven.ConfigurableMavenResolverSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Logger;

/**
 * Service Loader interface, used for collecting dependencies, which must be added to each deployment.
 *
 * @author Urban Malc
 * @since 1.0.0
 */
public interface MavenDependencyAppender {

    /**
     * Exposes Maven resolver system for additional configuration, before using it to resolve dependencies.
     *
     * @param resolver Exposed resolver.
     * @return Configured resolver.
     */
    default ConfigurableMavenResolverSystem configureResolver(ConfigurableMavenResolverSystem resolver) {
        return resolver;
    }

    /**
     * Should return a list of dependencies to be added to each deployment.
     *
     * Each specified dependency must be given as {@link String} in following format:
     * {@literal <groupId>:<artifactId>:<version>}.
     * If the {@literal <version>} is ommitted, the kumuluzVersion configuration property is used.
     *
     * @return A list of dependencies.
     */
    List<String> addLibraries();

    static List<String> getDeclaredLibraries() {
        final Logger LOG = Logger.getLogger(MavenDependencyAppender.class.getName());

        List<String> libraries = new ArrayList<>();
        ServiceLoader.load(MavenDependencyAppender.class)
                .forEach(mda -> {
                    LOG.fine("Adding libraries from " + mda.getClass().getSimpleName());
                    libraries.addAll(mda.addLibraries());
                });

        return libraries;
    }

    static ConfigurableMavenResolverSystem runResolverConfigurations(ConfigurableMavenResolverSystem resolver) {
        final Logger LOG = Logger.getLogger(MavenDependencyAppender.class.getName());

        for (MavenDependencyAppender mda : ServiceLoader.load(MavenDependencyAppender.class)) {
            LOG.fine("Configuring resolver with " + mda.getClass().getSimpleName());
            resolver = mda.configureResolver(resolver);
        }

        return resolver;
    }
}
