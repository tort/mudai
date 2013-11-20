package com.tort.mudai

import com.tort.mudai.mapper.{Item, Exit}
import scalaz.@@
import com.tort.mudai.mapper.Mob.FullName


case class RoomSnapshot(
                         title: String,
                         desc: String,
                         exits: Set[Exit],
                         objectsPresent: Seq[ItemAndNumber] = Seq(),
                         mobs: Seq[String] = Seq()
                         ) extends RoomKey

object RoomSnapshot {
  def apply(key: RoomKey): RoomSnapshot = RoomSnapshot(key.title, key.desc, key.exits)
}

case class ItemAndNumber(item: String @@ Item.FullName, number: Int)

trait RoomKey {
  def title: String

  def desc: String

  def exits: Set[Exit]
}
