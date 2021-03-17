package org.geektimes.configuration.microprofile.config.source;

import org.eclipse.microprofile.config.spi.ConfigSource;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * OSPropertiesConfigSource
 *
 * @author liuhao
 */
public class OSPropertiesConfigSource implements ConfigSource {
    private final Map<String, String> properties;

    public OSPropertiesConfigSource() {
        Map osProperties = System.getenv();
        this.properties = new HashMap<>(osProperties);
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
        return "OS Properties";
    }
}
