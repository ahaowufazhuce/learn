package org.geektimes.configuration.microprofile.config;


import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigValue;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.config.spi.Converter;

import java.util.*;

/**
 * JavaConfig
 *
 * @author Ma
 */
public class JavaConfig implements Config {
    /**
     * 内部可变的集合，不要直接暴露在外面
     */
    private List<ConfigSource> configSources = new LinkedList<>();
    private List<Converter> converters = new LinkedList<>();

    /**
     * 比较器
     */
    private static Comparator<ConfigSource> configSourceComparator = (o1, o2) -> Integer.compare(o2.getOrdinal(), o1.getOrdinal());

    /**
     * 构造方法
     * 1.获取classLoader
     * 2.加载实现了ConfigSource接口的类
     * 3.将ConfigSource所有实现加入列表并排序
     */
    public JavaConfig() {
        ClassLoader classLoader = getClass().getClassLoader();
        ServiceLoader<ConfigSource> serviceLoader = ServiceLoader.load(ConfigSource.class, classLoader);
        ServiceLoader<Converter> converterServiceLoader = ServiceLoader.load(Converter.class, classLoader);
        serviceLoader.forEach(configSources::add);
        converterServiceLoader.forEach(converters::add);
        configSources.sort(configSourceComparator);
    }

    /**
     * 查找配置项的值 指定返回值类型
     *
     * @param propertyName 属性名称
     * @param propertyType 指定的返回值类型
     * @param <T>
     * @return
     */
    @Override
    public <T> T getValue(String propertyName, Class<T> propertyType) {
        String propertyValue = getPropertyValue(propertyName);
        if (String.class.equals(propertyType)) {
            return (T) propertyValue;
        }
        return getConverter(propertyType).get().convert(propertyValue);
    }

    @Override
    public ConfigValue getConfigValue(String propertyName) {
        ConfigValue configValue = null;
        for (ConfigSource configSource : configSources) {
            String propertyValue = configSource.getValue(propertyName);
            if (propertyValue != null) {
                configValue = new JavaConfigValue(configSource, propertyName);
                break;
            }
        }
        return configValue;
    }

    /**
     * 查找配置项的值 不指定返回值类型
     * 1.遍历configSource
     * 2.使用key查找value，一旦此configSource中有，就停止遍历返回这个value
     *
     * @param propertyName
     * @return
     */
    protected String getPropertyValue(String propertyName) {
        String propertyValue = null;
        for (ConfigSource configSource : configSources) {
            propertyValue = configSource.getValue(propertyName);
            if (propertyValue != null) {
                break;
            }
        }
        return propertyValue;
    }

    @Override
    public <T> Optional<T> getOptionalValue(String propertyName, Class<T> propertyType) {
        T value = getValue(propertyName, propertyType);
        return Optional.ofNullable(value);
    }

    @Override
    public Iterable<String> getPropertyNames() {
        return configSources.get(0).getProperties().keySet();
    }

    @Override
    public Iterable<ConfigSource> getConfigSources() {
        return Collections.unmodifiableList(configSources);
    }

    /**
     * 提供转换器
     *
     * @param forType
     * @param <T>
     * @return
     */
    @Override
    public <T> Optional<Converter<T>> getConverter(Class<T> forType) {
        for (Converter converter : converters) {
            if (converter.getClass().getMethods()[0].getReturnType().equals(forType)) {
                return Optional.ofNullable(converter);
            }
        }
        return Optional.empty();
    }

    @Override
    public <T> T unwrap(Class<T> type) {
        return null;
    }
}
