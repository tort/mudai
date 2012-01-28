package com.tort.mudai.mapper

import org.jgrapht.DirectedGraph
import com.google.inject.Inject
import com.tort.mudai.RoomSnapshot
import com.tort.mudai.task.{EventDistributor, StatedTask}
import org.jgrapht.alg.DijkstraShortestPath
import java.util.{List => JList}
import collection.JavaConversions

trait Mapper {
  def currentLocation: Location

  def markWaterSource(waterSource: String)

  def nearestWaterSource: Option[Location]

  def pathTo(target: Location): Seq[Direction]

  def nearestShop: Option[Location]

  def nearestTavern: Option[Location]
}

class MapperImpl @Inject()(graph: DirectedGraph[Location, Direction],
                           persister: Persister,
                           directionHelper: DirectionHelper,
                           eventDistributor: EventDistributor) extends StatedTask with Mapper with LocationHelper {
  private var current: Option[Location] = None

  override def glance(direction: String, roomSnapshot: RoomSnapshot) {
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
      roomSnapshot.desc == location.desc &&
      roomSnapshot.exits == location.exits
  }

  def mapNewLocation(location: Location): Some[Location] = {
    persister.persistLocation(location)
    graph.addVertex(location)
    Some(location)
  }

  def mapNewLocation(roomSnapshot: RoomSnapshot): Some[Location] = {
    mapNewLocation(createLocation(roomSnapshot))
  }

  def mapExits(fromLocation: Location, direction: String, toLocation: Location) {
    fromLocation.addDirection(direction, toLocation)
    val oppositeDirection = directionHelper.getOppositeDirectionName(direction);
    toLocation.addDirection(oppositeDirection, fromLocation);
    graph.addEdge(fromLocation, toLocation, new Direction(direction));
    graph.addEdge(toLocation, fromLocation, new Direction(oppositeDirection));
    println("persisted path from " + fromLocation.title + " to " + toLocation.title)
  }

  private def criterion(currentLocation: Location, direction: String): (Location) => Boolean = {
    loc => loc != currentLocation && counterExitNotMapped(loc, direction)
  }

  private def mapLocationWithExits(currentLocation: Location, roomSnapshot: RoomSnapshot, direction: String) = {
    val persistentLocation = findOrMapLocation(roomSnapshot, criterion(currentLocation, direction))
    persistentLocation.foreach(mapExits(currentLocation, direction, _))
    persistentLocation
  }

  private def locationFromMap(currentLocation: Location, direction: String, roomSnapshot: RoomSnapshot): Option[Location] = {
    val locOption = currentLocation.getByDirection(direction)
    locOption.flatMap(loc => {
      if (matchSnapshot(loc, roomSnapshot))
        Some(loc)
      else {
        println("EXPECTED (" + loc.title + ") LOCATION DOESNT MATCH FOUND (" + roomSnapshot.title + ")")
        None
      }
    })
  }

  private def findOrMapLocation(roomSnapshot: RoomSnapshot, criterion: (Location) => Boolean = (Location) => true) = {
    //TODO use RoomKey
    val location = createLocation(roomSnapshot)

    val locations = persister.loadLocations(location)
    locations.isEmpty match {
      case true =>
        mapNewLocation(location)

      case _ => None
    }
  }

  private def findLocation(roomSnapshot: RoomSnapshot, criterion: (Location) => Boolean = (Location) => true) = {
    //TODO use RoomKey
    val location = createLocation(roomSnapshot)

    val locations = persister.loadLocations(location)
    println("location TITLE " + roomSnapshot.title + "CNT: " + locations.size)
    locations.size match {
      case 1 =>
        locations.headOption

      case _ =>
        println("NONE")
        None
    }
  }

  private def counterExitNotMapped(location: Location, direction: String) = {
    val room = location.getByDirection(directionHelper.getOppositeDirectionName(direction))

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

  private def pathTo(currentLocation: Location, target: Location) = {
    val algorythm = new DijkstraShortestPath[Location, Direction](graph, currentLocation, target)

    val result = algorythm.getPathEdgeList
    JavaConversions.iterableAsScalaIterable(result).toSeq
  }

  override def pathTo(target: Location) = {
    current.map(pathTo(_, target)).getOrElse(Seq())
  }

  //TODO eliminate null. use option itself
  override def currentLocation = synchronized {
    current.getOrElse(null)
  }

  private def markWaterSource(currentLocation: Location, waterSource: String) {
    currentLocation.waterSource = Some(waterSource)
    persister.persistLocation(currentLocation)
  }

  override def markWaterSource(waterSource: String) {
    current.foreach(markWaterSource(_, waterSource))
  }

  private def nearest(condition: (Location) => Boolean): Option[Location] = {
    val locations = persister.locationsByCondition(condition)

    //TODO replace with searching for nearest
    locations.headOption
  }

  override def nearestWaterSource = nearest(location => location.waterSource != null)

  override def nearestShop = nearest(location => location.isShop)

  override def nearestTavern = nearest(location => location.isTavern)
  
  def current(location: Location) {
    synchronized {
      current = Some(location)
    }
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

  def pathTo(target: Location): JList[Direction] = {
    val path = mapper.pathTo(target)
    JavaConversions.seqAsJavaList(path)
  }
}

trait LocationHelper {
  def createLocation(roomSnapshot: RoomSnapshot): Location = {
    Location(
      roomSnapshot.title,
      roomSnapshot.desc,
      roomSnapshot.exits
    )
  }
}
