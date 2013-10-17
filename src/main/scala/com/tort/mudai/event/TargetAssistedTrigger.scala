package com.tort.mudai.event

import com.tort.mudai.mapper.Mob._
import scalaz.@@

class TargetAssistedTrigger extends EventTrigger[TargetAssistedEvent] {
  val Pattern = "(?ms).*\n(.*) вступил[ао]? в битву на стороне (.*).".r

  def matches(text: String) = text.matches(Pattern.toString())

  def fireEvent(text: String) = {
    val Pattern(assister, target) = text

    TargetAssistedEvent(shortName(assister), genitive(target))
  }
}

case class TargetAssistedEvent(assister: String @@ ShortName, target: String @@ Genitive) extends Event
