package com.kumuluz.ee.testing.arquillian.utils;

import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;

public class ApplicationArchiveMarker implements ApplicationArchiveProcessor {

    public static final String MARKER_FILENAME = "/__kumuluzee_arquillian_marker__";

    @Override
    public void process(Archive<?> archive, TestClass testClass) {
        archive.add(EmptyAsset.INSTANCE, MARKER_FILENAME);
    }
}
