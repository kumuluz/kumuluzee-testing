package com.kumuluz.ee.testing.arquillian;

import com.kumuluz.ee.testing.arquillian.assets.SimpleBean;
import com.kumuluz.ee.testing.arquillian.provider.SimplePojo;
import com.kumuluz.ee.testing.arquillian.provider.SimpleResourceProvider;
import com.kumuluz.ee.testing.arquillian.provider.SimpleResourceProviderRemoteExtension;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.spi.RemoteLoadableExtension;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.naming.InitialContext;

@RunWith(Arquillian.class)
public class InjectWebArchiveTest {

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addAsLibrary(ShrinkWrap.create(JavaArchive.class)
                        .addClass(SimpleBean.class)
                        .addClass(SimplePojo.class)
                        .addClass(SimpleResourceProvider.class)
                        .addClass(SimpleResourceProviderRemoteExtension.class)
                        .addAsServiceProvider(RemoteLoadableExtension.class, SimpleResourceProviderRemoteExtension.class)
                        .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml"));
    }

    @Inject
    private SimpleBean bean;

    @ArquillianResource
    private SimplePojo arquillianProvidedPojo;

    @ArquillianResource
    private InitialContext initialContext;

    @Test
    public void injectTest() {
        Assert.assertNotNull(bean);
        Assert.assertEquals("bean-hello", bean.sayHello());
    }

    @Test
    public void arquillianResourceTest() {
        Assert.assertNotNull(arquillianProvidedPojo);
        Assert.assertEquals(42, arquillianProvidedPojo.getNumber());
    }

    @Test
    public void initialContextTest() {
        Assert.assertNotNull(initialContext);
    }
}
