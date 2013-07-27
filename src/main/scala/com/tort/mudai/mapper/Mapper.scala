package com.tort.mudai.mapper

import com.google.inject.Inject
import com.tort.mudai.RoomSnapshot
import com.tort.mudai.task.{EventDistributor, StatedTask}
import scalax.collection.mutable.Graph
import scalax.collection.edge.Implicits._
import scalax.collection.edge.LUnDiEdge
import com.tort.mudai.Metadata.Direction._
import com.tort.mudai.persistance.{Stat, MudaiDb}

trait Mapper {
  def currentLocation: Location

  def markWaterSource(waterSource: String)

  def nearestWaterSource: Option[Location]

  def pathTo(target: Location): List[Direction]

  def nearestShop: Option[Location]

  def nearestTavern: Option[Location]
}

class MapperImpl @Inject()(
                           persister: Persister,
                           directionHelper: DirectionHelper,
                           eventDistributor: EventDistributor) extends StatedTask with Mapper {
  private var current: Option[Location] = None
  private val graph: Graph[Location, LUnDiEdge] = Graph.empty

  override def glance(direction: Direction, roomSnapshot: RoomSnapshot) {
    synchronized {
      val newCurrent = current match {
        case Some(x) =>
          locationFromMap(x, direction, roomSnapshot).orElse(mapLocationWithExits(currentLocation, roomSnapshot, direction))
        case None =>
          findLocation(roomSnapshot)
      }

      current = newCurrent

      if (current.isDefined)
        resume()
      else
        pause()

      current.foreach(updateMobs(_, roomSnapshot))
    }
  }

  override def glance(roomSnapshot: RoomSnapshot) {
    synchronized {
      current = findOrMapLocation(roomSnapshot)
      current.foreach {
        resume()
        updateMobs(_, roomSnapshot)
      }
    }
  }

  def matchSnapshot(location: Location, roomSnapshot: RoomSnapshot) = {
    roomSnapshot.title == location.title &&
      roomSnapshot.desc == location.desc
  }

  def mapNewLocation(location: Location): Some[Location] = {
    graph += location
    Some(location)
  }

  def mapNewLocation(roomSnapshot: RoomSnapshot): Some[Location] = {
    mapNewLocation(Location(roomSnapshot))
  }

  def mapExits(fromLocation: Location, direction: Direction, toLocation: Location) {
    val edge = (fromLocation ~+ toLocation)(direction.id)
    graph += edge
    println("persisted path between " + fromLocation.title + " and " + toLocation.title)
  }

  private def criterion(currentLocation: Location, direction: Direction): (Location) => Boolean = {
    loc => loc != currentLocation && counterExitNotMapped(loc, direction)
  }

  private def mapLocationWithExits(currentLocation: Location, roomSnapshot: RoomSnapshot, direction: Direction) = {
    val persistentLocation = findOrMapLocation(roomSnapshot, criterion(currentLocation, direction))
    persistentLocation.foreach(mapExits(currentLocation, direction, _))
    persistentLocation
  }

  private def locationFromMap(currentLocation: Location, direction: Direction, roomSnapshot: RoomSnapshot): Option[Location] = {
    val locOption: Option[Location] = locationByDirection(currentLocation, direction)
    locOption.flatMap(loc => {
      if (matchSnapshot(loc, roomSnapshot))
        Some(loc)
      else {
        println("EXPECTED (" + loc.title + ") LOCATION DOESNT MATCH FOUND (" + roomSnapshot.title + ")")
        None
      }
    })
  }


  private def locationByDirection(currentLocation: Location, direction: Direction): Option[Location] = {
    node(currentLocation).outgoing.find(_.label == direction.id).map(_._2)
  }

  private def findOrMapLocation(roomSnapshot: RoomSnapshot, criterion: (Location) => Boolean = (Location) => true) = {
    val similar = graph.nodes.find((n: Location) => n.title == roomSnapshot.title && n.desc == roomSnapshot.desc)
    if (similar.isEmpty)
        mapNewLocation(Location(roomSnapshot))
    else
      None
  }

  private def findLocation(roomSnapshot: RoomSnapshot, criterion: (Location) => Boolean = (Location) => true): Option[Location] = {
    val locations = graph.nodes.filter((n: Location) => n.title == roomSnapshot.title && n.desc == roomSnapshot.desc)
    println("location TITLE " + roomSnapshot.title + "CNT: " + locations.size)
    locations.size match {
      case 1 =>
        locations.headOption.map[Location](x => x)

      case _ =>
        println("NONE")
        None
    }
  }

  private def counterExitNotMapped(location: Location, direction: Direction) = {
    val room = locationByDirection(location, oppositeDirection(direction))

    room == None
  }

  private def updateHabitation(currentLocation: Location, mob: Mob) {
    persister.persistMob(mob.habitationArea(mob.habitationArea + currentLocation))
  }

  private def kill(target: String, location: Location) {
    val mob = persister.tryFindMob(target).getOrElse({
      val newMob = Mob("target")
      persister.persistMob(newMob)
      newMob
    })
    updateHabitation(location, mob)
  }

  override def kill(target: String) {
    current.foreach(kill(target, _))
  }

  private def updateMobs(location: Location, roomSnapshot: RoomSnapshot) {
    for (mobLongName <- roomSnapshot.mobs) {
      persister.tryFindMob(mobLongName).foreach(mob => updateHabitation(location, mob))
    }
  }

  private def pathTo(currentLocation: Location, target: Location): Option[List[Direction]] = {
    (node(currentLocation) shortestPathTo node(target)).map(_.edges.map(_.label.toString).map(aliasToDirection(_)))
  }

  private def node(location: Location) = graph.get(location)

  override def pathTo(target: Location)  = {
    current.flatMap(curr => pathTo(curr, target)).getOrElse(List())
  }

  //TODO eliminate null. use option itself
  override def currentLocation = synchronized {
    current.getOrElse(null)
  }

  private def markWaterSource(currentLocation: Location, waterSource: String) {
    currentLocation.waterSource = Some(waterSource)
  }

  override def markWaterSource(waterSource: String) {
    current.foreach(markWaterSource(_, waterSource))
  }

  private def nearest(condition: (Location) => Boolean): Option[Location] = {
    graph.nodes.find((n: Location) => condition(n)).map[Location](x => x)
  }

  override def nearestWaterSource = nearest(location => location.waterSource != null)

  override def nearestShop = nearest(location => location.isShop)

  override def nearestTavern = nearest(location => location.isTavern)
  
  def current(location: Location) {
    synchronized {
      current = Some(location)
    }
  }

  override def viewStat(stat: Stat) {
    MudaiDb.saveStat(stat)
  }
}

class JMapperWrapper @Inject()(val mapper: Mapper) {
  def currentLocation = mapper.currentLocation

  def markWaterSource(waterSource: String) {
    mapper.markWaterSource(waterSource)
  }

  def nearestShop: Location = mapper.nearestShop.get

  def nearestTavern: Location = mapper.nearestTavern.get

  def nearestWaterSource: Location = mapper.nearestWaterSource.get

  def pathTo(target: Location): Seq[Direction] = {
    val path = mapper.pathTo(target)
    Seq()//TODO implement
  }
}
