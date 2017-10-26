package com.genitus.channel.tracker.config

import org.rogach.scallop.{ScallopConf, ScallopOption}

trait HDFSConf extends ScallopConf{
  lazy val nc_Sextant_Address: ScallopOption[String] = opt[String](
    "nc_Sextant_Address",
    descr = "nc_Sextant_Address",
    //default = Some("172.28.0.19:7070"),
    default=Some("jwzheng"),
    required = true,
    noshort = true
  )

  lazy val sc_Sextant_Address: ScallopOption[String] = opt[String](
    "sc_Sextant_Address",
    descr = "sc_Sextant_Address",
   // default = Some("172.28.128.17:7070"),
    default=Some("jwzheng"),
    required = true,
    noshort = true
  )

  lazy val nc_Sextant_city: ScallopOption[String] = opt[String](
    "nc_Sextant_City",
    descr = "nc_Sextant_City",
    // default = Some("172.28.128.17:7070"),
    default=Some("jwzheng"),
    required = true,
    noshort = true
  )
  lazy val sc_Sextant_city: ScallopOption[String] = opt[String](
    "sc_Sextant_City",
    descr = "sc_Sextant_City",
    // default = Some("172.28.128.17:7070"),
    default=Some("jwzheng"),
    required = true,
    noshort = true
  )
 // private Schema mediaSchema =   new Schema.Parser().parse("{\"type\":\"record\",\"name\":\"SessionMedia\",\"namespace\":\"org.genitus.karyo.model.data\",\"fields\":[{\"name\":\"sid\",\"type\":\"string\"},{\"name\":\"uid\",\"type\":\"string\"},{\"name\":\"recStatus\",\"type\":\"int\"},{\"name\":\"mediaData\",\"type\":{\"type\":\"record\",\"name\":\"MediaData\",\"namespace\":\"org.genitus.karyo.model.log\",\"fields\":[{\"name\":\"type\",\"type\":\"int\"},{\"name\":\"data\",\"type\":{\"type\":\"bytes\",\"java-class\":\"[B\"}}]}}]}");
 // private Schema dataSchema= new Schema.Parser().parse("{\"type\":\"record\",\"name\":\"SessionData\",\"namespace\":\"org.genitus.karyo.model.data\",\"fields\":[{\"name\":\"sid\",\"type\":\"string\"},{\"name\":\"svcLogs\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"SvcLog\",\"namespace\":\"org.genitus.karyo.model.log\",\"fields\":[{\"name\":\"type\",\"type\":\"int\"},{\"name\":\"sid\",\"type\":\"string\"},{\"name\":\"uid\",\"type\":\"string\"},{\"name\":\"syncid\",\"type\":\"int\"},{\"name\":\"timestamp\",\"type\":\"long\"},{\"name\":\"ip\",\"type\":\"int\"},{\"name\":\"callName\",\"type\":\"string\"},{\"name\":\"logs\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"RawLog\",\"fields\":[{\"name\":\"timestamp\",\"type\":\"long\"},{\"name\":\"level\",\"type\":\"string\"},{\"name\":\"extras\",\"type\":{\"type\":\"map\",\"values\":\"string\"}},{\"name\":\"descs\",\"type\":{\"type\":\"array\",\"items\":\"string\",\"java-class\":\"java.util.List\"}}]},\"java-class\":\"java.util.List\"}},{\"name\":\"mediaData\",\"type\":{\"type\":\"record\",\"name\":\"MediaData\",\"fields\":[{\"name\":\"type\",\"type\":\"int\"},{\"name\":\"data\",\"type\":{\"type\":\"bytes\",\"java-class\":\"[B\"}}]}}]},\"java-class\":\"java.util.List\"}}]}");    //  args[7] : new schema string , use to parse hdfs data


  /** log schema. */
  lazy val mediaSchema = opt[String](
    "media schema",
    descr = "media schema",
    default = Some("{\"type\":\"record\",\"name\":\"SessionMedia\",\"namespace\":\"org.genitus.karyo.model.data\",\"fields\":[{\"name\":\"sid\",\"type\":\"string\"},{\"name\":\"uid\",\"type\":\"string\"},{\"name\":\"recStatus\",\"type\":\"int\"},{\"name\":\"mediaData\",\"type\":{\"type\":\"record\",\"name\":\"MediaData\",\"namespace\":\"org.genitus.karyo.model.log\",\"fields\":[{\"name\":\"type\",\"type\":\"int\"},{\"name\":\"data\",\"type\":{\"type\":\"bytes\",\"java-class\":\"[B\"}}]}}]}"),
    required = true,
    noshort = true
  )

  /** log schema. */
  lazy val dataSchema = opt[String](
    "data schema",
    descr = "data schema",
    default = Some("{\"type\":\"record\",\"name\":\"SessionData\",\"namespace\":\"org.genitus.karyo.model.data\",\"fields\":[{\"name\":\"sid\",\"type\":\"string\"},{\"name\":\"svcLogs\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"SvcLog\",\"namespace\":\"org.genitus.karyo.model.log\",\"fields\":[{\"name\":\"type\",\"type\":\"int\"},{\"name\":\"sid\",\"type\":\"string\"},{\"name\":\"uid\",\"type\":\"string\"},{\"name\":\"syncid\",\"type\":\"int\"},{\"name\":\"timestamp\",\"type\":\"long\"},{\"name\":\"ip\",\"type\":\"int\"},{\"name\":\"callName\",\"type\":\"string\"},{\"name\":\"logs\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"RawLog\",\"fields\":[{\"name\":\"timestamp\",\"type\":\"long\"},{\"name\":\"level\",\"type\":\"string\"},{\"name\":\"extras\",\"type\":{\"type\":\"map\",\"values\":\"string\"}},{\"name\":\"descs\",\"type\":{\"type\":\"array\",\"items\":\"string\",\"java-class\":\"java.util.List\"}}]},\"java-class\":\"java.util.List\"}},{\"name\":\"mediaData\",\"type\":{\"type\":\"record\",\"name\":\"MediaData\",\"fields\":[{\"name\":\"type\",\"type\":\"int\"},{\"name\":\"data\",\"type\":{\"type\":\"bytes\",\"java-class\":\"[B\"}}]}}]},\"java-class\":\"java.util.List\"}}]}") ,
    required = true,
    noshort = true
  )
}
