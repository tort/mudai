package com.tort.mudai.mapper

case class Mob(name: String,
               descName: String = null,
               habitationArea: Set[Location] = Set()) {

  def getDescName = {
    if (descName != null) descName
    else name
  }

  def habitationArea(value: Set[Location]) = copy(habitationArea = value)

  def descName(value: String): Mob = copy(descName = value)
}
