package com.genitus.channel.tracker.demo;

/*import com.genitus.channel.tracker.client.HBaseClient;
import org.apache.avro.Schema;
import org.genitus.forceps.hbase.LogData;


import java.util.Date;
import java.util.List;*/

/**
 * 这段代码主要是测试 从hbase数据中获取数据的时延 和重复请求，找到解决方案
 */
import org.genitus.forceps.hbase.HBaseClient;
import org.genitus.forceps.hbase.LogData;

import java.io.IOException;
import java.util.List;

//java -cp lib/*:genitus-tracker-5.1-SNAPSHOT.jar com.genitus.channel.tracker.demo.Demo1 172.28.3.20 adtevak r d  iate8be4c52@nc15f041d10630016540
public class Demo1 {
    public static void main(String[] args) throws Exception{
       System.out.println("find log...");
       find(args[0],args[1]);
    }

    public static void find(String ipPort,String sid)throws IOException {
        HBaseClient hBaseClient = new HBaseClient.Builder( ipPort,"adtevak","r","d").build();
        List<LogData> list  = hBaseClient.getLogs(sid);
        System.out.println("list.size() is: "+list.size());
    }


    public static long toTimestamp(String sid) {
        if (null == sid || sid.length() != 32) {
            return -1;
        }
        return Long.parseLong(sid.substring(14, 25), 16);
    }
}
/* Schema schema = new Schema.Parser().parse("{\"type\":\"record\",\"name\":\"SvcLog\",\"namespace\":\"org.genitus.karyo.model.log\",\"fields\":[{\"name\":\"type\",\"type\":\"int\"},{\"name\":\"sid\",\"type\":\"string\"},{\"name\":\"uid\",\"type\":\"string\"},{\"name\":\"syncid\",\"type\":\"int\"},{\"name\":\"timestamp\",\"type\":\"long\"},{\"name\":\"ip\",\"type\":\"int\"},{\"name\":\"callName\",\"type\":\"string\"},{\"name\":\"logs\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"RawLog\",\"fields\":[{\"name\":\"timestamp\",\"type\":\"long\"},{\"name\":\"level\",\"type\":\"string\"},{\"name\":\"extras\",\"type\":{\"type\":\"map\",\"values\":\"string\"}},{\"name\":\"descs\",\"type\":{\"type\":\"array\",\"items\":\"string\",\"java-class\":\"java.util.List\"}}]},\"java-class\":\"java.util.List\"}},{\"name\":\"mediaData\",\"type\":{\"type\":\"record\",\"name\":\"MediaData\",\"fields\":[{\"name\":\"type\",\"type\":\"int\"},{\"name\":\"data\",\"type\":{\"type\":\"bytes\",\"java-class\":\"[B\"}}]}}]}");
        HBaseClient hBaseClient = new HBaseClient(args[0],args[1],args[2],args[3],schema);
        org.genitus.forceps.hbase.HBaseClient hBaseClient1 = hBaseClient.getClient();

        List<LogData> list =  hBaseClient1.getLogs(args[4]);

        System.out.println("list.size() is: "+list.size());
        long time = Demo1.toTimestamp(args[4]);
        Date date = new Date(time);
        System.out.println(date.toString());*/