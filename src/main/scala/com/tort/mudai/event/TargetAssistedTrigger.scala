package com.tort.mudai.event

class TargetAssistedTrigger extends EventTrigger[TargetAssistedEvent] {
  val Pattern = ("(?ms).*\n(.*) вступил[ао]? в битву на стороне .*").r

  def matches(text: String) = text.matches(Pattern.toString)

  def fireEvent(text: String) = {
    val Pattern(target) = text
    println("NEW")
    println("FIRE TARGET ASSISTED !%s!".format(target))

    TargetAssistedEvent(target)
  }
}

case class TargetAssistedEvent(target: String) extends Event
