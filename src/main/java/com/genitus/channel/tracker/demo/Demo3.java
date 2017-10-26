package com.genitus.channel.tracker.demo;

import java.io.File;

public class Demo3 {
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

    public void run() {
        File file = new File("d://download");
        delteFile(file);
         file = new File("d://download");
        file.mkdir();
    }

    public static void  main(String[] args){
/*        String sid = "iat3a0fc74d@nc15f0e6b304f0011050";
            String prefix = sid.substring(25,27);
            System.out.println( prefix+":"+Integer.parseInt(prefix,16));
            String suffix = sid.substring(27,29);
            System.out.println( suffix+":"+Integer.parseInt(suffix,16));
            return "."+prefix+"."+suffix;*/
        String[] elem = new String[9];
        for (int i=0;i<3;i++)
            elem[i]="a";
        for (int i=0;i<9;i++){
            if (elem[i]==null)
                System.out.println("null object");
        }


    }
}
