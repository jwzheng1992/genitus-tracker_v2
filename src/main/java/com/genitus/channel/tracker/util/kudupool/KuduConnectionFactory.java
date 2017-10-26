package com.genitus.channel.tracker.util.kudupool;

import org.apache.commons.pool.BasePoolableObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;

public class KuduConnectionFactory extends BasePoolableObjectFactory<Connection> {
    private static Logger logger = LoggerFactory.getLogger(KuduConnectionFactory.class);
/*    static {
        try {
            Class.forName("com.cloudera.impala.jdbc41.Driver");
        } catch (ClassNotFoundException e) {
            logger.error("com.cloudera.impala.jdbc41.Driver noe exist",e);
            throw new RuntimeException();
        }
    }*/
    private String url;
    /**
     * 这个方法是用来创建一个对象，当在GenericObjectPool类中调用borrowObject方法时，如果当前对象池中没有空闲的对象，GenericObjectPool会调用这个方法，创建一个对象，
     * 并把这个对象封装到PooledObject类中，并交给对象池管理。
     * @return
     * @throws Exception
     */
    public KuduConnectionFactory(String url){
        this.url=url;
    }

    public Connection makeObject() throws Exception{
        System.out.println("被调用了...");
        String kuduAddress="jdbc:impala://"+url+"/session";
        Connection connection = DriverManager.getConnection(kuduAddress);
        return connection;
    }
}
