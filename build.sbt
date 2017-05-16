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
  "com.github.detro" % "ghostdriver" % "2.0.0",
  "com.sksamuel.scrimage" %% "scrimage-core" % "2.1.7",
  "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0" % Test
)

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.mogproject.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.mogproject.binders._"
