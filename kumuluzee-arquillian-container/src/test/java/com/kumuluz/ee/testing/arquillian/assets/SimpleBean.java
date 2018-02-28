package com.kumuluz.ee.testing.arquillian.assets;

import javax.enterprise.context.Dependent;

@Dependent
public class SimpleBean {

    public String sayHello() {
        return "bean-hello";
    }
}
