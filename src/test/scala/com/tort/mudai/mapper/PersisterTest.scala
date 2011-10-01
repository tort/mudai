package com.tort.mudai.mapper

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{BeforeAndAfterEach, FeatureSpec}

trait PersisterTest extends FeatureSpec
with ShouldMatchers
with BeforeAndAfterEach
with MapperTestHelpers {
  protected def createPersister: Persister

  var persister: Persister = _

  def persistRandomMob() {
    persister.persistMob(randomMob)
  }

  feature("persister") {
    scenario("persist locations, enlist, match") {
      val location = randomLocation
      persister.persistLocation(location)

      persister.enlistLocations should have size 1
    }

    scenario("persist same location twice") {
      val location = randomLocation
      persister.persistLocation(location)
      persister.persistLocation(location)

      persister.enlistLocations should have size 2
    }

    scenario("persist equal locations twice") {
      persister.persistLocation(new Location("title", "desc", Set[Directions]()))
      persister.persistLocation(new Location("title", "desc", Set[Directions]()))

      persister.enlistLocations should have size 2
    }

    scenario("load unknown location") {
      val locations = persister.loadLocations(randomLocation) //TODO refactor to load by room key

      locations should be('empty)
    }

    scenario("load many locations") {
      persistRandomLocation()
      persistRandomLocation()
      persistRandomLocation()

      val locations = persister.enlistLocations

      locations should have size 3
    }

    scenario("load locations by title") {
      persister.persistLocation(Location("title", "desc", Set[Directions]()))
      persistRandomLocation()
      persistRandomLocation()

      val locations = persister.loadLocations("title")

      locations should have size 1
    }

    scenario("persist mobs, enlist, match") {
      persistRandomMob()
      persistRandomMob()

      val mobs = persister.enlistMobs

      mobs should have size 2
    }

    scenario("load location by custom condition") {
      persistRandomLocation()
      persistRandomLocation()
      val location = persistRandomLocation()
      location.isTavern = true

      val locations = persister.locationsByCondition(_.isTavern == true)

      locations should have size 1
    }

    scenario("find mob by name") {
      val mobName = "mob"

      persistRandomMob()
      persistRandomMob()
      persister.persistMob(Mob(mobName, "desc", Set()))

      val mob = persister.tryFindMob(mobName).get

      mob should not be null
      mob.name should be(mobName)
    }
  }

  override protected def beforeEach() {
    persister = createPersister
  }

  def persistRandomLocation() = {
    val location = randomLocation
    persister.persistLocation(location)
    location
  }
}

class MemPersisterTest extends PersisterTest {
  def createPersister = new MemPersister()
}