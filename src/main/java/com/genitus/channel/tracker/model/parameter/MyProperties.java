package com.genitus.channel.tracker.model.parameter;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class MyProperties {
    private static Logger logger = LoggerFactory.getLogger(MyProperties.class);
    public static String DownloadDirectory="";

    public static void readProperties()  {
        try {
            Properties pro = new Properties();
            FileInputStream in = new FileInputStream("my.properties");
            pro.load(in);
            in.close();
            DownloadDirectory=pro.getProperty("DownloadDirectory");
            System.out.println(DownloadDirectory);
        }catch (IOException e){
            logger.info("my.properties file does not exist");
        }
    }

    static {
        readProperties();
        logger.info("properties 被加载了");
        logger.info("DownloadDirectory is: "+DownloadDirectory);
    }


}

class Test{
    public static void main(String[] args)throws Exception{
        System.out.println("DownloadDirectory is: "+MyProperties.DownloadDirectory);
    }
}