import AssemblyKeys._ // put this at the top of the file

name := "mudai"

version := "0.1"

scalaVersion := "2.10.0"

fork := true


libraryDependencies += "com.google.inject" % "guice" % "3.0"

libraryDependencies += "com.google.inject.extensions" % "guice-assistedinject" % "3.0"

libraryDependencies += "com.assembla.scala-incubator" % "graph-core_2.10" % "1.6.1"

libraryDependencies += "org.scalatest" % "scalatest_2.10" % "1.9.1"

libraryDependencies += "org.mockito" % "mockito-core" % "1.8.5"

libraryDependencies += "org.testng" % "testng" % "6.1.1"

libraryDependencies += "org.mozilla" % "rhino" % "1.7R4"

libraryDependencies += "com.h2database" % "h2" % "1.3.168"

libraryDependencies += "io.netty" % "netty" % "3.6.0.Final"

libraryDependencies += "org.squeryl" % "squeryl_2.10" % "0.9.5-6"

libraryDependencies += "org.scalaz" % "scalaz-core_2.10" % "7.0.0-RC2"

libraryDependencies += "com.typesafe.akka" % "akka-actor_2.10" % "2.2-M3"

libraryDependencies += "com.typesafe.akka" % "akka-testkit_2.10" % "2.2-M3"

assemblySettings

test in assembly := {}
