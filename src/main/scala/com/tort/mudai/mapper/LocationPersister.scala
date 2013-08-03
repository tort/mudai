package com.tort.mudai.mapper

import com.tort.mudai.RoomKey
import scalaz._
import Scalaz._
import scala.slick.session.Database
import Database.threadLocalSession
import scala.slick.jdbc.{StaticQuery => Q, GetResult}
import Q.interpolation
import java.util

trait LocationPersister {
  def loadLocation(room: RoomKey): Seq[Location]

  def saveLocation(room: RoomKey): Location

  def allLocations: Seq[Location]
}

trait TransitionPersister {
  def loadTransition(prev: Location, direction: Direction, newLocation: Location): Option[Transition]

  def saveTransition(prev: Location, direction: Direction, newLocation: Location): Transition

  def allTransitions: Seq[Transition]
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
    sql"select * from location l where l.id = $id".as[Location].first
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

  def saveTransition(prev: Location, direction: Direction, newLocation: Location) = DB.db withSession {
    val id = util.UUID.randomUUID().toString
    val from = prev.id
    val dir = direction.id
    val to = newLocation.id
    sqlu"insert into transition(id, locFrom, direction, locTo) values($id, $from, $dir, $to)".first
    new Transition(id, prev, direction, newLocation)
  }
}

object DB {
  val DbUrl: String = "jdbc:h2:tcp://localhost:9092/~/workspace/mudai/mudai"
  val user = "sa"
  val password = ""
  val db: Database = Database.forURL(DbUrl, user, password, driver = "org.h2.Driver")
}
