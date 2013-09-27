package com.tort.mudai.mapper

class Mob(val id: String,
               val fullName: String,
               val shortName: Option[String],
               val alias: Option[String],
               val killable: Boolean)

class Item( val id: String,
            val fullName: String,
            val shortName: Option[String],
            val alias: Option[String],
            val objectType: Option[String])
