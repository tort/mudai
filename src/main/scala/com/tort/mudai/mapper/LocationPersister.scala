package com.tort.mudai.mapper

import com.tort.mudai.{RoomSnapshot, RoomKey}
import scalaz._
import Scalaz._
import scala.slick.session.Database
import Database.threadLocalSession
import scala.slick.jdbc.{StaticQuery => Q, GetResult}
import Q.interpolation
import java.util
import com.tort.mudai.Metadata.Direction._

trait LocationPersister {
  def loadLocation(id: String): Location

  def loadLocation(room: RoomKey): Seq[Location]

  def saveLocation(room: RoomKey): Location

  def allLocations: Seq[Location]

  def persistMobAndArea(mob: String, location: Location)

  def makeKillable(shortName: String)

  def killablesHabitation: Seq[Location]

  def mobByFullName(name: String): Option[Mob]

  def updateLocation(zone: String)(location: String)

  def nonBorderNeighbors(location: String): Set[Location]

  def zoneByName(zoneName: String): Zone
}

trait TransitionPersister {
  def loadTransition(prev: Location, direction: Direction, newLocation: Location): Option[Transition]

  def loadTransition(current: Location, direction: Direction): Option[Location]

  def saveTransition(prev: Location, direction: Direction, newLocation: Location, roomSnapshot: RoomSnapshot, isWeak: Boolean): Transition

  def allTransitions: Seq[Transition]

  def weakChainIntersection: Seq[(Transition, Transition)]

  def allWeakTransitions: Seq[Transition]

  def deleteWeakIntersection(locations: Iterable[Location], transitions: Seq[Transition])

  def replaceWeakWithStrong

  def updateToTransition(id: String, toLocId: String)

  def updateFromTransition(id: String, toLocId: String)
}

class SQLLocationPersister extends LocationPersister with TransitionPersister {
  implicit val getLocationResult = GetResult(l => Location(l.<<, l.<<, l.<<))
  implicit val getMobResult = GetResult(l => Mob(l.<<, l.<<, Option(l.<<), Option(l.<<), l.<<))
  implicit val getZoneResult = GetResult(z => new Zone(z.<<, z.<<))

  def locationByTitle(title: String): Seq[Location] = DB.db withSession {
    sql"select * from location where title like '%#$title%'".as[Location].list
  }

  def allLocations = DB.db withSession {
    sql"select * from location".as[Location].list
  }

  def allTransitions = DB.db withSession {
    sql"select id, locFrom, direction, locTo from transition".as[(String, String, String, String)]
      .list
      .map(x => new Transition(x._1, loadLocation(x._2), Direction(x._3), loadLocation(x._4)))
  }

  def loadLocation(room: RoomKey) = DB.db withSession {
    val title = room.title
    val desc = room.desc
    sql"select * from location l where l.title = $title and l.desc = $desc".as[Location].list
  }

  def loadLocation(id: String): Location = DB.db withSession {
    sql"select * from location l where l.id = $id".as[Location].firstOption match {
      case None => throw new RuntimeException("NO LOC FOUND FOR " + id)
      case Some(x) => x
    }
  }

  def generateId() = util.UUID.randomUUID().toString

  def saveLocation(room: RoomKey) = DB.db withSession {
    val title = room.title
    val desc = room.desc
    val newId = generateId
    sqlu"insert into location(id, title, desc) values($newId, $title, $desc)".first
    Location(newId, title, desc)
  }

  def loadTransition(prev: Location, direction: Direction, newLocation: Location) = DB.db withSession {
    val prevId = prev.id
    val dir = direction.id
    val newId = newLocation.id
    sql"select l.id, l.locFrom, l.direction, l.locTo from transition l where l.locFrom = $prevId and l.direction = $dir and l.locTo = $newId"
      .as[(String, String, String, String)]
      .list
      .map(x => new Transition(x._1, loadLocation(x._2), Direction(x._3), loadLocation(x._4))) match {
      case trans :: Nil => trans.some
      case _ => None
    }
  }

  def updateFromTransition(id: String, toLocId: String): Unit = DB.db withSession {
    sqlu"update transition set locFrom = $toLocId where id = $id".first
  }

  def updateToTransition(id: String, toLocId: String): Unit = DB.db withSession {
    sqlu"update transition set locTo = $toLocId where id = $id".first
  }

  def saveTransition(prev: Location, direction: Direction, newLocation: Location, roomSnapshot: RoomSnapshot, isWeak: Boolean) = DB.db withSession {
    def id = util.UUID.randomUUID().toString
    val from = prev.id
    val dir = direction.id
    val to = newLocation.id
    val oppositeDirId = oppositeDirection(nameToDirection(dir)).id
    val isBorder = roomSnapshot.exits.find(e => e.direction.id === oppositeDirId).exists(_.isBorder)
    sqlu"insert into transition(id, locFrom, direction, locTo, isWeak, isborder) values($id, $from, $dir, $to, $isWeak, $isBorder)".first
    sqlu"insert into transition(id, locFrom, direction, locTo, isWeak, isborder) values($id, $to, $oppositeDirId, $from, $isWeak, $isBorder)".first
    new Transition(id, prev, direction, newLocation)
  }

