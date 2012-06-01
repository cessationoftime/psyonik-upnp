/** Project */
name := "upnp"

version := "0.0.1-SNAPSHOT"

organization := "com.psyonik"

scalaVersion := "2.9.1"

// Dependencies 
resolvers ++= Seq(
	ScalaToolsSnapshots,
    //"scala-tools snapshots" at "http://scala-tools.org/repo-snapshots/",
    "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository",
	"external releases" at "http://redsoftserver/maven2_repo/external/",
	"external_free releases" at "http://redsoftserver/maven2_repo/external_free/",
	"inhouse snapshots" at "http://redsoftserver/maven2_repo/inhouse_snapshot/",
    "inhouse releases" at "http://redsoftserver/maven2_repo/inhouse/")

libraryDependencies ++= Seq(
    //"org.specs2" %% "specs2" % "1.7-SNAPSHOT",
	"org.specs2" %% "specs2" % "1.6.1" % "test",
    "org.specs2" %% "specs2-scalaz-core" % "6.0.1" % "test",
    "org.scala-tools.testing" %% "scalacheck" % "1.9" % "test",
    "org.pegdown" % "pegdown" % "1.0.2" % "test",
	//"com.borachio" %% "borachio-specs2-support" % "latest.integration",
	"org.scala-lang" % "scala-swing" % "2.9.1",
	"org.mockito" % "mockito-all" % "1.9.0-rc1",
	"org.scala-tools.subcut" %% "subcut" % "1.0-SNAPSHOT",
	"junit" % "junit" % "4.10" % "test"
	//"org.bitlet" % "weupnp" % "0.1.2-SNAPSHOT" % "test"
	)
	
	testOptions in Test += Tests.Argument("html","console")
	
	//testOptions in Test := Seq(Tests.Filter(s => s.endsWith("Test")))
	testOptions in Test ++= Seq(Tests.Filter(s => Seq("Spec", "Unit","index").exists(s.endsWith(_))))
	//testOptions in Test ++= Seq(Tests.Filter(s => Seq("index").exists(s.endsWith(_))))
	
	// traceLevel := 0,  If you want traces to be printed everywhere: (test as well as run, etc)
	traceLevel in run := 0 //for stacktraces in ~run command,  alternatively use "last run"
