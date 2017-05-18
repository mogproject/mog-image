name := """mog-image"""
organization := "com.mogproject"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.11"

resolvers += "jitpack" at "https://jitpack.io" // for ghostdriver

libraryDependencies += filters
libraryDependencies ++= Seq(
  "com.typesafe" % "config" % "1.3.1",
  "net.debasishg" %% "redisclient" % "3.4",
  ("com.github.detro" % "ghostdriver" % "2.0.0")
    .exclude("commons-logging", "commons-logging"),
  "com.sksamuel.scrimage" %% "scrimage-core" % "2.1.7",
  "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0" % Test
)

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

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.mogproject.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.mogproject.binders._"
