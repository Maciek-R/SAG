name := "SAG"

version := "0.1"

scalaVersion := "2.11.11"

//resolvers += "bintray-spark-packages" at "https://dl.bintray.com/spark-packages/maven/"
//addSbtPlugin("org.spark-packages" % "sbt-spark-package" % "0.2.6")


val sparkVersion = "2.3.0"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.5.12",
  "com.typesafe.akka" %% "akka-testkit" % "2.5.12" % Test,

  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-streaming" % sparkVersion,
  "org.apache.spark" %% "spark-sql" % sparkVersion,
  "org.apache.spark" %% "spark-hive" % sparkVersion,
  "org.apache.spark" %% "spark-repl" % sparkVersion,
  "org.apache.spark" %% "spark-mllib" % sparkVersion
)