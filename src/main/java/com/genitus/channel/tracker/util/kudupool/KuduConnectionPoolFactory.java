package com.genitus.channel.tracker.util.kudupool;
import com.genitus.channel.tracker.client.KuduClient;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class KuduConnectionPoolFactory {
    private GenericObjectPool<Connection> pool;
    private static Logger logger = LoggerFactory.getLogger(KuduConnectionPoolFactory.class);
    public KuduConnectionPoolFactory(String url ){
        GenericObjectPool.Config conf = new GenericObjectPool.Config();
        KuduConnectionFactory kuduConnectionFactory = new KuduConnectionFactory(url);
        pool = new GenericObjectPool<Connection>(kuduConnectionFactory, conf);
    }

    public Connection getConnection() throws Exception {
        return pool.borrowObject();
    }

    public void releaseConnection(Connection connection) throws Exception {
        logger.info("Return Object...");
        pool.returnObject(connection);
    }
/*    public void init(){
        pool=new GenericObjectPool();
    }*/

/*    public static void main(String[] args)throws Exception{
        String url="172.26.5.11:21050";
        KuduConnectionPoolFactory kuduConnectionPoolFactory = new KuduConnectionPoolFactory(url);

        for (int i=0;i<100;i++){

        }

        Connection connection = kuduConnectionPoolFactory.getConnection();
        System.out.println(connection);
        kuduConnectionPoolFactory.releaseConnection(connection);
    //    String log = kuduClient.getLog("iat16ba401b@sc15e92d369e780103f0");
    //    System.out.println(log);
        connection = kuduConnectionPoolFactory.getConnection();
        System.out.println(connection);
    }*/
}

/*
class KuduService{
    private KuduConnectionPoolFactory kuduConnectionPoolFactory;
    public KuduService(String url) throws Exception{
        kuduConnectionPoolFactory = new KuduConnectionPoolFactory(url);
    }

    public KuduConnectionPoolFactory getKuduConnectionPoolFactory() {
        return kuduConnectionPoolFactory;
    }

    public void releaseConnection(){
       // if ()

    }


    */
/**
     * get client log from kudu
     * @param sid
     * @return if the city is beijing,then return client log;
     *          ifthe city is guangzhou,return empty string;
     * @throws SQLException
     *//*

    public String getClientLog(String sid)throws Exception{
        KuduClient kuduClient = kuduConnectionPoolFactory.getConnection();
        return kuduClient.getLog(sid);
    }
    public static void main(String[] args)throws Exception{
        KuduService kuduService = new KuduService("172.26.5.11:21050");
        System.out.println("here1");
        KuduConnectionPoolFactory kuduConnectionPoolFactory =kuduService.getKuduConnectionPoolFactory();
        KuduClient kuduClient = kuduConnectionPoolFactory.getConnection();
        System.out.println(kuduClient.toString());
        kuduConnectionPoolFactory.releaseConnection(kuduClient);
        System.out.println("here1");
        kuduClient = kuduConnectionPoolFactory.getConnection();
        System.out.println(kuduClient.toString());
*/
/*        System.out.println(kuduService.toString());
        String log =kuduService.getClientLog(args[0]);
        System.out.println(log);*//*


    }
}
*/
