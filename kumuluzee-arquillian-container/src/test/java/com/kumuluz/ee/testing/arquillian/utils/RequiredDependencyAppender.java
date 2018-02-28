package com.kumuluz.ee.testing.arquillian.utils;

import com.kumuluz.ee.testing.arquillian.spi.MavenDependencyAppender;

import java.util.Collections;
import java.util.List;

public class RequiredDependencyAppender implements MavenDependencyAppender {

    @Override
    public List<String> addLibraries() {
        return Collections.singletonList("com.kumuluz.ee:kumuluzee-jax-rs-jersey:");
    }
}
