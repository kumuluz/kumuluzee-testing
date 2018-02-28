package com.kumuluz.ee.testing.arquillian.provider;

import org.jboss.arquillian.container.test.spi.RemoteLoadableExtension;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;

public class SimpleResourceProviderRemoteExtension implements RemoteLoadableExtension {

    @Override
    public void register(ExtensionBuilder extensionBuilder) {
        extensionBuilder.service(ResourceProvider.class, SimpleResourceProvider.class);
    }
}
