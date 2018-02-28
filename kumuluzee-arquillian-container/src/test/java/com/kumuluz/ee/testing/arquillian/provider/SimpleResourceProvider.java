package com.kumuluz.ee.testing.arquillian.provider;

import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;

import java.lang.annotation.Annotation;

public class SimpleResourceProvider implements ResourceProvider {

    @Override
    public boolean canProvide(Class<?> aClass) {
        return aClass.isAssignableFrom(SimplePojo.class);
    }

    @Override
    public Object lookup(ArquillianResource arquillianResource, Annotation... annotations) {
        SimplePojo pojo = new SimplePojo();
        pojo.setNumber(42);

        return pojo;
    }
}
