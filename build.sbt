ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.14"

libraryDependencies ++= Seq(
  "com.github.mjakubowski84" %% "parquet4s-core" % "2.23.0",
  "org.apache.hadoop" % "hadoop-common" % "3.3.6",
  "org.apache.spark" %% "spark-core" % "3.5.1",
  "org.apache.spark" %% "spark-sql"  % "3.5.1"
)


lazy val root = (project in file("."))
  .settings(
    name := "Fruit-Freshness"
  )
