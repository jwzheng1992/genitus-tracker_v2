package com.genitus.channel.tracker.router;

import com.codahale.metrics.annotation.Timed;
import com.genitus.channel.tracker.model.parameter.MyProperties;
import com.genitus.channel.tracker.service.ESService;
import com.genitus.channel.tracker.service.HBaseService;
import com.genitus.channel.tracker.service.HDFSService;
import com.genitus.channel.tracker.service.KuduService;
import com.genitus.channel.tracker.util.ZipCompressor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.*;

@Path("/tracker")
@Produces(MediaType.APPLICATION_JSON)
public class RestApiRouter {
    private static Logger logger = LoggerFactory.getLogger(RestApiRouter.class);

    private KuduService kuduService;
    private HBaseService hBaseService;
    private HDFSService hdfsService;
    private ESService esService;
    private String downloadFileDirectory= MyProperties.DownloadDirectory;


    @Inject
    public RestApiRouter(KuduService kuduService,HBaseService hBaseService,HDFSService hdfsService,ESService esService){
        this.esService = esService;
        this.hdfsService = hdfsService;
        this.hBaseService = hBaseService;
        this.kuduService = kuduService;
    }

    @Path("search/clientlog/{sid}") @GET @Timed
    public Response geClienttLogBySid(@PathParam("sid") String sid) throws Exception{
        try {
            String response = kuduService.getClientLog(sid);
            System.out.println("Client log is:");
            System.out.println(response);
            if (response!=null)
                return Response.ok(response).build();
            else
                return Response.ok("Can find this log by this sid: "+sid).build();
        }catch (SQLException e){
            logger.error("Get client log error by sid: "+sid,e);
            return Response.status(500).build();
        }
    }

    @Path("search/serverlog/{sid}") @GET @Timed
    public Response getServerLogBySid(@PathParam("sid") String sid) throws Exception  {
        long timestart = System.currentTimeMillis();
        long time1 = RestApiRouter.toTimestamp(sid);
        long time2 = System.currentTimeMillis();
        long millSeconds = time2-time1;
        long hours = millSeconds/1000/60/60;
        logger.info("hours are: "+hours);
        HashMap<String,String> map = new HashMap<>();
        if (hours<2){
            // first hbase ,then hdfs
            map = hBaseService.getLog(sid);
            if (map!=null){
                logger.info("It is on hbase...");
                /*System.out.println("Server log is:");
                System.out.println(map.get("data"));*/
                System.out.println("old method, we cost tatal time :"+(System.currentTimeMillis()-timestart));
                return Response.ok(map.get("data")).header("Content-Encoding", "GBK").build();
                //       return  Response.ok(map.get("data")).build();
            }
            logger.info("It is not save on hbase, we will search on hdfs...");
            map =  hdfsService.getLog(sid);
            /*System.out.println("Server log is:");
            System.out.println(map.get("data"));*/
            return Response.ok(map.get("data")).build();
        }
        else {
            //hdfs
            logger.info("It is on hdfs...");
            map =  hdfsService.getLog(sid);
            /*System.out.println("Server log is:");
            System.out.println(map.get("data"));*/
            return Response.ok(map.get("data")).build();
        }

    }

    @Path("search/{json}") @GET @Timed
    public Response getSidFromESCluster(@PathParam("json") String json)  {
        //delete audio download directory
        delteFile(new File(downloadFileDirectory));
        File file = new File(downloadFileDirectory);
        file.mkdir();
        try {
            String json1 = json.replace(")","}");
            String json2 = json1.replace("(","{");
            logger.info(json2);
            String response = esService.search(json2);
            /*System.out.println("ES result is:");
            System.out.println(response);*/
            return Response.ok(response).build();
        }catch (Exception e){
            logger.error("Get sid error",e);
            return Response.status(500).build();
        }
    }

    /**
     * 这个方法只下载指定sid对应的音频文件 不下载log文件 而且调用该方法的时候 wav文件是存在的
     * @param sid
     * @return
     */
    @GET
    @Path("download/audio/{sid}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadAudio(@PathParam("sid") String sid) throws Exception {
        String directory = downloadFileDirectory+sid+"/";
        String audioFileName = directory+sid+".wav";
        logger.info("Audio file path and file name is: "+audioFileName);
        File file = new File(audioFileName);
        if (file.exists()){
            System.out.println("audio file exists......");
            String fileName = URLEncoder.encode(file.getName(), "UTF-8");
            return Response.ok(file).header("Content-disposition", "attachment;filename=" + fileName).header("Cache-Control", "no-cache").build();
        }
        else {
            return Response.status(500).build();
        }
    }


/*
    */
/**
     * 这个方法是根据sid去hbase hdfs找音频数据 并把音频数据下载下来
     * @param
     * @return
     * @throws Exception
     *//*

    @GET
    @Path("download/audio1/{sid}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadAudio1(@PathParam("sid") String sid) throws Exception {
        long time = System.currentTimeMillis();
        long time_end=0;

        long time1 = RestApiRouter.toTimestamp(sid);
        long time2 = System.currentTimeMillis();
        long millSeconds = time2-time1;
        long hours = millSeconds/1000/60/60;
        logger.info("hours are: "+hours);
        if (hours<2){
            // hbase
            hBaseService.getAudio(sid);
            String directory = downloadFileDirectory+sid+"/";
            String audioFileName = directory + sid + ".wav";
            File file = new File(audioFileName);
            if (file.exists()){
                logger.info("audio file exists on hbase......");
                String fileName = URLEncoder.encode(file.getName(), "UTF-8");
                return Response.ok(file).header("Content-disposition", "attachment;filename=" + fileName).header("Cache-Control", "no-cache").build();
            }
            else {
                //hdfs
                logger.info("audio file exists on hdfs......");
                hdfsService.getAudio(sid);
                directory = downloadFileDirectory+sid+"/";
                audioFileName = directory + sid + ".wav";
                file = new File(audioFileName);
                //download and return;
                System.out.println("audio file exists......");
                String fileName = URLEncoder.encode(file.getName(), "UTF-8");
                return Response.ok(file).header("Content-disposition", "attachment;filename=" + fileName).header("Cache-Control", "no-cache").build();
            }
        }
        else {
            // hdfs
            logger.info("audio file exists on hdfs......");
            hdfsService.getAudio(sid);
            String directory = downloadFileDirectory+sid+"/";
            String audioFileName = directory + sid + ".wav";
            File file = new File(audioFileName);
            //download and return;
            System.out.println("audio file exists......");
            String fileName = URLEncoder.encode(file.getName(), "UTF-8");
            return Response.ok(file).header("Content-disposition", "attachment;filename=" + fileName).header("Cache-Control", "no-cache").build();
        }
    }
*/


