package com.mmall.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * @author Sunsongoing
 */

public class PropertiesUtil extends PropertyPlaceholderConfigurer {

    private static Logger logger = LoggerFactory.getLogger(PropertiesUtil.class);
    private static Properties props;

    static {
        loadProps();
    }


    synchronized static private void loadProps() {
        logger.info("开始加载properties文件内容.......");
        String fileName = "mmall.properties";
        props = new Properties();
        InputStreamReader in = null;
        try {
            //第一种，通过类加载器进行获取properties文件流
            in = new InputStreamReader(PropertiesUtil.class.getClassLoader().getResourceAsStream(fileName), "UTF-8");
            // 第二种，通过类进行获取properties文件流
            //in = PropertiesUtil.class.getResourceAsStream("/jdbc.properties");
            props.load(in);
        } catch (FileNotFoundException e) {
            logger.error(fileName + "文件未找到");
        } catch (IOException e) {
            logger.error(fileName + "出现IOException");
        } finally {
            try {
                if (null != in) {
                    in.close();
                }
            } catch (IOException e) {
                logger.error(fileName + "文件流关闭出现异常");
            }
        }
        logger.info("加载" + fileName + "文件内容完成...........");
        logger.info(fileName + "文件内容：" + props);
    }

    public static String getProperty(String key) {
        if (null == props) {
            loadProps();
        }
        return props.getProperty(key.trim());
    }

    public static String getProperty(String key, String defaultValue) {
        if (null == props) {
            loadProps();
        }
        // (val == null) ? default : val;
        return props.getProperty(key.trim(), defaultValue);
    }
}

