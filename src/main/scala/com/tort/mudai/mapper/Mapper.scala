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

class MapperImpl @Inject()(graph: DirectedGraph[Location, Direction],
                           persister: Persister,
                           directionHelper: DirectionHelper,
                           eventDistributor: EventDistributor) extends StatedTask with Mapper {
  private var current: Option[Location] = None

  private def mapExits(currentLocation: Location, direction: String, newLocation: Location) {
    currentLocation.addDirection(direction, newLocation)
    val oppositeDirection = directionHelper.getOppositeDirectionName(direction);
    newLocation.addDirection(oppositeDirection, currentLocation);
    graph.addEdge(currentLocation, newLocation, new Direction(direction));
    graph.addEdge(newLocation, currentLocation, new Direction(oppositeDirection));
    println("persisted path from " + currentLocation.title + " to " + newLocation.title)
  }

  private def mapLocationWithExits(currentLocation: Location, roomSnapshot: RoomSnapshot, direction: String) = {
    val location = findOrMapLocation(roomSnapshot, loc => loc != currentLocation && counterExitNotMapped(loc, direction))
    location.foreach(loc => mapExits(currentLocation, direction, loc))
    location
  }

  def matchSnapshot(location: Location, roomSnapshot: RoomSnapshot) = {
    roomSnapshot.title == location.title &&
      roomSnapshot.desc == location.desc &&
      roomSnapshot.exits == location.exits
  }

  private def fromMap(currentLocation: Location, direction: String, roomSnapshot: RoomSnapshot): Option[Location] = {
    val locOption = currentLocation.getByDirection(direction)
    locOption.map(loc => {
      if (matchSnapshot(loc, roomSnapshot))
        loc
      else {
        println("EXPECTED (" + loc.title + ") LOCATION DOESNT MATCH FOUND (" + roomSnapshot.title + ")")
        null
      }
    })
  }

  override def glance(direction: String, roomSnapshot: RoomSnapshot) {
    synchronized {
      current = current.flatMap {
        resume()
        fromMap(_, direction, roomSnapshot).orElse(mapLocationWithExits(currentLocation, roomSnapshot, direction))
      }.orElse{
        pause()
        findOrMapLocation(roomSnapshot)
      }
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

  private def findOrMapLocation(roomSnapshot: RoomSnapshot, criterion: (Location) => Boolean = (Location) => true) = {
    //TODO use RoomKey
    val location = createLocation(roomSnapshot)

    val locations = persister.loadLocations(location)
    val filtered = locations filter criterion
    filtered.size match {
      case 0 =>
        persister.persistLocation(location)
        graph.addVertex(location)
        Some(location)

      case 1 => Some(locations.head)

      case _ => None
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

  def createLocation(roomSnapshot: RoomSnapshot): Location = {
    Location(
      roomSnapshot.title,
      roomSnapshot.desc,
      roomSnapshot.exits
    )
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
}

