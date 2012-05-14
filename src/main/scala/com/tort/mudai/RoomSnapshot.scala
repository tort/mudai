package com.tort.mudai

import mapper.Exit


case class RoomSnapshot(
                         title: String,
                         desc: String,
                         exits: Set[Exit],
                         objectsPresent: Seq[String] = Seq(),
                         mobs: Seq[String] = Seq()
                         ) extends RoomKey

object RoomSnapshot {
  def apply(key: RoomKey): RoomSnapshot = RoomSnapshot(key.title, key.desc, key.exits)
}

trait RoomKey {
  def title: String

  def desc: String

  def exits: Set[Exit]
}
