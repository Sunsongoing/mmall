package com.mmall.util;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

/**
 * @author Sunsongoing
 */

public class PropertiesUtil extends PropertyPlaceholderConfigurer {

    private static String property;

    public static String getProperty(String key, String what) {


        return property;
    }
}
