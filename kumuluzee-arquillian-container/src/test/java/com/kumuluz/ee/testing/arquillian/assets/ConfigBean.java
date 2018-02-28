package com.kumuluz.ee.testing.arquillian.assets;

import com.kumuluz.ee.configuration.utils.ConfigurationUtil;

import javax.enterprise.context.Dependent;
import java.util.Optional;

@Dependent
public class ConfigBean {

    public Optional<String> getConfigValue() {
        return ConfigurationUtil.getInstance().get("test.string-value");
    }
}
