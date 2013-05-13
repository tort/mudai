package com.tort.mudai.person

import akka.actor.ActorSystem
import akka.actor.Props
import akka.testkit.{TestActorRef, TestProbe, TestKit, ImplicitSender}
import org.scalatest.{BeforeAndAfter, WordSpec, BeforeAndAfterAll}
import org.scalatest.matchers.MustMatchers
import scala.concurrent.duration._

class PersonTest(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
  with WordSpec with MustMatchers with BeforeAndAfterAll with BeforeAndAfter {

  def this() = this(ActorSystem())

  override def afterAll() {
    system.shutdown()
  }

  "person must" must {
    "snoop" in {
      val person = TestActorRef[Person]
      val probe = TestProbe()
      val adapter = TestProbe()

      probe.send(person, Snoop(probe.ref))
      adapter.send(person, RawRead("test"))

      probe.expectMsgClass(classOf[RawRead])
    }

    "forgive multiple snooping attempts" in {
      val person = TestActorRef[Person]
      val probe = TestProbe()
      val adapter = TestProbe()

      probe.send(person, Snoop)
      probe.send(person, Snoop)
      adapter.send(person, RawRead("test"))

      probe.receiveOne(50 millis)
      probe.expectNoMsg(10 millis)
    }

    "stop snooping" in {
      val person = TestActorRef[Person]
      val probe = TestProbe()
      val adapter = TestProbe()

      probe.send(person, Snoop)
      adapter.send(person, RawRead("test"))
      probe.send(person, StopSnoop)
      adapter.send(person, RawRead("test"))

      probe.receiveOne(50 millis)
      probe.expectNoMsg(10 millis)
    }

    "log to file" in {
      fail("not implemented")
    }
  }
}
