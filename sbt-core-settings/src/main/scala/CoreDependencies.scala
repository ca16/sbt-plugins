package org.allenai.sbt.core

import sbt._
import sbt.Keys._

/** Declares common dependencies with specific versions to encourage
  * consistency accross projects
  */
object CoreDependencies {
  val slf4jVersion = "1.7.7"
  // The logging API to use. This should be the only logging dependency of any API artifact
  // (anything that's going to be depended on outside of this SBT project).
  val slf4jApi = "org.slf4j" % "slf4j-api" % slf4jVersion
  // Removes all unneeded log4j & slf4j implementations from the given modules. Adds in a single
  // slf4j implementation (logback), and the log4j -> slf4j bridge.
  // This should be called on libraryDependencies like:
  // addLoggingDependencies(libraryDependencies)
  def addLoggingDependencies(deps: SettingKey[Seq[ModuleID]]): Seq[Setting[Seq[ModuleID]]] = {
    val cleanedDeps = deps ~= { seq =>
      seq map { module =>
        // Exclude the transitive dependencies that might mess things up for us.
        // slf4j replaces log4j.
        (module exclude("log4j", "log4j")
           // We're using logback as the slf4j implementation, and we're providing it below.
           exclude("org.slf4j", "slf4j-log4j12")
           exclude("org.slf4j", "slf4j-jdk14")
           exclude("org.slf4j", "slf4j-jcl")
           exclude("org.slf4j", "slf4j-simple")
           // We'll explicitly provide the logback version; this avoids having to do an override.
           exclude("ch.qos.logback", "logback-core")
           exclude("ch.qos.logback", "logback-classic"))
      }
    }
    // Now, add the logging libraries.
    val logbackDeps = deps ++= Seq(
      slf4jApi,
      // Bridge log4j logging to slf4j.
      "org.slf4j" % "log4j-over-slf4j" % slf4jVersion,
      // Use logback for the implementation.
      "ch.qos.logback" % "logback-core" % "1.1.2",
      "ch.qos.logback" % "logback-classic" % "1.1.2")
    Seq(cleanedDeps, logbackDeps)
  }

  val scopt = "com.github.scopt" % "scopt_2.10" % "3.2.0"
  val typesafeConfig = "com.typesafe" % "config" % "1.2.0"

  // Akka
  val akkaVersion = "2.3.5"
  def akkaModule(id: String) = "com.typesafe.akka" %% s"akka-$id" % akkaVersion
  val akkaActor = akkaModule("actor")
  val akkaLogging = akkaModule("slf4j")

  // Spray
  val sprayVersion = "1.3.1"
  def sprayModule(id: String) = "io.spray" % s"spray-$id" % sprayVersion
  val sprayCan = sprayModule("can")
  val sprayRouting = sprayModule("routing")
  val sprayClient = sprayModule("client")
  val sprayTestkit = sprayModule("testkit")
  val sprayCaching = sprayModule("caching")

  // Spray json (separate from Spray toolkit)
  val sprayJson = "io.spray" %%  "spray-json" % "1.2.6"

}
