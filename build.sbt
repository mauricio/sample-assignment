name := "digital-ocean"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies += "io.netty" % "netty-all" % "4.0.36.Final"

libraryDependencies += "org.specs2" %% "specs2-core" % "3.7.2" % Test

scalacOptions in Test ++= Seq("-Yrangepos")

enablePlugins(JavaAppPackaging)
