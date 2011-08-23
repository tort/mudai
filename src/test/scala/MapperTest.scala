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
      val mapper: MapperImpl = createMapper
      mapTerrain(mapper)
      checkPathBack(mapper)
      moveBack(mapper)
      checkForth(mapper)
    }
  }

  def createMapper(): MapperImpl = {
    val injector: Injector = Guice.createInjector(new MapperTestModule)
    val mapper: MapperImpl = injector.getInstance(classOf[MapperImpl])
    assert(mapper != null)

    mapper
  }

  def room3: String = {
    "room 3"
  }

  def mapTerrain(mapper: MapperImpl) {
    val roomSnapshot: RoomSnapshot = new RoomSnapshot()
    roomSnapshot.setLocationTitle("room 1")
    mapper.lookAround(roomSnapshot)
    assert(mapper.currentLocation() != null)

    mapper.move(Directions.SOUTH.getName, "room 2")
    mapper.move(Directions.SOUTH.getName, room3);
  }

  def checkPathBack(mapper: MapperImpl) {
    val pathBack: List[Direction] = mapper.pathTo("room 1")
    assert(pathBack.size() == 2)
  }

  def moveBack(mapper: MapperImpl) {
    mapper.move(Directions.NORTH.getName, "room 2")
    mapper.move(Directions.NORTH.getName, "room 1")
    assert(mapper.currentLocation().getTitle == "room 1")
  }

  def checkForth(mapper: MapperImpl) {
    val pathForth: List[Direction] = mapper.pathTo(room3)
    assert(pathForth.size == 2)
  }

  override protected def afterEach() {
    val db: File = new File("mapper-test.db")
    if (db.exists())
      db.deleteOnExit()
  }
}