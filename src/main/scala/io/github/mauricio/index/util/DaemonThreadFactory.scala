package io.github.mauricio.index.util

import java.util.concurrent.{Executors, ThreadFactory}

object DaemonThreadFactory extends ThreadFactory {

  override def newThread(r: Runnable): Thread = {
    val thread = Executors.defaultThreadFactory().newThread(r)
    thread.setDaemon(true)
    thread
  }

}
