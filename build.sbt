ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.8.1"

libraryDependencies += "org.pan-data" %% "pan-data" % "0.0.1"

lazy val root = (project in file("."))
  .settings(
    name := "Fruit-Freshness"
  )
