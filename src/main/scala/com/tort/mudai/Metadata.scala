package com.tort.mudai

object Metadata {

  object Direction {
    val North = mapper.Direction("север")
    val South = mapper.Direction("юг")
    val East = mapper.Direction("восток")
    val West = mapper.Direction("запад")
    val Up = mapper.Direction("вверх")
    val Down = mapper.Direction("вниз")

    val aliasToDirection =
      Map("N" -> North,
        "n" -> North,
        "S" -> South,
        "s" -> South,
        "E" -> East,
        "e" -> East,
        "W" -> West,
        "w" -> West,
        "^" -> Up,
        "v" -> Down
      )

    val nameToDirection = Map(
      "север" -> North,
      "юг" -> South,
      "восток" -> East,
      "запад" -> West,
      "вверх" -> Up,
      "вниз" -> Down
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

}
