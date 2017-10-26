package com.genitus.channel.tracker.module

import javax.inject.Named

import com.genitus.channel.tracker.client.HBaseClient
import com.genitus.channel.tracker.config.HBaseConf
import com.genitus.channel.tracker.service.HBaseService
import com.google.inject.{AbstractModule, Provides, Singleton}
import org.apache.avro.Schema
import org.slf4j.LoggerFactory
import scala.collection.JavaConverters._
import scala.collection.mutable

class HBaseModule(val hbaseConf: HBaseConf) extends AbstractModule{
  override def configure(): Unit = {bind(classOf[HBaseConf]).toInstance(hbaseConf)}

  //logger
  private val log = LoggerFactory.getLogger(classOf[HBaseModule])



  @Named("hbaseSchema")
  @Singleton
  @Provides
  def provideHBaseSchema(hbaseConf: HBaseConf):Schema ={
    val hbaseSchema:Schema =   new Schema.Parser().parse(hbaseConf.hbaseSchema())
    hbaseSchema
  }

  @Provides
  @Singleton
  def provideHBaseClient(hbaseConf: HBaseConf,@Named("hbaseSchema") hbaseSchema:Schema):mutable.Map[String,HBaseClient]={
    log.info("HBase client 初始化")
    val ncHBaseClient:HBaseClient=new HBaseClient(hbaseConf.nc_ZK_Address(),hbaseConf.table(),hbaseConf.family(),hbaseConf.qualifier(),hbaseSchema)
    val scHBaseClient:HBaseClient=new HBaseClient(hbaseConf.sc_ZK_Address(),hbaseConf.table(),hbaseConf.family(),hbaseConf.qualifier(),hbaseSchema)

    val hdfsClientMap:mutable.HashMap[String,HBaseClient]= new mutable.HashMap[String,HBaseClient];
    hdfsClientMap += ( hbaseConf.sc_ZK_City()  ->scHBaseClient)
    hdfsClientMap += (  hbaseConf.nc_ZK_City() -> ncHBaseClient)
    log.info("HBase client 初始化完成")
    hdfsClientMap
  }



  @Provides
  @Singleton
  def provideHBaseService(hdfsClientMap: mutable.Map[String,HBaseClient],@Named("hbaseSchema") hbaseSchema:Schema):HBaseService={
    val hBaseService:HBaseService = new HBaseService(hdfsClientMap.asJava,hbaseSchema)
    hBaseService
  }

}
