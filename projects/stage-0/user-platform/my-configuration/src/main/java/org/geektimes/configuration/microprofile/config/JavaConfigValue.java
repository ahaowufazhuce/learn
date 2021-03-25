package org.geektimes.configuration.microprofile.config;

import org.eclipse.microprofile.config.ConfigValue;
import org.eclipse.microprofile.config.spi.ConfigSource;

/**
 * JavaConfigValue
 *
 * @author pengxing on 2021/3/15
 */
public class JavaConfigValue implements ConfigValue {
    private ConfigSource configSource;
    private String name;

    public JavaConfigValue(ConfigSource configSource, String name) {
        this.configSource = configSource;
        this.name = name;
    }

    /**
     * The name of the property.
     *
     * @return the name of the property.
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * The value of the property lookup with transformations (expanded, etc).
     *
     * @return the value of the property lookup or {@code null} if the property could not be found
     */
    @Override
    public String getValue() {
        return configSource.getValue(name);
    }

    /**
     * The value of the property lookup without any transformation (expanded , etc).
     *
     * @return the raw value of the property lookup or {@code null} if the property could not be found.
     */
    @Override
    public String getRawValue() {
        return configSource.getValue(name);
    }

    /**
     * The {@link ConfigSource} name that loaded the property lookup.
     *
     * @return the ConfigSource name that loaded the property lookup or {@code null} if the property could not be found
     */
    @Override
    public String getSourceName() {
        return configSource.getName();
    }

    /**
     * The {@link ConfigSource} ordinal that loaded the property lookup.
     *
     * @return the ConfigSource ordinal that loaded the property lookup or {@code 0} if the property could not be found
     */
    @Override
    public int getSourceOrdinal() {
        return configSource.getOrdinal();
    }
}
