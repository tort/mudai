package com.tort.mudai.mapper

case class Mob(id: String,
               fullName: String,
               shortName: Option[String],
               alias: Option[String],
               killable: Boolean)
