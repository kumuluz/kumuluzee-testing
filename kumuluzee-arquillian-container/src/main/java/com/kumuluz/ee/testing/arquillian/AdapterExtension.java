package com.kumuluz.ee.testing.arquillian;

import com.kumuluz.ee.testing.arquillian.utils.ApplicationArchiveMarker;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.core.spi.LoadableExtension;

public class AdapterExtension implements LoadableExtension {

    public void register(ExtensionBuilder extensionBuilder) {
        extensionBuilder.service(DeployableContainer.class, KumuluzEEContainer.class);
        extensionBuilder.service(ApplicationArchiveProcessor.class, ApplicationArchiveMarker.class);
    }
}