    @GET
    @Path("download/audioAndLog/{sid}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadAudioAndLog(@PathParam("sid") String sidStr) throws Exception {
       String sidStr1 = sidStr.substring(1,sidStr.length()-1);
       logger.info("SidList is:"+sidStr1);
       String[] sidList = sidStr1.split(",");
       for (String sid:sidList){
           if(sid.startsWith("iat")){  //iat的日志包含audio和log两部分，两部分都要下载
               String audioFilePath = downloadFileDirectory+sid+"/"+sid+".wav";
               String logFilePath = downloadFileDirectory+sid+"/"+sid+".log";
               hBaseService.saveAudioAndLog(sid);
               File audioFile = new File(audioFilePath);
               File logFile = new File(logFilePath);
               //只有当既没有audio 也没有log的时候 才去hdfs中找数据
               if (!audioFile.exists()&&!logFile.exists()){
                   hdfsService.saveAudioAndLog(sid);
               }
           }
           else { //ist ise auth uup 都只有文本日志下载的需求
               String logFilePath = downloadFileDirectory+sid+"/"+sid+".log";
               hBaseService.saveAudioAndLog(sid);
               File logFile = new File(logFilePath);
               //只有当既没有audio 也没有log的时候 才去hdfs中找数据
               if (!logFile.exists()){
                   hdfsService.saveAudioAndLog(sid);
               }
           }



       }
        File file = compressDirectory(downloadFileDirectory,sidList);
        if (file.exists()){
            System.out.println("audio file exists......");
            String fileName = URLEncoder.encode(file.getName(), "UTF-8");
            return Response.ok(file).header("Content-disposition", "attachment;filename=" + fileName).header("Cache-Control", "no-cache").build();
        }
        else {
            return Response.status(500).build();
        }
    }


    private File compressDirectory(String directory,String[] sidList){
        String zipFullName= directory+"zip"+"/"+sidList[0]+".zip";  //建立压缩文件的存储路径
        List<String> srcList = new ArrayList<String>();
        for (String sid:sidList){
            String audioAndLogDirectory = directory+sid;
            srcList.add(audioAndLogDirectory); // 添加要压缩的文件夹
        }
        ZipCompressor zca = new ZipCompressor(zipFullName);   //建立压缩的文件
        zca.compress(srcList);
        return new File(zipFullName);

    }
    /**
     * 压缩directory文件夹下的sid文件夹    directory= "/home/jwzheng/download/"  sid="iat175672ba%40sc15e33ded4a88010330"
     * 压缩该文件至/home/jwzheng/download/iat175672ba%40sc15e33ded4a88010330/iat175672ba%40sc15e33ded4a88010330.zip
     * @param directory 父文件夹
     * @param sid  子文件夹    压缩的是sid文件夹  保存压缩文件到sid文件夹下  和原来的.wav .log 同一个目录
     * @return
     */
    private File compressDirectory(String directory,String sid){
        String zipFullName= directory+sid+"/"+sid+".zip";  //建立压缩文件的存储路径
        List<String> srcList = new ArrayList<String>();
        String audioAndLogDirectory = directory+sid;
        srcList.add(audioAndLogDirectory); // 添加要压缩的文件夹
        ZipCompressor zca = new ZipCompressor(zipFullName);   //建立压缩的文件
        zca.compress(srcList);
        return new File(zipFullName);
    }
    private static long toTimestamp(String sid) {
        if (null == sid || sid.length() != 32) {
            return -1;
        }
        return Long.parseLong(sid.substring(14, 25), 16);
    }

    public static void main(String[] args){
        long time = RestApiRouter.toTimestamp("ath84cd35ee@nc15ece5dac9200152e0");
        System.out.println(time);
        Date date = new Date(time);
        System.out.println(date);
        long time1 = System.currentTimeMillis();
        long x = time1-time;
        System.out.println(x/1000/60/60/24);
    }
    //递归删除文件及文件夹
    public void delteFile(File file){
        File []filearray=    file.listFiles();
        if(filearray!=null){
            for(File f:filearray){
                System.out.println(f.getName());
                if(f.isDirectory()){
                    delteFile(f);
                }else{
                    f.delete();
                }
            }
            file.delete();
        }
    }
}
