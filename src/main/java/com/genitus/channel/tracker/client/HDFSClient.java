package com.genitus.channel.tracker.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.genitus.channel.tracker.model.hbaseResult.HBaseResult;
import com.genitus.channel.tracker.util.audio.*;
import com.genitus.channel.tracker.util.hdfsthrift.ThriftConnectionPoolFactory;
import com.genitus.channel.tracker.util.hdfsthrift.ThriftPoolConfig;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericArray;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransport;
import org.genitus.karyo.model.data.SessionData;
import org.genitus.karyo.model.log.SvcLog;
import org.genitus.sextant.SextantService;
import org.jcodings.util.Hash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.*;

import static com.alibaba.fastjson.JSON.parseObject;


public class HDFSClient {
    private static Logger logger = LoggerFactory.getLogger(HDFSClient.class);
    private ThriftConnectionPoolFactory pool;
    private Schema mediaSchema;
    private Schema dataSchema;
  //  private Schema mediaSchema =   new Schema.Parser().parse("{\"type\":\"record\",\"name\":\"SessionMedia\",\"namespace\":\"org.genitus.karyo.model.data\",\"fields\":[{\"name\":\"sid\",\"type\":\"string\"},{\"name\":\"uid\",\"type\":\"string\"},{\"name\":\"recStatus\",\"type\":\"int\"},{\"name\":\"mediaData\",\"type\":{\"type\":\"record\",\"name\":\"MediaData\",\"namespace\":\"org.genitus.karyo.model.log\",\"fields\":[{\"name\":\"type\",\"type\":\"int\"},{\"name\":\"data\",\"type\":{\"type\":\"bytes\",\"java-class\":\"[B\"}}]}}]}");
 //   private Schema dataSchema= new Schema.Parser().parse("{\"type\":\"record\",\"name\":\"SessionData\",\"namespace\":\"org.genitus.karyo.model.data\",\"fields\":[{\"name\":\"sid\",\"type\":\"string\"},{\"name\":\"svcLogs\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"SvcLog\",\"namespace\":\"org.genitus.karyo.model.log\",\"fields\":[{\"name\":\"type\",\"type\":\"int\"},{\"name\":\"sid\",\"type\":\"string\"},{\"name\":\"uid\",\"type\":\"string\"},{\"name\":\"syncid\",\"type\":\"int\"},{\"name\":\"timestamp\",\"type\":\"long\"},{\"name\":\"ip\",\"type\":\"int\"},{\"name\":\"callName\",\"type\":\"string\"},{\"name\":\"logs\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"RawLog\",\"fields\":[{\"name\":\"timestamp\",\"type\":\"long\"},{\"name\":\"level\",\"type\":\"string\"},{\"name\":\"extras\",\"type\":{\"type\":\"map\",\"values\":\"string\"}},{\"name\":\"descs\",\"type\":{\"type\":\"array\",\"items\":\"string\",\"java-class\":\"java.util.List\"}}]},\"java-class\":\"java.util.List\"}},{\"name\":\"mediaData\",\"type\":{\"type\":\"record\",\"name\":\"MediaData\",\"fields\":[{\"name\":\"type\",\"type\":\"int\"},{\"name\":\"data\",\"type\":{\"type\":\"bytes\",\"java-class\":\"[B\"}}]}}]},\"java-class\":\"java.util.List\"}}]}");    //  args[7] : new schema string , use to parse hdfs data

   // private static final List<String> AUDIO_EXTRAS = Arrays.asList(PropertiesUtils.getProperty("logs.extras.fields", "").split(","));
  //  private static final List<String> extras = Arrays.asList(("sub,eng_host,s_city,timestamp,appid,aue,auf,caller_name,client_ip,cver,domain,ent,imei,imsi,msc_mac,sn,country,sub_ntt,prs,ptt,rse,rst,sch,scn,sent,uid,age,gender,ret,province,city,operator,caller_appid,openudid,sub_ntt,net_subtype,net_type").split(","));
   private static final List<String> extras = Arrays.asList(("sub,eng_host,s_city,timestamp,appid,aue,auf,caller_name,client_ip,cver,domain,ent,imei,imsi,msc_mac,sn,country,sub_ntt,prs,ptt,rse,rst,sch,scn,sent,uid,age,gender,ret,province,city,operator,caller_appid,openudid,sub_ntt,net_subtype,net_type,timestamp").split(","));

