package com.kumuluz.ee.testing.arquillian;

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

public class KumuluzEEContainer implements DeployableContainer<KumuluzEEContainerConfig> {

    private static final Logger log = Logger.getLogger(KumuluzEEContainer.class.getName());

    private Map<Archive, AbstractDeployment> deployments;

    public KumuluzEEContainer() {
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

    @Override
    public void start() {
        // NO-OP
    }

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
        log.info("Deploying " + archive.getName());

        KumuluzEEContainerConfig containerConfig = KumuluzEEContainerConfig.getInstance();

        AbstractDeployment deployment;

        switch (containerConfig.getPackaging()) {
            case KumuluzEEContainerConfig.PACKAGING_UBER_JAR:
                Archive<?> uberJar = ArchiveUtils.generateUberJar(archive);
                log.fine("UberJar: " + uberJar.toString(true));
                deployment = new UberJarDeployment(uberJar);
                break;
            case KumuluzEEContainerConfig.PACKAGING_EXPLODED:
                Archive<?> exploded = ArchiveUtils.generateExploded(archive);
                log.fine("Exploded structure: " + exploded.toString(true));
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
