package org.geektimes.configuration.microprofile.config.source;

import org.eclipse.microprofile.config.spi.ConfigSource;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * ApplicationConfigSource
 *
 * @author liuhao
 */
public class ApplicationConfigSource implements ConfigSource {

    /**
     * 应用属性
     */
    private final Map<String, String> properties;

    public ApplicationConfigSource() {
        Map applicationProperties = init();
        this.properties = new HashMap<>(applicationProperties);
    }

    public Map init() {
        try {
            InputStream inputStream = ApplicationConfigSource.class.getResourceAsStream("/META-INF/application.properties");
            Properties properties = new Properties();
            properties.load(inputStream);
            return properties;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new HashMap<>();
    }

    @Override
    public Set<String> getPropertyNames() {
        return properties.keySet();
    }

    @Override
    public String getValue(String propertyName) {
        return properties.get(propertyName);
    }

    @Override
    public String getName() {
        return "Application Config";
    }
}
