package com.tort.mudai.mapper

import scalaz.{@@, Equal, Tag}

sealed trait Direction

object Direction {
  def apply(dir: String) = Tag[String, Direction](dir)
  implicit val directionEqual: Equal[String @@ Direction] = Equal.equal(_ == _)

  val North = Direction("север")
  val South = Direction("юг")
  val East = Direction("восток")
  val West = Direction("запад")
  val Up = Direction("вверх")
  val Down = Direction("вниз")

  val aliasToDirection =
    Map("N" -> North,
      "n" -> North,
      "S" -> South,
      "s" -> South,
      "E" -> East,
      "e" -> East,
      "W" -> West,
      "w" -> West,
      "u" -> Up,
      "U" -> Up,
      "d" -> Down,
      "D" -> Down
    )

  val oppositeDirection = Map(
    North -> South,
    South -> North,
    East -> West,
    West -> East,
    Up -> Down,
    Down -> Up
  )
}
