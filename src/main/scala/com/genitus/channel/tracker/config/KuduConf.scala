package com.genitus.channel.tracker.config

import org.rogach.scallop.{ScallopConf, ScallopOption}

trait KuduConf extends ScallopConf{
  /** Impala  Address */
  lazy val impala_Address: ScallopOption[String] = opt[String](
    "impala_Address",
    descr = "impala_Address",
   // default = Some("172.26.5.11:21050"),
    default=Some("jwzheng"),
    required = true,
    noshort = true)
}
