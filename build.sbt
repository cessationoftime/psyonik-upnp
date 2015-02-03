/** Project */
name := "psyonik-upnp"

version := "0.0.1-SNAPSHOT"

organization := "com.psyonik"

scalaVersion := "2.11.5"

// Dependencies 
libraryDependencies ++= Seq(
	"org.specs2" %% "specs2" % "2.4.16" % "test",
    "org.scalacheck" %% "scalacheck" % "1.12.1"  % "test",
    "org.pegdown" % "pegdown" % "1.2.1" % "test",
	"org.scala-lang.modules" %% "scala-xml" % "1.0.3",
	"org.mockito" % "mockito-all" % "1.9.0" % "test")
	
	testOptions in Test += Tests.Argument("html","console")
	
	testOptions in Test ++= Seq(Tests.Filter(s => Seq("Spec", "Unit","index").exists(s.endsWith(_))))
	
	traceLevel in run := 0 //for stacktraces in ~run command,  alternatively use "last run"
