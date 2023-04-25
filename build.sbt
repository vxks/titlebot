ThisBuild / version := "0.1-SNAPSHOT"

ThisBuild / scalaVersion := "3.2.2"

val zioVersion = "2.0.13"

name := "titlebot-backend"
libraryDependencies := Seq(
  "dev.zio"     %% "zio"               % zioVersion,
  "dev.zio"     %% "zio-http"          % "3.0.0-RC1",
  "dev.zio"     %% "zio-json"          % "0.5.0",
  "dev.zio"     %% "zio-test"          % zioVersion % Test,
  "dev.zio"     %% "zio-test-sbt"      % zioVersion % Test,
  "dev.zio"     %% "zio-test-magnolia" % zioVersion % Test,
  "com.lihaoyi" %% "requests"          % "0.8.0"
)

testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")

//Compile / mainClass    := Some("com.vxksoftware.Titlebot")
dockerExposedPorts     := Seq(8080)
Docker / daemonUserUid := None
Docker / daemonUser    := "daemon"

// Docker
enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)
