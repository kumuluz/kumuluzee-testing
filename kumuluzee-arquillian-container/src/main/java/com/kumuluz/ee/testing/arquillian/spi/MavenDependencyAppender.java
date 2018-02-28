package com.kumuluz.ee.testing.arquillian.spi;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public interface MavenDependencyAppender {

    List<String> addLibraries();

    static List<String> getDeclaredLibraries() {
        List<String> libraries = new ArrayList<>();

        ServiceLoader.load(MavenDependencyAppender.class)
                .forEach(mavenDependencyAppender -> libraries.addAll(mavenDependencyAppender.addLibraries()));

        return libraries;
    }
}
