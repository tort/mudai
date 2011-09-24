package com.tort.mudai.mapper

import com.google.inject.{Injector, Guice}
import java.util.List
import com.tort.mudai.RoomSnapshot
import org.scalatest.{BeforeAndAfterEach, FeatureSpec}
import org.scalatest.matchers.ShouldMatchers

class MapperTest extends FeatureSpec with BeforeAndAfterEach with ShouldMatchers {
  val room3: String = "room 3"
  val desc: String = "desc"

  feature("mapping") {
    scenario("map two equal locations, fall on third") {
      val injector: Injector = Guice.createInjector(new MapperTestModule)
      val persister: Persister = injector.getInstance(classOf[Persister])
      val mapper: MapperImpl = injector.getInstance(classOf[MapperImpl])

      val startRoomExits: java.util.Set[Directions] = new java.util.HashSet()
      startRoomExits.add(Directions.SOUTH)
      mapper.lookAround(room("title", "desc", startRoomExits))
      mapper.move(Directions.SOUTH.getName, room("title", "desc", exits))
      mapper.move(Directions.SOUTH.getName, room("title", "desc", exits))
      mapper.move(Directions.SOUTH.getName, room("anotherTitle", "desc", exits))

      persister.enlistLocations().size() should equal(4)
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

      assert(persister.enlistLocations().size() == 0)

      mapper.lookAround(room("title", "desc 1", exits))
      mapper.move(Directions.SOUTH.getName(), room("title", "desc 2", exits))
      mapper.move(Directions.SOUTH.getName(), room("title", "desc 3", exits))

      persister.enlistLocations().size() should equal(3)
    }

    scenario("when identifying room, take available directions into account") {
      val injector: Injector = Guice.createInjector(new MapperTestModule)
      val persister: Persister = injector.getInstance(classOf[Persister])
      val mapper: MapperImpl = injector.getInstance(classOf[MapperImpl])

      assert(persister.enlistLocations().size() == 0)

      val exits: java.util.Set[Directions] = new java.util.HashSet()
      exits.add(Directions.SOUTH)
      mapper.lookAround(room("title", "desc", exits))
      exits.add(Directions.NORTH)
      mapper.move(Directions.SOUTH.getName(), room("title", "desc", exits))
      exits.add(Directions.EAST)
      mapper.move(Directions.SOUTH.getName(), room("title", "desc", exits))

      persister.enlistLocations().size() should equal(3)
    }
    scenario("when identifying room, take neighbornship into account")(pending)
  }

  def exits = {
    val exits: java.util.Set[Directions] = new java.util.HashSet()
    exits.add(Directions.SOUTH)
    exits.add(Directions.NORTH)
    exits
  }

  def mapTerrain(mapper: MapperImpl): Location = {
    val roomSnapshot: RoomSnapshot = new RoomSnapshot()
    roomSnapshot.setLocationTitle("room 1")
    roomSnapshot.setLocationDesc(desc)
    roomSnapshot.setExits(exits)
    mapper.lookAround(roomSnapshot)
    assert(mapper.currentLocation() != null)
    val firstRoom = mapper.currentLocation()

    mapper.move(Directions.SOUTH.getName, room("room 2", desc, exits))
    mapper.move(Directions.SOUTH.getName, room(room3, desc, exits));

    firstRoom
  }

  def room(title: String, desc: String, exits: java.util.Set[Directions]): RoomSnapshot = {
    val roomSnapshot: RoomSnapshot = room(title, desc)
    roomSnapshot.setExits(exits)

    roomSnapshot
  }

  def room(title: String, desc: String): RoomSnapshot = {
    val snapshot = new RoomSnapshot
    snapshot.setLocationTitle(title)
    snapshot.setLocationDesc(desc)

    snapshot
  }

  def checkPathBack(mapper: MapperImpl) {
    val firstRoom: Location = mapTerrain(mapper)

    val pathBack: List[Direction] = mapper.pathTo(firstRoom)
    assert(pathBack.size() == 2)
  }

  def moveBack(mapper: MapperImpl): Location = {
    checkPathBack(mapper)

    val lastRoom: Location = mapper.currentLocation()

    val exits: java.util.Set[Directions] = new java.util.HashSet()
    exits.add(Directions.SOUTH)
    exits.add(Directions.NORTH)
    mapper.move(Directions.NORTH.getName, room("room 2", desc, exits))
    mapper.move(Directions.NORTH.getName, room("room 1", desc, exits))
    assert(mapper.currentLocation().getTitle == "room 1")

    lastRoom
  }

  def checkPathForth(mapper: MapperImpl) {
    val lastRoom: Location = moveBack(mapper)

    val pathForth: List[Direction] = mapper.pathTo(lastRoom)
    assert(pathForth.size == 2)
  }
}
