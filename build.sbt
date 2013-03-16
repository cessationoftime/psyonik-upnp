/** Project */
name := "psyonik-upnp"

version := "0.0.1-SNAPSHOT"

organization := "com.psyonik"

scalaVersion := "2.10.0"

// Dependencies 
libraryDependencies ++= Seq(
	"org.specs2" % "specs2_2.10" % "1.14" % "test",
    "org.scalacheck" % "scalacheck_2.10" % "1.10.0"  % "test",
    "org.pegdown" % "pegdown" % "1.2.1" % "test",
	"org.mockito" % "mockito-all" % "1.9.0" % "test")
	
	testOptions in Test += Tests.Argument("html","console")
	
	testOptions in Test ++= Seq(Tests.Filter(s => Seq("Spec", "Unit","index").exists(s.endsWith(_))))
	
	traceLevel in run := 0 //for stacktraces in ~run command,  alternatively use "last run"
