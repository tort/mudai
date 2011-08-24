package com.tort.mudai.mapper

import com.google.inject.{Injector, Guice}
import java.util.List
import java.io.File
import org.scalatest.{BeforeAndAfterEach, FeatureSpec}
import com.tort.mudai.RoomSnapshot

class MapperTest extends FeatureSpec with BeforeAndAfterEach {
  val room3: String = "room 3"

  feature("mapping") {
    scenario("map three locations, pass back, check way forth") {
      checkPathForth(mapper())
    }
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
    mapTerrain(mapper)

    val pathBack: List[Direction] = mapper.pathTo("room 1")
    assert(pathBack.size() == 2)
  }

  def moveBack(mapper: MapperImpl) {
    checkPathBack(mapper)

    mapper.move(Directions.NORTH.getName, "room 2")
    mapper.move(Directions.NORTH.getName, "room 1")
    assert(mapper.currentLocation().getTitle == "room 1")
  }

  def checkPathForth(mapper: MapperImpl) {
    moveBack(mapper)

    val pathForth: List[Direction] = mapper.pathTo(room3)
    assert(pathForth.size == 2)
  }

  def mapper(): MapperImpl = {
    val injector: Injector = Guice.createInjector(new MapperTestModule)
    val mapper: MapperImpl = injector.getInstance(classOf[MapperImpl])
    assert(mapper != null)

    mapper
  }

  override protected def afterEach() {
    val db: File = new File("mapper-test.db")
    if (db.exists())
      db.deleteOnExit()
  }
}