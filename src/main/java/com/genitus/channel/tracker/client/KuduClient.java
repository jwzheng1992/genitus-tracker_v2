package com.genitus.channel.tracker.client;

//import com.genitus.channel.tracker.service.KuduService;
import com.genitus.channel.tracker.util.kudupool.KuduConnectionPoolFactory;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class KuduClient {
    private static Logger logger = LoggerFactory.getLogger(KuduClient.class);
/*    static {
        try {
            Class.forName("com.cloudera.impala.jdbc41.Driver");
        } catch (ClassNotFoundException e) {
            logger.error("com.cloudera.impala.jdbc41.Driver noe exist",e);
            throw new RuntimeException();
        }
    }*/
    private KuduConnectionPoolFactory kuduConnectionPoolFactory;

    /**
     * Constructor method.
     * @param url  这是c3p0-config.xml文件中的一项的数据库名称，可以配置该数据库下的信息
     */
    public KuduClient(String url) throws SQLException{
        kuduConnectionPoolFactory = new KuduConnectionPoolFactory(url);
    /*    try {
            connection = DriverManager.getConnection(kuduAddress);
        }catch (SQLException e){
            throw new SQLException("SQLException");
        }*/
    }

    /**
     * Get data from kudu database using sid.
     * @param sid
     * @return
     * @throws SQLException
     */
    public String getLog(String sid) throws Exception{
        //String sql = "select rawlog from session.client_session where day = '2017-09-04' and sid = "+sid;  //select rawlog from session.client_session where day = '2017-08-24' and sid = 'iated28b0bd@sc15e125c268e8010210'
        Date date = new Date(toTimestamp(sid));
        String day = new SimpleDateFormat("yyyy-MM-dd").format(date);
        String sql = "select rawlog from session.client_session where day = '"+day+"' and sid ='"+sid+"'";
        logger.info("sql is:"+sql);
        return getResult(sql);
    }

    /**
     * Get data from Kudu database using sql
     * This method is called by the above method:getLog(String sid)
     * @param sql
     * @return
     * @throws SQLException
     */
    private String getResult(String sql)throws Exception{
        String json=null;
        Connection connection = kuduConnectionPoolFactory.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ResultSet resultSet = preparedStatement.executeQuery();
        if ( resultSet.next())
            json = (String)resultSet.getObject(1);
        else
            logger.warn("Can not get client log by this sid. The sql sentence is: "+sql);
        kuduConnectionPoolFactory.releaseConnection(connection);
        return json;
    }




    // test method
    public static void main(String[] args)throws Exception{



/*        try {
          //  KuduClient kuduClient = new KuduClient("jdbc:impala://172.26.5.11:21050/session");
            KuduClient kuduClient = new KuduClient("172.26.5.11:21050");
            long start = System.currentTimeMillis();
            System.out.println(kuduClient.getLog("iat27a8b8a8@sc15dcbb153fd8410480"));
            System.out.println(System.currentTimeMillis()-start);
            System.out.println(kuduClient.getLog("iat27a8b8a8@sc15dcbb153fd8410480"));
            System.out.println(System.currentTimeMillis()-start);
            System.out.println(kuduClient.getLog("iat27a8b8a8@sc15dcbb153fd8410480"));
            System.out.println(System.currentTimeMillis()-start);
            System.out.println(kuduClient.getLog("iat27a8b8a8@sc15dcbb153fd8410480"));
            System.out.println(System.currentTimeMillis()-start);
            //   System.out.println(result);
        }catch (SQLException e){
            //log....
            logger.error("",e);

        }*/
    }


    /**
     * Parse sid to get timestampe in mills
     *
     * @param sid sid.
     * @return timestampe in mills.
     */
    private long toTimestamp(String sid) {
        if (null == sid || sid.length() != 32) {
            return -1;
        }
        return Long.parseLong(sid.substring(14, 25), 16);
    }


}