    public HDFSClient(String host,int port ,int timeout){
        ThriftPoolConfig thriftPoolConfig = new ThriftPoolConfig( host, port , timeout);
        pool= new ThriftConnectionPoolFactory(thriftPoolConfig);
        logger.info("pool is initialized");
    }

    public HDFSClient(String ipPort,Schema mediaSchema,Schema dataSchema){
        this.dataSchema = dataSchema;
        this.mediaSchema = mediaSchema;
        String[] elem = ipPort.split(":");
        ThriftPoolConfig thriftPoolConfig = new ThriftPoolConfig( elem[0], Integer.parseInt(elem[1]) , 5000);
        pool= new ThriftConnectionPoolFactory(thriftPoolConfig);
        logger.info("pool is initialized");
    }

    /**
     * 为了找到aue auf的值，我们需要首先对原始的byteBuffer进行反序列化，然后判断该条信息是否是type=1的日志 如果是，则从当前的日志中找到aue auf信息
     * @param byteBuffer
     * @return
     * @throws Exception
     */
    private String findAueAuf(ByteBuffer byteBuffer,String sid)throws Exception {
        GenericDatumReader<GenericRecord> genericDatumReader = new GenericDatumReader<GenericRecord>(dataSchema);
        byte[] bytes = byteBuffer.array();
        if (bytes != null) {
        //    System.out.println("************************Not empty****************************");
            BinaryDecoder binaryDecoder = DecoderFactory.get().binaryDecoder(bytes, null);
            GenericRecord genericRecord = genericDatumReader.read(null, binaryDecoder);
            Object svcLogsObj = genericRecord.get("svcLogs");
            GenericArray<GenericRecord> svcLogs = (GenericArray<GenericRecord>) svcLogsObj;
            for (GenericRecord svcLog : svcLogs) {
                if ((Integer) svcLog.get("type") == 1) {
               //     System.out.println("svcLog is:");
               //     System.out.println(svcLog);
                    GenericArray<GenericRecord> logs = (GenericArray<GenericRecord>) svcLog.get("logs");
                    GenericRecord log = logs.get(0);
                    Map map1 = parseObject(log.toString(),Map.class);
                    Map map2 = parseObject(map1.get("extras").toString(),Map.class);
                    String params = map2.get("params").toString();
                    logger.info("In HDFSClient findAueAuf method, params is: " + params);
                    return parseAueAuf(params,sid);
                }
            }
            return "null";
        } else {
            throw new Exception("In HDFSClient findAueAuf method, byte[] bytes is null");
        }


    }

    /**
     * 解析params，从中获取出aue auf的值
     * params一般为以下字符串：
     * lang=sms-vip,acous=sms-vip,rate=16k,appid=100IME,syncid=0,aue=speex-wb,auf=audio/L16;rate=16000,eos=3000,arm_mode=true,irp=1,apr=1,isvip=1,uid=v570958891,sid=iat397eb5be@nc15e56ca9a620015270,rst=json
     * 返回的字符串为： speex-wb&&audio/L16;rate=16000
     * @param params
     * @return
     */
    private String parseAueAuf(String params,String sid){
     //   String s="ME,syncid=1,aue=speex-wb,auf=audio/L16;rate=16000,eos=16000,ar";
        int startPos =0;
        int endPos=0;
        if (sid.startsWith("ist")){
            startPos = params.indexOf("aue=");
            endPos=params.indexOf(",svad");
        }
        else {
            startPos = params.indexOf("aue=");
            endPos=params.indexOf(",eos");
        }
        System.out.println("startPos is:"+startPos+"   endPos is: "+endPos);

        String aueAuf = params.substring(startPos,endPos);  //aueAuf=  aue=speex-wb,auf=audio/L16;rate=16000
        logger.info("params.substring(startPos,endPos) is: "+aueAuf);

        String[] elem = aueAuf.split(","); // elem[0]= aue=speex-wb  elem[1]= auf=audio/L16;rate=16000
        String[] elem1 = elem[0].split("="); //elem1[0]= aue  elem1[1]= speex-wb
        String[] elem2 = elem[1].split("=");//elem2[0]= auf  elem2[1]= audio/L16;rate elem2[2]=16000

        String aue = elem1[1];
        String auf = elem2[1]+"="+elem2[2];
  //      System.out.println("In parseAueAuf method: "+aue+"&&"+auf);
        return aue+"&&"+auf; // speex-wb&&audio/L16;rate=16000
    }

//这个不用改

