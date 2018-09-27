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
package com.kumuluz.ee.testing.arquillian;

import com.kumuluz.ee.logs.impl.JavaUtilDefaultLogConfigurator;
import com.kumuluz.ee.testing.arquillian.deployment.AbstractDeployment;
import com.kumuluz.ee.testing.arquillian.deployment.ExplodedDeployment;
import com.kumuluz.ee.testing.arquillian.deployment.UberJarDeployment;
import com.kumuluz.ee.testing.arquillian.utils.ArchiveUtils;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.container.spi.client.protocol.metadata.HTTPContext;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * KumuluzEE Container Adapter.
 *
 * @author Urban Malc
 * @since 1.0.0
 */
public class KumuluzEEContainer implements DeployableContainer<KumuluzEEContainerConfig> {

    private final Logger LOG; // initialized in constructor after logging init

    private static boolean loggingInitialized = false;

    private static synchronized void initLogging() {
        if (!loggingInitialized) {
            // initialize logging the same way as in KumuluzEE
            JavaUtilDefaultLogConfigurator.init();
            loggingInitialized = true;
        }
    }

    private Map<Archive, AbstractDeployment> deployments;

    public KumuluzEEContainer() {
        initLogging();
        this.LOG = Logger.getLogger(KumuluzEEContainer.class.getName());
        this.deployments = new HashMap<>();
    }

    @Override
    public Class<KumuluzEEContainerConfig> getConfigurationClass() {
        return KumuluzEEContainerConfig.class;
    }

    @Override
    public void setup(KumuluzEEContainerConfig containerConfig) {
        KumuluzEEContainerConfig.init(containerConfig);
    }

    /**
     * Not implemented since application is packaged with the KumuluzEE Server and each deployment starts its own
     * instance.
     */
    @Override
    public void start() {
        // NO-OP
    }

    /**
     * Not implemented since application is packaged with the KumuluzEE Server and each deployment starts its own
     * instance.
     */
    @Override
    public void stop() {
        // NO-OP
    }

    @Override
    public ProtocolDescription getDefaultProtocol() {
        return new ProtocolDescription("Servlet 3.0");
    }

    @Override
    public void deploy(Descriptor descriptor) {
        throw new UnsupportedOperationException("Deploying descriptors is not supported");
    }

    @Override
    public void undeploy(Descriptor descriptor) {
        throw new UnsupportedOperationException("Undeploying descriptors is not supported");
    }

    @Override
    public void undeploy(Archive<?> archive) throws DeploymentException {
        AbstractDeployment deployment = deployments.get(archive);

        if (deployment == null) {
            throw new DeploymentException("Could not find deployed archive " + archive.getName());
        }

        deployment.stop();
        deployments.remove(archive);
    }

    @Override
    public ProtocolMetaData deploy(Archive<?> archive) throws DeploymentException {
        LOG.info("Deploying " + archive.getName());

        KumuluzEEContainerConfig containerConfig = KumuluzEEContainerConfig.getInstance();

        AbstractDeployment deployment;

        switch (containerConfig.getPackaging()) {
            case KumuluzEEContainerConfig.PACKAGING_UBER_JAR:
                Archive<?> uberJar = ArchiveUtils.generateUberJar(archive);
                LOG.fine("UberJar: " + uberJar.toString(true));
                deployment = new UberJarDeployment(uberJar);
                break;
            case KumuluzEEContainerConfig.PACKAGING_EXPLODED:
                Archive<?> exploded = ArchiveUtils.generateExploded(archive);
                LOG.fine("Exploded structure: " + exploded.toString(true));
                deployment = new ExplodedDeployment(exploded);
                break;
            default:
                throw new DeploymentException("Unrecognised deployment: " + containerConfig.getPackaging());
        }

        deployment.init();

        deployments.put(archive, deployment);

        HTTPContext context = deployment.start();

        ProtocolMetaData metaData = new ProtocolMetaData();
        metaData.addContext(context);
        return metaData;
    }

}
