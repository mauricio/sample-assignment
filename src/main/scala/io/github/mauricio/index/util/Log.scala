package io.github.mauricio.index.util

import org.slf4j.LoggerFactory

object Log {

  def get[T](implicit tag: reflect.ClassTag[T]) =
    LoggerFactory.getLogger(tag.runtimeClass.getName)

}