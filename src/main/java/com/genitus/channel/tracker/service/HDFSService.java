package com.genitus.channel.tracker.service;

import com.genitus.channel.tracker.client.HDFSClient;
import com.genitus.channel.tracker.model.parameter.MyProperties;
import com.genitus.channel.tracker.util.audio.AvroSerializerUtil;
import com.genitus.channel.tracker.util.audio.RemoteService;
import com.google.common.util.concurrent.AbstractIdleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;


public class HDFSService extends AbstractIdleService {
    private static Logger logger = LoggerFactory.getLogger(HDFSService.class);
    private String downloadFileDirectory= MyProperties.DownloadDirectory;
    private Map<String, HDFSClient> hdfsClientMap;

    @Inject
    public HDFSService(Map<String, HDFSClient> hdfsClientMap){
        logger.info("hdfs Service 初始化");
        this.hdfsClientMap=hdfsClientMap;
    }

    protected void startUp() throws Exception {

    }

    protected void shutDown() throws Exception {

    }




    public HashMap<String,String> getLog(String sid) throws Exception{
        String city = getCity(sid);
        HDFSClient hdfsClient = hdfsClientMap.get(city);
        //添加一个处理逻辑，判断sid是否是ist类型，因为ist 日志只有文本日志 没有音频。
        if (sid.startsWith("ist")){
            String dataLog = hdfsClient.getData_ist(sid);
            System.out.println("dataLog:"+dataLog);
            HashMap<String,String> map = new HashMap<String,String>();
            map.put("data",dataLog);
            return map;
        }
        if (sid.startsWith("ise")){
            String dataLog = hdfsClient.getData_ise(sid);
            HashMap<String,String> map = new HashMap<String,String>();
            map.put("data",dataLog);
            return map;
        }
        //对于其他类型的sid，比如iat，按照预先定义逻辑处理。

        String dataLog = hdfsClient.getData(sid);
        //需要从dataLog中解析出aueauf
        String[] elem = dataLog.split("AUEAUF:");
        if (!elem[1].equals("null")){
            String aueauf = elem[1];
            String[] elem1 = aueauf.split("&&");
            String aue = elem1[0];
            String auf=elem1[1];
            logger.info("In HDFSService getLog method, aue is: "+aue+", auf is: "+auf);
            try {
                byte[] bytes = hdfsClient.getMedia(sid);
                new RemoteService().savaAudioFile(bytes,aue,auf, sid);
            }catch (Exception e){
                logger.error("SavaAudioFile exception",e);
            }finally {
                String data = elem[0];
                HashMap<String,String> map = new HashMap<String,String>();
                map.put("data",data);
                return map;
            }
        }
        else {
            String data = elem[0];
            HashMap<String,String> map = new HashMap<String,String>();
            map.put("data",data);
            return map;
        }


    }



    public HashMap<String,String> saveAudioAndLog(String sid) throws Exception{
        String city = getCity(sid);
        HDFSClient hdfsClient = hdfsClientMap.get(city);
        String dataLog = hdfsClient.getData(sid);
        //需要从dataLog中解析出aueauf
        String[] elem = dataLog.split("AUEAUF:");
        String aueauf = elem[1];
        String[] elem1 = aueauf.split("&&");
        String aue = elem1[0];
        String auf=elem1[1];
        logger.info("In HDFSService getLog method, aue is: "+aue+", auf is: "+auf);
        try {
            byte[] bytes = hdfsClient.getMedia(sid);
            new RemoteService().savaAudioFile(bytes,aue,auf, sid);
        }catch (Exception e){
            logger.error("SavaAudioFile exception",e);
        }finally {
            String data = elem[0];
            HashMap<String,String> map = new HashMap<String,String>();
            map.put("data",data);
            //在本地存储log文件
            String directory = downloadFileDirectory+sid;
            File fileDir = new File(directory);
            if (!fileDir.exists()) {
                fileDir.mkdirs();
            }
            FileWriter fw = new FileWriter(downloadFileDirectory+sid+"/"+sid+".log");
            fw.write(data,0,data.length());
            fw.flush();
            fw.close();
            return map;
        }
    }



    /**
     * parse sid to get city(nc or sc)
     * @param sid
     * @return
     */
    private String getCity(String sid){
        return sid.substring(12,14);
    }

}
