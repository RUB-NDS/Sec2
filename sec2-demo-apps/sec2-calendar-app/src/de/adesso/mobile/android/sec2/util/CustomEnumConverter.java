package de.adesso.mobile.android.sec2.util;

import java.lang.reflect.Method;

import com.thoughtworks.xstream.converters.SingleValueConverter;
import com.thoughtworks.xstream.converters.enums.EnumConverter;

public class CustomEnumConverter<T extends Enum<T>> extends EnumConverter implements SingleValueConverter {

    private Class<T> enumType;
    private Method method;

    // This is the method name each enum class must contain.
    private final static String METHOD_NAME = "getEnum";

    public static <V extends Enum<V>> SingleValueConverter create(Class<V> enumClass) {
        return new CustomEnumConverter<V>(enumClass);
    }

    private CustomEnumConverter(Class<T> newEnumType) {
        this.enumType = newEnumType;
        try {
            method = enumType.getMethod(METHOD_NAME, String.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings ("rawtypes")
    @Override
    public boolean canConvert(Class type) {
        return type == enumType;
    }

    @Override
    public Object fromString(String str) {
        try {
            return method.invoke(null, str);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Enum.valueOf(enumType, "");
    }

    @Override
    public String toString(Object obj) {
        return obj.toString();
    }

}