  def loadTransition(current: Location, direction: Direction): Option[Location] = DB.db withSession {
    val loc = current.id
    val dir = direction.id
    sql"select l.* from transition t join location l on t.locTo = l.id where t.locFrom = $loc and t.direction = $dir"
      .as[Location]
      .list match {
      case Nil => None
      case l :: Nil => l.some
    }
  }

  def weakChainIntersection: Seq[(Transition, Transition)] = DB.db withSession {
    Q.queryNA[(String, String, String, String, Boolean, Boolean, String, String, String, String, Boolean, Boolean)]("select tw.*, t.* " +
      "from transition t join location lf on t.locFrom = lf.id join location lt on t.locTo = lt.id " +
      "join transition tw join location lfw on tw.locFrom = lfw.id join location ltw on tw.locTo = ltw.id " +
      "where t.isweak = 0 " +
      "and tw.isweak = 1 " +
      "and lf.title = lfw.title " +
      "and lf.desc = lfw.desc " +
      "and lt.title = ltw.title " +
      "and lt.desc = ltw.desc " +
      "and t.direction = tw.direction ").list
      .map(x =>
      (new Transition(x._1, loadLocation(x._2), Direction(x._3), loadLocation(x._4), x._5, x._6),
        new Transition(x._7, loadLocation(x._8), Direction(x._9), loadLocation(x._10), x._11, x._12)))
  }

  def allWeakTransitions: Seq[Transition] = DB.db withSession {
    sql"select * from transition where isweak = 1"
      .as[(String, String, String, String, Boolean)]
      .list
      .map(x => new Transition(x._1, loadLocation(x._2), Direction(x._3), loadLocation(x._4), x._5))
  }

  def deleteWeakIntersection(locations: Iterable[Location], transitions: Seq[Transition]): Unit = DB.db withSession {
    val tids = transitions.map("'" + _.id + "'").mkString(",")
    val lids = locations.map("'" + _.id + "'").mkString(",")
    sqlu"delete from transition where id in (#$tids)".first
    sqlu"delete from location where id in (#$lids)".first
  }

  def replaceWeakWithStrong: Unit = DB.db withSession {
    sqlu"update transition set isweak = 0 where isweak = 1".first
  }

  def mobByShortName(name: String) = DB.db withSession {
    sql"select m.id, m.fullname, m.shortname, m.alias, m.iskillable from mob m where m.shortName = $name".as[Mob].firstOption
  }

  def mobByFullName(name: String): Option[Mob] = DB.db withSession {
    val mob = sql"select m.id, m.fullname, m.shortname, m.alias, m.iskillable from mob m where m.fullName = $name".as[Mob].firstOption
    mob match {
      case None =>
        val id = generateId()
        val alias: String = null
        val shortName: String = null
        val fullName = name
        sqlu"insert into mob(id, alias, shortName, fullName) values($id, $alias, $shortName, $fullName)".first
        Some(Mob(id, fullName, Option(alias), Option(shortName), killable = false))
      case mob => mob
    }
  }

  def makeKillable(shortName: String) = DB.db withSession {
    mobByShortName(shortName) match {
      case Some(mob) =>
        val mobId = mob.id
        sqlu"update mob set iskillable = 1 where id = $mobId".first
      case _ =>
    }
  }

  def persistMobAndArea(mobName: String, location: Location) = DB.db withSession {
    mobByFullName(mobName) match {
      case Some(mob) =>
        val locId = location.id
        val mobId = mob.id
        val count = sql"select count(*) from habitation h join mob m on m.id = h.mob where m.id = $mobId and h.location = $locId".as[Int].first
        count match {
          case 0 =>
            sqlu"insert into habitation(id, mob, location) values($generateId, $mobId, $locId)".first
          case x if x > 0 =>
        }
      case None =>
    }
  }

  def killablesHabitation = DB.db withSession {
    sql"select distinct l.* from habitation h join mob m on h.mob = m.id join location l on h.location = l.id where m.iskillable = 1".as[Location].list
  }

  def nonBorderNeighbors(location: String) = DB.db withSession {
    val locFrom = location
    sql"select lt.* from location lf join transition t on t.locfrom = lf.id join location lt on t.locto = lt.id where lf.id = $locFrom and t.isborder = 0".as[Location].list().toSet
  }

  def updateLocation(zoneId: String)(location: String) = DB.db withSession {
    val locId = location
    sqlu"update location set zone = $zoneId where id = $locId".first
  }

  def zoneByName(zoneName: String) = DB.db withSession {
    sql"select * from zone where name = $zoneName".as[Zone].firstOption match {
      case None =>
        val newId = generateId()
        sqlu"insert into zone(id, name) values($newId, $zoneName)".first
        new Zone(newId, zoneName)
      case Some(zone) =>
        zone
    }
  }
}

object DB {
  val DbUrl: String = "jdbc:h2:tcp://localhost:9092/~/workspace/mudai/mudai"
  val user = "sa"
  val password = ""
  val db: Database = Database.forURL(DbUrl, user, password, driver = "org.h2.Driver")
}
