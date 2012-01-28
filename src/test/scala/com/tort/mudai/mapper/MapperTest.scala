package com.tort.mudai.mapper

import com.google.inject.{Injector, Guice}
import org.scalatest.{BeforeAndAfterEach, FeatureSpec}
import org.scalatest.matchers.ShouldMatchers
import com.tort.mudai.RoomSnapshot

class MapperTest extends FeatureSpec with BeforeAndAfterEach with ShouldMatchers {
  val room3: String = "room 3"
  val desc: String = "desc"
  var injector: Injector = _
  var persister: Persister = _
  var mapper: MapperImpl = _

  override protected def beforeEach() {
    injector = Guice.createInjector(new MapperTestModule)
    persister = injector.getInstance(classOf[Persister])
    mapper = injector.getInstance(classOf[MapperImpl])
    assert(persister.enlistLocations.size == 0)
  }

  feature("mapping") {
    scenario("map several equal locations in same direction") {
      mapLocation(Set(Directions.SOUTH))
      mapSomeRooms(Directions.SOUTH, southAndNorthExits)
    }

    scenario("lose map, then continue mapping when known room found") {
      val startRoomExits = Set(Directions.SOUTH, Directions.EAST, Directions.WEST)
      val startLocation = mapStartLocation(startRoomExits)

      mapSomeRooms(Directions.SOUTH, exitsAll)
      loseMap(Directions.EAST)
      walkThroughUnknownTerrain
      findKnownRoom(startRoomExits, startLocation)
      mapLocation(Directions.SOUTH, southAndNorthExits)
    }

    scenario("map three locations, pass back, check way forth") {
      checkPathForth(mapper)
    }

    scenario("when identifying room, take description into account") {
      mapper.glance(room("title", "desc 1", southAndNorthExits))
      mapper.glance(Directions.SOUTH.getName, room("title", "desc 2", southAndNorthExits))
      mapper.glance(Directions.SOUTH.getName, room("title", "desc 3", southAndNorthExits))

      persister.enlistLocations.size should equal(3)
    }

    scenario("when identifying room, take available directions into account") {
      mapper.glance(RoomSnapshot("title", "desc", Set(Directions.SOUTH)))
      mapper.glance(Directions.SOUTH.getName, RoomSnapshot("title", "desc", Set(Directions.SOUTH, Directions.NORTH)))
      mapper.glance(Directions.SOUTH.getName, RoomSnapshot("title", "desc", Set(Directions.SOUTH, Directions.NORTH, Directions.EAST)))

      persister.enlistLocations.size should equal(3)
    }
  }

  def southAndNorthExits = Set(Directions.SOUTH, Directions.NORTH)

  def exitsAll = Set(Directions.SOUTH, Directions.NORTH, Directions.EAST, Directions.WEST)

  def mapTerrain(mapper: MapperImpl): Location = {
    val roomSnapshot: RoomSnapshot = new RoomSnapshot(
      "room 1",
      desc,
      southAndNorthExits)
    mapper.glance(roomSnapshot)
    assert(mapper.currentLocation != null)
    val firstRoom = mapper.currentLocation

    mapper.glance(Directions.SOUTH.getName, room("room 2", desc, southAndNorthExits))
    mapper.glance(Directions.SOUTH.getName, room(room3, desc, southAndNorthExits));

    firstRoom
  }

  def room(title: String, desc: String, exits: Set[Directions]): RoomSnapshot = {
    val roomSnapshot: RoomSnapshot = new RoomSnapshot(title, desc, exits)

    roomSnapshot
  }

  def room(title: String, desc: String): RoomSnapshot = {
    new RoomSnapshot(title, desc, Set())
  }

  def checkPathBack(mapper: MapperImpl) {
    val firstRoom: Location = mapTerrain(mapper)

    val pathBack = mapper.pathTo(firstRoom)
    assert(pathBack.size == 2)
  }

  def moveBack(mapper: MapperImpl): Location = {
    checkPathBack(mapper)

    val lastRoom: Location = mapper.currentLocation

    val exits = Set(Directions.SOUTH, Directions.NORTH)
    mapper.glance(Directions.NORTH.getName, room("room 2", desc, exits))
    mapper.glance(Directions.NORTH.getName, room("room 1", desc, exits))
    assert(mapper.currentLocation.title == "room 1")

    lastRoom
  }

  def checkPathForth(mapper: MapperImpl) {
    val lastRoom: Location = moveBack(mapper)

    val pathForth = mapper.pathTo(lastRoom)
    assert(pathForth.size == 2)
  }

  def mapLocation(direction: Directions, exits: Set[Directions]) {
    val roomCount = persister.enlistLocations.size
    //TODO eliminate getName
    mapper.glance(direction.getName, room("title", "desc", exits))
    val size = persister.enlistLocations.size
    size should equal(roomCount + 1)
  }

  def mapLocation(exits: Set[Directions]) {
    val roomCount = persister.enlistLocations.size
    //TODO eliminate getName
    mapper.glance(room("title", "desc", exits))
    val size = persister.enlistLocations.size
    size should equal(roomCount + 1)
  }

  def mapSomeRooms(direction: Directions, exits: Set[Directions]) {
    (1 to 4).foreach(i => mapLocation(direction, exits))
  }

  def loseMap(direction: Directions) {
    // unable to detect unique targetLocation. map lost
    val roomCount = persister.enlistLocations.size
    mapper.glance(direction.getName, room("title", "desc", exitsAll))
    persister.enlistLocations.size should equal(roomCount)
    assert(mapper.isPaused)
  }

  def walkThroughUnknownTerrain {
    //do not map new rooms until find known
    val roomCount = persister.enlistLocations.size
    mapper.glance(Directions.EAST.getName, room("unknownTitle", "unknownDesc", exitsAll))
    persister.enlistLocations.size should equal(roomCount)
    assert(mapper.isPaused)
  }

  def findKnownRoom(startRoomExits: Set[Directions], startLocation: Location) {
    // unique room identified. mapping works again
    val roomCount = persister.enlistLocations.size
    mapper.glance(Directions.EAST.getName, room("first", "desc", startRoomExits))
    persister.enlistLocations.size should equal(roomCount)
    assert(mapper.isRunning)

    val cur = mapper.currentLocation
    assert(cur == startLocation)
  }

  def mapStartLocation(startRoomExits: Set[Directions]) = {
    mapper.glance(RoomSnapshot("first", "desc", startRoomExits))
    mapper.currentLocation
  }
}
