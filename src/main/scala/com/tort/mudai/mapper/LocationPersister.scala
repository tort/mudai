package com.tort.mudai.mapper

import com.tort.mudai.RoomKey
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
}

trait TransitionPersister {
  def loadTransition(prev: Location, direction: Direction, newLocation: Location): Option[Transition]

  def loadTransition(current: Location, direction: Direction): Option[Location]

  def saveTransition(prev: Location, direction: Direction, newLocation: Location, isWeak: Boolean): Transition

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

  def saveLocation(room: RoomKey) = DB.db withSession {
    val id = util.UUID.randomUUID().toString
    val title = room.title
    val desc = room.desc
    sqlu"insert into location(id, title, desc) values($id, $title, $desc)".first
    Location(id, title, desc)
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

  def saveTransition(prev: Location, direction: Direction, newLocation: Location, isWeak: Boolean) = DB.db withSession {
    def id = util.UUID.randomUUID().toString
    val from = prev.id
    val dir = direction.id
    val to = newLocation.id
    val oppositeDir = oppositeDirection(nameToDirection(dir)).id
    sqlu"insert into transition(id, locFrom, direction, locTo, isWeak) values($id, $from, $dir, $to, $isWeak)".first
    sqlu"insert into transition(id, locFrom, direction, locTo, isWeak) values($id, $to, $oppositeDir, $from, $isWeak)".first
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
    Q.queryNA[(String, String, String, String, Boolean, String, String, String, String, Boolean)]("select tw.*, t.* " +
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
      (new Transition(x._1, loadLocation(x._2), Direction(x._3), loadLocation(x._4), x._5),
        new Transition(x._6, loadLocation(x._7), Direction(x._8), loadLocation(x._9), x._10)))
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
}

object DB {
  val DbUrl: String = "jdbc:h2:tcp://localhost:9092/~/workspace/mudai/mudai"
  val user = "sa"
  val password = ""
  val db: Database = Database.forURL(DbUrl, user, password, driver = "org.h2.Driver")
}
