package com.tort.mudai.task

import com.tort.mudai.Handler

import java.util.concurrent.atomic.AtomicReference
import actors.Actor

class EventDistributor {

  private val tasks: AtomicReference[Seq[AbstractTask]] = new AtomicReference(Seq[AbstractTask]())
  private val actors: AtomicReference[Seq[Actor]] = new AtomicReference(Seq[Actor]())

  def invoke(handler: Handler) {
    for (task <- tasks.get()) {
      handler.handle(task)
    }
  }

  def subscribe(task: AbstractTask) {
    tasks.getAndSet(tasks.get() :+ task)
  }

  def programmerError(e: Throwable) {
    invoke(new Handler() {
      override def handle(task: AbstractTask) {
        task.programmerError(e)
      }
    })
  }

  def adapterException(e: Exception) {
    invoke(new Handler() {
      override def handle(task: AbstractTask) {
        task.adapterException(e)
      }
    });
  }

  def connectionClose() {
    invoke(new Handler() {
      override def handle(task: AbstractTask) {
        task.connectionClosed();
      }
    })
  }

  def getTargets = tasks.get

  def getActorTargets = actors.get

  def rawReadEvent(ga_block: String) {
    invoke(new Handler() {
      override def handle(task: AbstractTask) {
        task.rawRead(ga_block);
      }
    })
  }

  def unsubscribe(task: Task) {
    tasks.getAndSet(tasks.get().filterNot(_ == task))
  }
}
