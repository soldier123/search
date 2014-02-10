package utils;

import play.Logger;

import java.util.Arrays;
import java.util.List;

/**
 * User: 刘建力(liujianli@gtadata.com))
 * Date: 13-3-26
 * Time: 下午5:50
 * 功能描述:
 */
public class ValueUtil {
    public static <T> T getIfEmpty(Object value, T defaultValue, ValueType type) {
        if (!(value instanceof String)) {
            Logger.warn("不是String类型");
        }
        String castValue = String.valueOf(value);
        if (castValue != null && !"".equals(castValue.trim()) && !"null".equals(castValue)) {
            switch (type) {
                case INTEGER:
                    return (T) Integer.valueOf(castValue);
                case LONG:
                    return (T) Long.valueOf(castValue);
                case DOUBLE:
                    return (T) Double.valueOf(castValue);
                case STRING:
                    return (T) castValue;
            }
        }
        return defaultValue;
    }

    public static <T> List<T> wrapSingleToList(T value) {
        List<T> list = Arrays.asList(value);
        return list;
    }

    public static Object[] toLowercase(Object[] value) {
        if (value == null) {
            return null;
        } else {
            for(int i = 0 ;i<value.length;i++){
                String val = String.valueOf(value[i]).toLowerCase();
                value[i] = val;
            }
            return value;
        }
    }
    public static Object[] toUpcase(Object[] value) {
        if (value == null) {
            return null;
        } else {
            for(int i = 0 ;i<value.length;i++){
                String val = String.valueOf(value[i]).toUpperCase();
                value[i] = val;
            }
            return value;
        }
    }

    public enum ValueType {
        INTEGER,
        LONG,
        DOUBLE,
        STRING
    }
}
