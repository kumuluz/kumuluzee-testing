package com.kumuluz.ee.testing.arquillian.jaxrs;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.is;

@RunWith(Arquillian.class)
public class JaxRsTest {
    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class)
                .addClass(SimpleApp.class)
                .addClass(SimpleResource.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void dependencyAppenderTest() throws ClassNotFoundException {
        Assert.assertNotNull(Class.forName("javax.ws.rs.core.Application"));
    }

    @Test
    @RunAsClient
    public void resourceTest() {
        when().get("/test").then().statusCode(200).and().body(is("hello"));
    }
}
