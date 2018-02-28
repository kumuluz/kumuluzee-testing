package com.kumuluz.ee.testing.arquillian.utils;

import com.kumuluz.ee.testing.arquillian.KumuluzEEContainerConfig;
import com.kumuluz.ee.testing.arquillian.spi.MavenDependencyAppender;
import org.jboss.shrinkwrap.resolver.api.maven.ConfigurableMavenResolverSystem;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.repository.MavenRemoteRepositories;
import org.jboss.shrinkwrap.resolver.api.maven.repository.MavenRemoteRepository;
import org.jboss.shrinkwrap.resolver.api.maven.repository.MavenUpdatePolicy;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class RequiredLibraries {

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

    public static File[] getRequiredLibraries() {
        List<String> libraries = MavenDependencyAppender.getDeclaredLibraries();

        KumuluzEEContainerConfig config = KumuluzEEContainerConfig.getInstance();
        String kumuluzVersion = config.getKumuluzVersion();

        libraries.addAll(getIncludedLibraries(config.getIncludeRequiredLibraries()));

        // append kumuluz version, if version not included
        libraries = libraries.stream().map(s -> (s.endsWith(":")) ? s + kumuluzVersion : s)
                .collect(Collectors.toList());

        if (libraries.isEmpty()) {
            return new File[] {};
        }

        ConfigurableMavenResolverSystem resolver = Maven.configureResolver();

        if (kumuluzVersion.contains("SNAPSHOT")) {
            MavenRemoteRepository sonatypeSnapshots = MavenRemoteRepositories
                    .createRemoteRepository("sonatype-snapshots",
                            "https://oss.sonatype.org/content/repositories/snapshots", "default");
            sonatypeSnapshots.setUpdatePolicy(MavenUpdatePolicy.UPDATE_POLICY_DAILY);
            resolver = resolver.withRemoteRepo(sonatypeSnapshots);
        }

        return resolver.resolve(libraries).withTransitivity().asFile();


    }

    private static List<String> getIncludedLibraries(String includeRequiredLibraries) {
        switch (includeRequiredLibraries) {
            case KumuluzEEContainerConfig.INCLUDE_LIBS_FALSE:
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
