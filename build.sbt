import AssemblyKeys._ // put this at the top of the file

name := "mudai"

version := "0.1"

scalaVersion := "2.9.1"

fork := true


libraryDependencies += "com.google.inject" % "guice" % "3.0"

libraryDependencies += "com.google.inject.extensions" % "guice-assistedinject" % "3.0"

libraryDependencies += "com.assembla.scala-incubator" %% "graph-core" % "1.4.3"

libraryDependencies += "org.scalatest" % "scalatest_2.9.1" % "1.6.1"

libraryDependencies += "org.mockito" % "mockito-core" % "1.8.5"

libraryDependencies += "org.testng" % "testng" % "6.1.1"

libraryDependencies += "org.mozilla" % "rhino" % "1.7R4"

assemblySettings

test in assembly := {}
