package com.genitus.channel.tracker.demo;

import com.genitus.channel.tracker.util.audio.RemoteService;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;

//转换音频 speex转wav
public class Demo4 {
    public static void main(String[] args) throws IOException  {
        new Demo4().transform("/home/jwzheng/genitus-tracker_test/iat5735d38c@lc15f481c21cf340a350.speex");

    }



    //第二种获取文件内容方式
    public byte[] transform( String speexFile) throws IOException {
        FileInputStream in=new FileInputStream(speexFile);

        ByteArrayOutputStream out=new ByteArrayOutputStream(1024);

        System.out.println("bytes available:"+in.available());

        byte[] temp=new byte[1024];

        int size=0;

        while((size=in.read(temp))!=-1)
        {
            out.write(temp,0,size);
        }

        in.close();

        byte[] bytes=out.toByteArray();
        System.out.println("bytes size got is:"+bytes.length);
        new RemoteService().savaAudioFile(bytes,"speex-wb","audio/L16;rate=16000", "iat5735d38c@lc15f481c21cf340a350");

        return bytes;
    }

}
