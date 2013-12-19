import AssemblyKeys._ // put this at the top of the file

name := "mudai"

version := "0.1"

scalaVersion := "2.10.2"

fork := true


libraryDependencies += "com.google.inject" % "guice" % "3.0"

libraryDependencies += "com.assembla.scala-incubator" % "graph-core_2.10" % "1.6.1"

libraryDependencies += "net.sf.jgrapht" % "jgrapht" % "0.8.3"

libraryDependencies += "org.scalatest" % "scalatest_2.10" % "1.9.1"

libraryDependencies += "com.h2database" % "h2" % "1.3.168"

libraryDependencies += "io.netty" % "netty" % "3.6.0.Final"

libraryDependencies += "org.scalaz" % "scalaz-core_2.10" % "7.0.5"

libraryDependencies += "com.typesafe.akka" % "akka-actor_2.10" % "2.2.1"

libraryDependencies += "com.typesafe.akka" % "akka-testkit_2.10" % "2.2.1"

libraryDependencies += "com.typesafe" % "slick_2.10" % "1.0.0-RC2"

assemblySettings

test in assembly := {}

retrieveManaged := true

resolvers += "typesafe" at "http://repo.typesafe.com/typesafe/releases"
