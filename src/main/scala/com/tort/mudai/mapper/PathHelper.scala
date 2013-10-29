package com.tort.mudai.mapper

import com.tort.mudai.mapper.Direction._
import scalaz._
import Scalaz._
import org.jgrapht.alg.DijkstraShortestPath
import org.jgrapht.graph.DefaultEdge

trait PathHelper {
  def pathTo(current: Option[Location], target: Location): List[String @@ Direction]
}

class ScalaGraphPathHelper(transitionPersister: TransitionPersister) extends PathHelper {

  import scalax.collection.mutable.Graph
  import scalax.collection.edge.Implicits._
  import scalax.collection.edge.LDiEdge

  def pathTo(current: Option[Location], target: Location): List[String @@ Direction] = {
    println(s"PATH-TO: ${current.map(_.title)} -> ${target.title}")
    current.flatMap(curr => pathTo(curr, target)).getOrElse(List())
  }

  private def pathTo(currentLocation: Location, target: Location): Option[List[String @@ Direction]] = {
    val graph = Graph.empty[String, LDiEdge]

    transitionPersister.allTransitions.foreach {
      case transition =>
        val fromId: String = transition.from.id
        val toId: String = transition.to.id
        val edge = (fromId ~+> toId)(transition.direction)
        graph += edge
    }

    def node(location: String) = graph.get(location)

    val shortest = node(currentLocation.id) shortestPathTo node(target.id)
    shortest.map(_.edges.map(e => Direction(e.label.toString)))
  }
}

class JGraphtPathHelper(transitionPersister: TransitionPersister) extends PathHelper {

  import org.jgrapht._
  import org.jgrapht.graph._
  import scala.collection.JavaConverters._

  def pathTo(current: Option[Location], target: Location): List[String @@ Direction] = {
    println(s"PATH-TO: ${current.map(_.title)} -> ${target.title}")
    current.flatMap(curr => pathTo(curr, target)).getOrElse(List())
  }

  private def pathTo(currentLocation: Location, target: Location): Option[List[String @@ Direction]] = {
    val graph = new DefaultDirectedGraph[String, LabeledEdge](classOf[LabeledEdge])

    transitionPersister.allTransitions.foreach {
      case transition =>
        val fromId: String = transition.from.id
        val toId: String = transition.to.id
        graph.addVertex(fromId)
        graph.addVertex(toId)
        graph.addEdge(fromId, toId, new LabeledEdge(transition.direction))
    }

    DijkstraShortestPath.findPathBetween(graph, currentLocation.id, target.id) match {
      case null => None
      case path =>
        path.asScala.toList.map(e => Direction(e.label)).some
    }
  }
}

class LabeledEdge(val label: String) extends DefaultEdge
