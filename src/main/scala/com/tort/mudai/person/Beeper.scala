package com.tort.mudai.person

import akka.actor.Actor
import javax.sound.sampled.AudioSystem
import java.io.File
import com.tort.mudai.event.TellEvent

class Beeper extends Actor {
  def receive = {
    case e: TellEvent =>
      val clip = AudioSystem.getClip
      clip.open(AudioSystem.getAudioInputStream(new File("tell.wav")))
      clip.start()
  }
}
