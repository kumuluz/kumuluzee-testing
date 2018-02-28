package com.kumuluz.ee.testing.arquillian;

import com.kumuluz.ee.testing.arquillian.provider.SimplePojo;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@SuppressWarnings("ArquillianDeploymentAbsent")
@RunWith(Arquillian.class)
@RunAsClient
public class ArquillianResourceTest {

    @ArquillianResource
    private SimplePojo simplePojo;

    @Test
    public void arquillianResourceTest() {
        Assert.assertNotNull(simplePojo);
        Assert.assertEquals(42, simplePojo.getNumber());
    }
}
