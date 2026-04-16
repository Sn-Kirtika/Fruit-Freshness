ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.8.1"

libraryDependencies ++= Seq(
  "com.github.mjakubowski84" %% "parquet4s-core" % "2.23.0",
  "org.apache.hadoop" % "hadoop-common" % "3.3.6"
)

lazy val root = (project in file("."))
  .settings(
    name := "Fruit-Freshness"
  )
