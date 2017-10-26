package com.genitus.channel.tracker.config

import org.rogach.scallop.ScallopConf

trait ESConf extends ScallopConf{
  lazy val nc_ES_IP = opt[String](
    "nc_ES_IP",
    descr = "nc_ES_IP",
    //  default = Sojwzhengme("172.28.4.20"),
    default=Some(""),
    required = true,
    noshort = true
  )
  lazy val sc_ES_IP = opt[String](
    "sc_ES_IP",
    descr = "sc_ES_IP",
    //  default = Some("172.28.4.20"),
    default=Some("jwzheng"),
    required = true,
    noshort = true
  )
  lazy val nc_ES_Port = opt[String](
    "nc_ES_Port",
    descr = "nc_ES_Port",
    default=Some("jwzheng"),
    //   default = Some("172.28.128.14"),
    required = true,
    noshort = true
  )

  lazy val sc_ES_Port = opt[String](
    "sc_ES_Port",
    descr = "sc_ES_Port",
    default=Some("jwzheng"),
    //   default = Some("172.28.128.14"),
    required = true,
    noshort = true
  )


  lazy val nc_ES_Cluster_Name = opt[String](
    "nc_ES_Cluster_Name",
    descr = "nc_ES_Cluster_Name",
    default=Some("jwzheng"),
    //   default = Some("9300"),
    required = true,
    noshort = true
  )

  lazy val sc_ES_Cluster_Name = opt[String](
    "sc_ES_Cluster_Name",
    descr = "sc_ES_Cluster_Name",
    default=Some("jwzheng"),
    //   default = Some("9300"),
    required = true,
    noshort = true
  )

  lazy val nc_ES_City = opt[String](
    "nc_ES_City",
    descr = "nc_ES_City",
    default=Some("jwzheng"),
    //  default = Some("9300"),
    required = true,
    noshort = true
  )
  lazy val sc_ES_City = opt[String](
    "sc_ES_City",
    descr = "sc_ES_City",
    default=Some("jwzheng"),
    //  default = Some("9300"),
    required = true,
    noshort = true
  )

}
