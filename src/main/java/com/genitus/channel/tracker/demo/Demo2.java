package com.genitus.channel.tracker.demo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 这段代码主要是测试为什么在nc集群上可以搜索到sc的sid
 * 知道原因 采用过滤sc的方式来进行解决
 */
public class Demo2 {
    public static void main(String[] args){
        String sid ="iat47b82713@sc15f01de3eeb8010110";
        String pattern ="[A-Za-z0-9]+@sc[A-Za-z0-9]+";
        Pattern r = Pattern.compile(pattern);
        Matcher matcher = r.matcher(sid);
        System.out.println(matcher.matches());

    }


}
