package com.tort.mudai.mapper

import com.tort.mudai.RoomKey

trait Persister {
  def persistLocation(current: Location)

  /**
   * find mob by long name, or create new
   * @param name
   * @return mob, found or created
   */
  def tryFindMob(name: String): Option[Mob]

  @deprecated("use tryFindMob instead")
  def findMob(mobLongName: String): Mob

  def persistMob(mob: Mob)

  def loadLocations(prototype: RoomKey): Seq[Location]

  def loadLocations(locationTitle: String): Seq[Location]

  def locationsByCondition(condition: (Location) => Boolean): Seq[Location]

  def enlistLocations: Seq[Location]

  def enlistMobs: Seq[Mob]
}

class MemPersister extends Persister {
  val locations = scala.collection.mutable.ArrayBuffer[Location]()
  val mobs = new scala.collection.mutable.HashSet[Mob]

  def persistLocation(location: Location) { locations += location }

  def tryFindMob(name: String) = mobs.find(_.name == name)

  def findMob(name: String) = tryFindMob(name).get

  def persistMob(mob: Mob) { mobs.add(mob) }

  def loadLocations(prototype: RoomKey) = locations.filter(loc => loc.title == prototype.title && loc.desc == prototype.desc)

  def locationsByCondition(condition: (Location) => Boolean) = locations.filter(condition)

  def enlistLocations = locations

  def enlistMobs = mobs.toSeq

  def loadLocations(title: String) = locations.filter(_.title == title).toSeq

  def loadAllLocations = locations
}