    /**
     * 这个方法直接有HDFSService调用 返回日志的data部分和aue auf 信息
     * @param sid
     * @return
     * @throws Exception
     */
    public String getData(String sid) throws Exception {
        ByteBuffer byteBuffer = getByteBufferData(sid);
        String aueAuf = findAueAuf(byteBuffer,sid);
        System.out.println("sid is:"+sid+"auesuf is:"+aueAuf);
/*        System.out.println("Print info:");
        System.out.println(decodeByteBuffer(byteBuffer,dataSchema)+"AUEAUF:"+aueAuf);*/
        return decodeByteBuffer(byteBuffer,dataSchema,sid)+"AUEAUF:"+aueAuf;
    }

    /**
     * 和上面的代码一样 这个代码提供给ist类型服务
     * @param sid
     * @return
     * @throws Exception
     */
    public String getData_ise(String sid) throws Exception {
        ByteBuffer byteBuffer = getByteBufferData(sid);
        return decodeByteBuffer(byteBuffer,dataSchema,sid);
    }
    //ist类型 svclogs太大 仅仅返回extrasInfo
    public String getData_ist(String sid) throws Exception {
        ByteBuffer byteBuffer = getByteBufferData(sid);
        return decodeByteBuffer(byteBuffer,dataSchema,sid);
    }

    /**
     * 这个方法直接由HDFSService调用，返回格式是获取到的media 的 byte[] bytes
     * 返回byte[] bytes是因为在HDFSService中要对bytes进行转存为WAV格式 而那个转存的方法需要的是byte[] 的media信息。
     * @param sid
     * @return
     * @throws Exception
     */
    public byte[] getMedia(String sid) throws Exception {
        logger.info("Get media...");
        TTransport tTransport = pool.getConnection();
        try {
            TProtocol protocol = new TBinaryProtocol(tTransport);
            SextantService.Client client = new SextantService.Client(protocol);
            System.out.println("client.getMedia(sid).array().length is: "+client.getMedia(sid).array().length);
            return AvroSerializerUtil.getSessionMedia(client.getMedia(sid).array()).mediaData.data;
        } finally {
            logger.info("closeTTransport!");
            closeTTransport(tTransport);
        }
    }


    private ByteBuffer getByteBufferData(String sid) throws Exception {
        logger.info("Get data...");
        TTransport tTransport = pool.getConnection();
        try {
            TProtocol protocol = new TBinaryProtocol(tTransport);
            SextantService.Client client = new SextantService.Client(protocol);
            return client.getData(sid);
        } finally {
            logger.info("closeTTransport!");
            closeTTransport(tTransport);
        }
    }






    private void closeTTransport(TTransport tTransport) {
        try {
            if (tTransport!=null)
                pool.releaseConnection(tTransport);
        }catch (Exception e){
            logger.warn("hdfs pool releaseConnection exception",e);
        }
        logger.info("hdfs pool releaseConnection closed.");
    }


