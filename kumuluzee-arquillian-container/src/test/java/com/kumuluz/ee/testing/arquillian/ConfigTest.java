package com.kumuluz.ee.testing.arquillian;

import com.kumuluz.ee.testing.arquillian.assets.ConfigBean;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.Optional;

@RunWith(Arquillian.class)
public class ConfigTest {

    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class)
                .addClass(ConfigBean.class)
                .addAsResource("simple-config.yml", "config.yml")
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    private ConfigBean configBean;

    @Test
    public void configTest() {
        Optional<String> configValue = configBean.getConfigValue();

        Assert.assertTrue(configValue.isPresent());
        Assert.assertEquals("config-hello", configValue.get());
    }
}
