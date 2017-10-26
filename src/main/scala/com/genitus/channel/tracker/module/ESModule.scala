package com.genitus.channel.tracker.module



import com.genitus.channel.tracker.client.ESClient
import com.genitus.channel.tracker.config.{ESConf, KuduConf}
import com.genitus.channel.tracker.service.ESService
import com.google.inject.{AbstractModule, Provides, Singleton}
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._
import scala.collection.mutable


class ESModule(val esConf: ESConf) extends AbstractModule{
  override def configure(): Unit = {bind(classOf[ESConf]).toInstance(esConf)}

  //logger
  private val log = LoggerFactory.getLogger(classOf[ESModule])

/*
  @Provides
  @Singleton
  def provideESClient(esConf: ESConf):Map[String,ESClient]={
    val ncESClient:ESClient = new ESClient("cluster.name",esConf.ncESClusterName(),esConf.ncESIp(),esConf.ncESPort())
    val scESClient:ESClient = new ESClient("cluster.name",esConf.scESClusterName(),esConf.scESIp(),esConf.scESPort())
    val esClientMap: Map[String, ESClient] = Map(
      "sc"->scESClient,
      "nc"->ncESClient
    )
    log.info("完成esClientMap启动")
    esClientMap
  }*/


  @Provides
  @Singleton
  def provideESClient(esConf: ESConf):mutable.Map[String,ESClient]={

    val nc_ES_IP:Array[String] = esConf.nc_ES_IP().split(",")
    for(i<- 0 until nc_ES_IP.length)
      println(nc_ES_IP(i))
    val sc_ES_IP:Array[String] = esConf.sc_ES_IP().split(",")
    for(i<- 0 until sc_ES_IP.length)
      println(sc_ES_IP(i))

    val nc_ES_Port:Array[String] = esConf.nc_ES_Port().split(",")
    for(i<- 0 until nc_ES_Port.length)
      println(nc_ES_Port(i))
    val sc_ES_Port:Array[String] = esConf.sc_ES_Port().split(",")
    for(i<- 0 until sc_ES_Port.length)
      println(sc_ES_Port(i))

    val esClientMap:mutable.HashMap[String,ESClient]= new mutable.HashMap[String,ESClient];
    val ncESClient:ESClient = new ESClient("cluster.name",esConf.nc_ES_Cluster_Name(),esConf.nc_ES_IP(),esConf.nc_ES_Port())
    val scESClient:ESClient = new ESClient("cluster.name",esConf.sc_ES_Cluster_Name(),esConf.sc_ES_IP(),esConf.sc_ES_Port())
    esClientMap += ( esConf.nc_ES_City() -> ncESClient)
    esClientMap += ( esConf.sc_ES_City() -> scESClient)
    log.info("完成esClientMap启动")
    esClientMap

  }

  @Provides
  @Singleton
  def provideESService( esClientMap: mutable.Map[String, ESClient]):ESService={
    val eSService:ESService = new ESService(esClientMap.asJava);
    eSService

  }

}
