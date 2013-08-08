package com.tort.mudai.mapper

import scalax.collection.mutable.Graph
import scalax.collection.edge.Implicits._
import scalax.collection.edge.LDiEdge
import com.tort.mudai.Metadata.Direction._
class PathHelper(transitionPersister: TransitionPersister) {
  def pathTo(current: Option[Location], target: Location): List[Direction] = {
    current.flatMap(curr => pathTo(curr, target)).getOrElse(List())
  }

  private def pathTo(currentLocation: Location, target: Location): Option[List[Direction]] = {
    val graph = Graph.empty[String, LDiEdge]

    transitionPersister.allTransitions.foreach {
      case transition =>
        val edge = (transition.from.id ~+> transition.to.id)(transition.direction.id)
        graph += edge
    }

    def node(location: String) = graph.get(location)

    val shortest = node(currentLocation.id) shortestPathTo node(target.id)
    shortest.map(_.edges.map(_.label.toString).map(nameToDirection(_)))
  }
}
