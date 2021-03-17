package org.geektimes.configuration.microprofile.config.source;

import org.eclipse.microprofile.config.spi.ConfigSource;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
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

    public Map<String, String> init() {
        Map<String, String> result = new HashMap<>();
        try {
            InputStream inputStream = ApplicationConfigSource.class.getResourceAsStream("/META-INF/application.properties");
            byte[] array = new byte[128];
            int byteNumber = inputStream.read(array);
            String context = new String(array, 0, byteNumber, "UTF-8");
            String[] lines = context.split("\n");
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                if (!line.contains("=") || line.startsWith("#")) {
                    continue;
                }
                String[] lineArray = line.split("=");
                result.put(lineArray[0], lineArray[1]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
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
