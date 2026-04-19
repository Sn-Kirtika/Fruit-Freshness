ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.16"

libraryDependencies ++= Seq(
  "com.github.mjakubowski84" %% "parquet4s-core" % "2.13.0",
  "org.apache.hadoop" % "hadoop-common" % "3.3.6",
  "org.apache.hadoop" % "hadoop-mapreduce-client-core" % "3.3.6",
  "org.apache.hadoop" % "hadoop-client" % "3.3.6",

)
libraryDependencies += "com.microsoft.onnxruntime" % "onnxruntime" % "1.17.1"
lazy val root = (project in file("."))
  .settings(
    name := "Fruit-Freshness"
  )
