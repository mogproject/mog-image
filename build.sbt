name := """mog-image"""
organization := "com.mogproject"

version := "1.0-SNAPSHOT"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .dependsOn(mogCore)

scalaVersion := "2.11.11"

libraryDependencies += filters
libraryDependencies ++= Seq(
  ws.exclude("commons-logging", "commons-logging"),
  "com.typesafe" % "config" % "1.3.1",
  "net.debasishg" %% "redisclient" % "3.4",
  "com.sksamuel.scrimage" %% "scrimage-core" % "2.1.7",
  "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0" % Test
)

addCommandAlias("run", """;+compile;set javaOptions += "-Dlogger.resource=/logback-dev.xml"; root/run""")

import AssemblyKeys._

assemblySettings

mainClass in assembly := Some("play.core.server.ProdServerStart")

fullClasspath in assembly += Attributed.blank(PlayKeys.playPackageAssets.value)

mergeStrategy in assembly := {
  case "META-INF/io.netty.versions.properties" => MergeStrategy.first
  case "org.apache.commons.logging.**" => MergeStrategy.first
  case x =>
    val oldStrategy = (mergeStrategy in assembly).value
    oldStrategy(x)
}

lazy val mogCore = ProjectRef(uri("git://github.com/mogproject/mog-core-scala.git#master"), "mogCoreJVM")
