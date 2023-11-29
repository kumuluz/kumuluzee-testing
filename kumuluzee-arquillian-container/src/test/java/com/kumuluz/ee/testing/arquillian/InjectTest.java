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

import com.kumuluz.ee.testing.arquillian.assets.SimpleBean;
import com.kumuluz.ee.testing.arquillian.provider.SimplePojo;
import com.kumuluz.ee.testing.arquillian.provider.SimpleResourceProvider;
import com.kumuluz.ee.testing.arquillian.provider.SimpleResourceProviderRemoteExtension;
import jakarta.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.spi.RemoteLoadableExtension;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.naming.InitialContext;

/**
 * Tests CDI injection, injection of {@link ArquillianResource} and injection of {@link InitialContext}.
 *
 * @author Urban Malc
 * @since 1.0.0
 */
@ExtendWith(ArquillianExtension.class)
public class InjectTest {

    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class)
                .addClass(SimpleBean.class)
                .addClass(SimplePojo.class)
                .addClass(SimpleResourceProvider.class)
                .addClass(SimpleResourceProviderRemoteExtension.class)
                .addAsServiceProvider(RemoteLoadableExtension.class, SimpleResourceProviderRemoteExtension.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    private SimpleBean bean;

    @ArquillianResource
    private SimplePojo arquillianProvidedPojo;

    @ArquillianResource
    private InitialContext initialContext;

    @Test
    void injectTest() {
        Assertions.assertNotNull(bean);
        Assertions.assertEquals("bean-hello", bean.sayHello());
    }

    @Test
    void arquillianResourceTest() {
        Assertions.assertNotNull(arquillianProvidedPojo);
        Assertions.assertEquals(42, arquillianProvidedPojo.getNumber());
    }

    @Test
    void initialContextTest() {
        Assertions.assertNotNull(initialContext);
    }
}
