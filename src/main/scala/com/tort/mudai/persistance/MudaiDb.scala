package com.tort.mudai.persistance

import org.squeryl.PrimitiveTypeMode._
import org.squeryl.{KeyedEntity, SessionFactory, Session, Schema}
import org.squeryl.annotations.Column
import org.squeryl.adapters.H2Adapter

object MudaiDb extends Schema with H2_Connection {
  val stats = table[Stat]

  private def updateStat(oldStat: Stat, newStat: Stat) {
    if(newStat.desc.length > oldStat.desc.length)
      stats.update(newStat)
  }

  def saveStat(stat: Stat) = transaction {
    from(stats)(s => where(s.name === stat.name) select(s)).headOption match {
      case None => stats.insert(stat)
      case Some(x) => updateStat(x, stat)
    }
  }

  SessionFactory.concreteFactory = connectToDb()
}

class Stat(val name: String, val desc: String) extends KeyedEntity[String] {
  @Column("name") val id: String = name
}

trait H2_Connection {
  def connectToDb() : Option[() => Session] = {
      Class.forName("org.h2.Driver")
      Some(() => {
        val c = java.sql.DriverManager.getConnection(
          "jdbc:h2:mudai;AUTO_SERVER=TRUE",
          "sa",
          ""
        )
        c.setAutoCommit(false)
        Session.create(c, new H2Adapter)
      })
  }
}