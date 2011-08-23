package com.tort

import mudai.mapper.{Direction, Directions, MapperImpl, MapperTestModule}
import mudai.RoomSnapshot
import com.google.inject.{Injector, Guice}
import java.util.List
import java.io.File
import org.scalatest.{BeforeAndAfterEach, FeatureSpec}

class MapperTest extends FeatureSpec with BeforeAndAfterEach {
  feature("mapping") {
    scenario("map three locations, pass back, check way forth") {
      val injector: Injector = Guice.createInjector(new MapperTestModule)
      val mapper: MapperImpl = injector.getInstance(classOf[MapperImpl])

      assert(mapper != null)

      val roomSnapshot: RoomSnapshot = new RoomSnapshot()
      roomSnapshot.setLocationTitle("room 1")
      mapper.lookAround(roomSnapshot)
      assert(mapper.currentLocation() != null)

      mapper.move(Directions.SOUTH.getName, "room 2")
      mapper.move(Directions.SOUTH.getName, "room 3");

      val pathBack: List[Direction] = mapper.pathTo("room 1")
      assert(pathBack.size() == 2)

      mapper.move(Directions.NORTH.getName, "room 2")
      mapper.move(Directions.NORTH.getName, "room 1")
      assert(mapper.currentLocation().getTitle == "room 1")

      val pathForth: List[Direction] = mapper.pathTo("room 3")
      assert(pathForth.size == 2)
    }

  }

  override protected def afterEach() {
    val db: File = new File("mapper-test.db")
    if (db.exists())
      db.deleteOnExit()
  }
}