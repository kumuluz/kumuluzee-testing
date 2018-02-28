package com.kumuluz.ee.testing.arquillian.provider;

import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;

public class SimpleResourceProviderExtension implements LoadableExtension {

    @Override
    public void register(ExtensionBuilder extensionBuilder) {
        extensionBuilder.service(ResourceProvider.class, SimpleResourceProvider.class);
    }
}