    /**
     * The data and media on hdfs are serialized data. Note:The data and media on Hbase are serialized and compressed data.
     * This method is used to deserialize the data and media on hdfs.
     * @param byteBuffer The byteBuffer is serialized using avro technique,so we need schema to deserialized it.
     * @param schema The schema is used to deserialized the avro bytebuffer
     * @return For log data, we will get the data we know;for media,we can continue decoding the output to get wav file.
     * @throws IOException
     */
    /**
     * 这个方法主要是反序列化bytebuffer 将其反序列化为字符串
     * @param byteBuffer
     * @param schema
     * @return
     * @throws Exception
     */
    private  String decodeByteBuffer(ByteBuffer byteBuffer, Schema schema,String sid)throws Exception {
        Map<String,String > extrasMap = new HashMap<String,String>();
        GenericDatumReader<GenericRecord> genericDatumReader = new  GenericDatumReader<GenericRecord>(schema);
        byte[]  bytes = byteBuffer.array();
        System.out.println("bytes.length: "+bytes.length);
        if(bytes!=null&&bytes.length!=0){
            BinaryDecoder binaryDecoder = DecoderFactory.get().binaryDecoder(bytes,null);
            GenericRecord genericRecord = genericDatumReader.read(null,binaryDecoder);
         //   genericRecord.get("svcLogs");
            JSONArray jsonArray = JSONArray.parseArray(genericRecord.get("svcLogs").toString());
      //   System.out.println("genericRecord.toString():"+genericRecord.toString());
            System.out.println("HDFS 上查找的svcLogs的数量是： "+jsonArray.size());

            for (int i=0;i<jsonArray.size();i++){
                Map map1 = parseObject(jsonArray.get(i).toString(),Map.class);
                JSONArray jsonArray1 = JSONArray.parseArray(map1.get("logs").toString());
                for (int j=0;j<jsonArray1.size();j++){
                    Map map2 = parseObject(jsonArray1.get(j).toString(),Map.class);
                    Map map3 = parseObject(map2.get("extras").toString(),Map.class);
                    //向extrasMap中添加params中解析的一些字段
                    if(map3.containsKey("params")&&!map3.get("params").equals("")){
                       /* System.out.println(map3.get("params"));*/
                        String[] elem = ((String)map3.get("params")).split(",");
                        for (int k=0;k<elem.length;k++){
                        //    System.out.println(elem[k]);
                            if (elem[k].contains("=")){
                                int pos = elem[k].indexOf("=");
                                String key = elem[k].substring(0,pos);
                                String value = elem[k].substring(pos+1,elem[k].length());
                                if (extras.contains(key))
                                    extrasMap.put(key,value);
                            }
                        }
                    }
                    //向extrasMap中添加finalResult中解析的recs字段
                    if (map3.containsKey("finalResult")&&!map3.get("finalResult").equals("")){
                        String finalResult = map3.get("finalResult").toString();
                        /*System.out.println("finalResult is: "+ finalResult);*/
                        //添加一个逻辑处理finalresult中全部是汉字的情况
                        String words="";
                        if(finalResult.startsWith("[{")){  //匹配绝大多数情况 汉字作为json存在finalresults中
                            if (sid.startsWith("ist")||sid.startsWith("ise")){
                                words = getRecs_ist(finalResult);
                            }
                            else
                                words = getRecs(finalResult);
                        }
                        else {  //有些sid 比如ist81737cd2@nc15f27ed0c0b0010450 finalresults直接是汉字
                            if (sid.startsWith("ise"))
                                words="";
                            else
                                words=finalResult;
                           /* System.out.println("words is:"+finalResult);*/
                        }

                        extrasMap.put("recs",words);
                    }

                    //向extrasMap中添加timestamp字段
                    if (map3.containsKey("timestamp")&&!map3.get("timestamp").equals(""))
                        extrasMap.put("timestamp",map3.get("timestamp").toString());
                    //向extrasMap中添加client_ip信息
                    if (map3.containsKey("client_ip")&&!map3.get("client_ip").equals(""))
                        extrasMap.put("client_ip",map3.get("client_ip").toString());
                    //向extrasMap中添加client_ip信息
                    if (map3.containsKey("ret")&&!map3.get("ret").equals(""))
                        extrasMap.put("ret",map3.get("ret").toString());
                    //向extrasMap中添加client_ip信息
                    if (map3.containsKey("sub")&&!map3.get("sub").equals(""))
                        extrasMap.put("sub",map3.get("sub").toString());

                }

            }
            HBaseResult hBaseResult = new HBaseResult();
            hBaseResult.setSid(sid);
            //eatrasMap中添加sis_ip信息
            extrasMap.put("sis_ip",getSisIp(sid));
            hBaseResult.setExtrasInfo(extrasMap);
            JSONArray jsonArray1 = JSONArray.parseArray(genericRecord.get("svcLogs").toString());
            String[] svcLogs = new String[jsonArray1.size()];
            for (int i=0;i<jsonArray1.size();i++){
                /*svcLogs[i]=jsonArray1.get(i).toString();*/
                Map map = JSONObject.parseObject(jsonArray1.get(i).toString(),Map.class);
                svcLogs[i]="{\"type\": "+map.get("type").toString()+","+
                        "\"sid\": \""+map.get("sid").toString()+"\","+
                        "\"uid\": \""+map.get("uid").toString()+"\","+
                        "\"syncid\": "+map.get("syncid").toString()+","+
                        "\"timestamp\": "+map.get("timestamp").toString()+","+
                        "\"ip\": "+map.get("ip").toString()+","+
                        "\"callName\": \""+map.get("callName").toString()+"\","+
                        "\"logs\": "+map.get("logs").toString()+"}";
                /*System.out.println(svcLogs[i]);*/
            }
            if (!sid.startsWith("ist"))
                hBaseResult.setSvcLogs(svcLogs);

/*            if (sid.startsWith("ist")){
                Map<String,String> map = hBaseResult.getExtrasInfo();
                Iterator<Map.Entry<String,String>> iter = map.entrySet().iterator();
                String extrasInfo="";
                while (iter.hasNext()){
                    Map.Entry entry = iter.next();
                    extrasInfo+=entry.getKey()+",,,"+entry.getValue()+";;;";
                }
                return extrasInfo;
            }*/


            return hBaseResult.toString(sid);

        }
        else {
            logger.warn("decompress failed,it return null byte[]");
            throw new Exception("decompress failed,it return null byte[]");
        }
    }


