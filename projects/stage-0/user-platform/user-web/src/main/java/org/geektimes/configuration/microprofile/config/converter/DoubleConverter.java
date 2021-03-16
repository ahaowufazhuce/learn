package org.geektimes.configuration.microprofile.config.converter;

import org.eclipse.microprofile.config.spi.Converter;

/**
 * DoubleConverter
 *
 * @author pengxing on 2021/3/16
 */
public class DoubleConverter implements Converter {
    private static final long serialVersionUID = 7468855410558792490L;

    /**
     * Convert the given string value to a specified type. Callers <em>must not</em> pass in {@code null} for
     * {@code value}; doing so may result in a {@code NullPointerException} being thrown.
     *
     * @param value the string representation of a property value (must not be {@code null})
     * @return the converted value, or {@code null} if the value is empty
     * @throws IllegalArgumentException if the value cannot be converted to the specified type
     * @throws NullPointerException     if the given value was {@code null}
     */
    @Override
    public Double convert(String value) throws IllegalArgumentException, NullPointerException {
        return Double.valueOf(value);
    }
}
