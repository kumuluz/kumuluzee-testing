package com.kumuluz.ee.testing.arquillian;

import com.kumuluz.ee.testing.arquillian.assets.DescriptorServlet;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.is;

@RunWith(Arquillian.class)
public class DescriptorServletTest {

    @Deployment(testable = false)
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class)
                .addClass(DescriptorServlet.class)
                .addAsResource("descriptors/web.xml", "webapp/WEB-INF/web.xml");
    }

    @Test
    @RunAsClient
    public void responseTest() {
        when().get("/test").then().statusCode(200).and().body(is("descriptor-hello"));
    }
}
