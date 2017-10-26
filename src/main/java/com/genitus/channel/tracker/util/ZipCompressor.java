package com.genitus.channel.tracker.util;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Zip;
import org.apache.tools.ant.types.FileSet;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ZipCompressor {
    private File zipFile;

    public ZipCompressor(String pathName) {
        zipFile = new File(pathName);
    }

    public void compress(String srcPathName) {
        File srcdir = new File(srcPathName);
        if (!srcdir.exists())
            throw new RuntimeException(srcPathName + "不存在！");

        Project prj = new Project();
        Zip zip = new Zip();
        zip.setProject(prj);
        zip.setDestFile(zipFile);
        FileSet fileSet = new FileSet();
        fileSet.setProject(prj);
        fileSet.setDir(srcdir);
        zip.addFileset(fileSet);

        zip.execute();
    }

    public void compress(List<String> srcPathNames) {
        Project prj = new Project();
        Zip zip = new Zip();
        zip.setProject(prj);
        zip.setDestFile(zipFile);
        for (String srcPathName : srcPathNames) {
            System.out.println(srcPathName);
            File srcdir = new File(srcPathName);
            FileSet fileSet = new FileSet();
            fileSet.setProject(prj);
            fileSet.setDir(srcdir);
            zip.addFileset(fileSet);
        }

        zip.execute();
    }

    public static void main(String[] args) {
/*      //  List<String> srcList = new ArrayList<String>();
       *//* srcList.add("D:\\temp\\Log");
        srcList.add("D:\\temp\\logs");
        srcList.add("D:\\temp\\sync");*//*
   //     srcList.add("D:\\temp1");
    //    srcList.add("D:\\temp2");
    //    ZipCompressor zca = new ZipCompressor("D:\\temp2.zip");
     //   zca.compress(srcList);*/

      //ZipCompressor.compressDirectory("D:\\download\\","iat175672ba%40sc15e33ded4a88010330");
        String sidStr = "(asdda,asdads,qweqwe,qwee)";
        String sid = sidStr.substring(1,sidStr.length()-1);
        System.out.println(sid);


    }




    public static File compressDirectory(String directory,String sid){
        String zipFullName= directory+sid+"\\"+sid+".zip";
        List<String> srcList = new ArrayList<String>();

        String audioAndLogDirectory = directory+sid;
        srcList.add(audioAndLogDirectory);
        ZipCompressor zca = new ZipCompressor(zipFullName);
        zca.compress(srcList);
        return new File(zipFullName);
    }
}
