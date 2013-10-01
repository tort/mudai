package com.tort.mudai.mapper

import scalax.collection.mutable.Graph
import scalax.collection.edge.Implicits._
import scalax.collection.edge.LDiEdge
import com.tort.mudai.mapper.Direction._
import scalaz.@@

class PathHelper(transitionPersister: TransitionPersister) {
  def pathTo(current: Option[Location], target: Location): List[String @@ Direction] = {
    println(s"PATH-TO: ${current.map(_.title)} -> ${target.title}")
    current.flatMap(curr => pathTo(curr, target)).getOrElse(List())
  }

  private def pathTo(currentLocation: Location, target: Location): Option[List[String @@ Direction]] = {
    val graph = Graph.empty[String, LDiEdge]

    transitionPersister.allTransitions.foreach {
      case transition =>
        val edge = (transition.from.id ~+> transition.to.id)(transition.direction)
        graph += edge
    }

    def node(location: String) = graph.get(location)

    val shortest = node(currentLocation.id) shortestPathTo node(target.id)
    shortest.map(_.edges.map(e => Direction(e.label.toString)))
  }
}
