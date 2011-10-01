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
  }

  feature("mapping") {
    scenario("map several equal locations in same direction") {
      val startRoomExits = Set(Directions.SOUTH)
      mapper.glance(RoomSnapshot("title", "desc", startRoomExits))
      //TODO eliminate getName
      persister.enlistLocations.size should equal(1)
      mapper.glance(Directions.SOUTH.getName, room("title", "desc", exits))
      persister.enlistLocations.size should equal(2)
      mapper.glance(Directions.SOUTH.getName, room("title", "desc", exits))
      persister.enlistLocations.size should equal(3)
      mapper.glance(Directions.SOUTH.getName, room("title", "desc", exits))
      persister.enlistLocations.size should equal(4)
      mapper.glance(Directions.SOUTH.getName, room("title", "desc", exits))

      persister.enlistLocations.size should equal(5)
    }

    scenario("lose map, then continue mapping when known room found") {
      val startRoomExits = Set(Directions.SOUTH, Directions.EAST, Directions.WEST)
      mapper.glance(RoomSnapshot("first", "desc", startRoomExits))
      val startLocation = mapper.currentLocation

      persister.enlistLocations.size should equal(1)
      mapper.glance(Directions.SOUTH.getName, RoomSnapshot("title", "desc", exitsAll))
      persister.enlistLocations.size should equal(2)
      mapper.glance(Directions.SOUTH.getName, room("title", "desc", exitsAll))
      persister.enlistLocations.size should equal(3)
      mapper.glance(Directions.SOUTH.getName, room("title", "desc", exitsAll))
      persister.enlistLocations.size should equal(4)
      mapper.glance(Directions.SOUTH.getName, room("title", "desc", exitsAll))
      persister.enlistLocations.size should equal(5)
      // unable to detect unique targetLocation. map lost
      mapper.glance(Directions.EAST.getName, room("title", "desc", exitsAll))
      persister.enlistLocations.size should equal(5)
      // unique room identified. mapping works again
      mapper.glance(Directions.EAST.getName, room("first", "desc", startRoomExits))
      persister.enlistLocations.size should equal(5)
      val cur = mapper.currentLocation
      assert(cur == startLocation)
    }

    scenario("map three locations, pass back, check way forth") {
      val injector: Injector = Guice.createInjector(new MapperTestModule)
      val mapper: MapperImpl = injector.getInstance(classOf[MapperImpl])
      assert(mapper != null)

      checkPathForth(mapper)
    }

    scenario("when identifying room, take description into account") {
      val injector: Injector = Guice.createInjector(new MapperTestModule)
      val persister: Persister = injector.getInstance(classOf[Persister])
      val mapper: MapperImpl = injector.getInstance(classOf[MapperImpl])

      assert(persister.enlistLocations.size == 0)

      mapper.glance(room("title", "desc 1", exits))
      mapper.glance(Directions.SOUTH.getName, room("title", "desc 2", exits))
      mapper.glance(Directions.SOUTH.getName, room("title", "desc 3", exits))

      persister.enlistLocations.size should equal(3)
    }

    scenario("when identifying room, take available directions into account") {
      val injector: Injector = Guice.createInjector(new MapperTestModule)
      val persister: Persister = injector.getInstance(classOf[Persister])
      val mapper: MapperImpl = injector.getInstance(classOf[MapperImpl])

      assert(persister.enlistLocations.size == 0)

      mapper.glance(RoomSnapshot("title", "desc", Set(Directions.SOUTH)))
      mapper.glance(Directions.SOUTH.getName, RoomSnapshot("title", "desc", Set(Directions.SOUTH, Directions.NORTH)))
      mapper.glance(Directions.SOUTH.getName, RoomSnapshot("title", "desc", Set(Directions.SOUTH, Directions.NORTH, Directions.EAST)))

      persister.enlistLocations.size should equal(3)
    }
  }

  def exits = {
    Set(Directions.SOUTH, Directions.NORTH)
  }

  def exitsAll = {
    Set(Directions.SOUTH, Directions.NORTH, Directions.EAST, Directions.WEST)
  }

  def mapTerrain(mapper: MapperImpl): Location = {
    val roomSnapshot: RoomSnapshot = new RoomSnapshot(
    "room 1",
    desc,
    exits)
    mapper.glance(roomSnapshot)
    assert(mapper.currentLocation != null)
    val firstRoom = mapper.currentLocation

    mapper.glance(Directions.SOUTH.getName, room("room 2", desc, exits))
    mapper.glance(Directions.SOUTH.getName, room(room3, desc, exits));

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
}
