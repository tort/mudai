package com.tort.mudai.mapper

import scalaz.@@
import com.tort.mudai.mapper.Location.LocationId

trait ReachabilityHelper {
  def reachableFrom(l: Location, neighbors: (String @@ LocationId) => Set[Location], exclude: Set[Location] = Set()): Set[String @@ LocationId] = {
    def internal(visited: Set[String @@ LocationId])(loc: String @@ LocationId): Set[String @@ LocationId] = {
      val neighborsFound: Set[Location] = neighbors(loc).filterNot(l => exclude.contains(l))
      (neighborsFound.map(_.id) -- visited).flatMap(internal(visited ++ neighborsFound.map(_.id) + loc)) + loc
    }

    internal(Set())(l.id)
  }
}
