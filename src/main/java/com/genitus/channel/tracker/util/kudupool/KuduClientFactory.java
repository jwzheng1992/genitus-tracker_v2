package com.genitus.channel.tracker.util.kudupool;

import com.genitus.channel.tracker.client.KuduClient;
import org.apache.commons.pool.BasePoolableObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KuduClientFactory  extends BasePoolableObjectFactory<KuduClient> {
    private String url;
    private static Logger logger = LoggerFactory.getLogger(KuduClientFactory.class);

    public KuduClientFactory(String url )throws Exception{
        logger.info("url is: "+url);
        this.url=url;
    }


    /**
     * 这个方法是用来创建一个对象，当在GenericObjectPool类中调用borrowObject方法时，如果当前对象池中没有空闲的对象，GenericObjectPool会调用这个方法，创建一个对象，
     * 并把这个对象封装到PooledObject类中，并交给对象池管理。
     * @return
     * @throws Exception
     */
    public  KuduClient makeObject() throws Exception{
        System.out.println("被调用了...");
        return new KuduClient(url);
    }



}