    private String getRecs(String finalResult){
        /*System.out.println(finalResult);*/
        String str1 = finalResult.replace("\\\"","\"");
        /*System.out.println("str1 is:"+str1);*/
        String words="";
        JSONArray jsonArray = JSONArray.parseArray(str1);
        for (int i=0;i<jsonArray.size();i++){
            /*System.out.println(jsonArray.get(i));*/
            Map map1 = JSON.parseObject(jsonArray.get(i).toString(), Map.class);
            JSONArray jsonArray1 = JSONArray.parseArray(map1.get("ws").toString());
            for (int j=0;j<jsonArray1.size();j++){
                Map map2 = JSON.parseObject(jsonArray1.get(j).toString(), Map.class);

                JSONArray jsonArray2 = JSONArray.parseArray(map2.get("cw").toString());
                Map map3 = JSON.parseObject( jsonArray2.get(0).toString(), Map.class);
               /* System.out.println(map3.get("w"));*/
                words+=map3.get("w");
                /*System.out.println(words);*/
            }
        }
        System.out.println("words is: "+words);
        return words;
    }
    private String getRecs_ist(String finalResult){
        /*System.out.println("finalResult is: "+finalResult);*/
        String words="";
        JSONArray jsonArray = JSONArray.parseArray(finalResult);
        for (int i=0;i<jsonArray.size();i++) {
         //   System.out.println("jsonArray.get("+i+") is: "+jsonArray.get(i));
            Map map1 = JSON.parseObject(jsonArray.get(i).toString(), Map.class);
            Map map1_1 = JSON.parseObject(map1.get("cn").toString(), Map.class);
            Map map2 = JSON.parseObject(map1_1.get("st").toString(), Map.class);
            JSONArray jsonArray1 = JSONArray.parseArray(map2.get("rt").toString());
            Map map3 = JSON.parseObject( jsonArray1.get(0).toString(), Map.class);
            JSONArray jsonArray2 = JSONArray.parseArray(map3.get("ws").toString());
            for (int j=0;j<jsonArray2.size();j++){
                Map map4 = JSON.parseObject(jsonArray2.get(j).toString(), Map.class);
                JSONArray jsonArray3 = JSONArray.parseArray(map4.get("cw").toString());
                Map map5 = JSON.parseObject( jsonArray3.get(0).toString(), Map.class);
           //     System.out.println("map5.get(w) is: "+map5.get("w"));
                words+=map5.get("w");
           //     System.out.println("words is: "+words);
            }
        }
        System.out.println("words is: "+words);
        return words;
    }
    private String getSisIp(String sid){
        return  "."+Integer.parseInt(sid.substring(25,27),16)+"."+Integer.parseInt(sid.substring(27,29),16);
    }
}
