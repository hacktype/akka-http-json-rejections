lazy val CommonSettings = Seq(
  organization := "com.github.hacktype",
  version := "0.1.0",
  scalaVersion := "2.11.4"
)

lazy val root = (project in file(".")).
  settings(CommonSettings: _*).
  settings(
    name := "AkkaHttp",
    libraryDependencies ++= {
      val akkaV       = "2.4.1"
      val akkaStreamV = "2.0.1"
      val scalaTestV  = "2.2.5"
      Seq(
        "com.typesafe.akka" %% "akka-actor"                           % akkaV,
        "com.typesafe.akka" %% "akka-stream-experimental"             % akkaStreamV,
        "com.typesafe.akka" %% "akka-http-core-experimental"          % akkaStreamV,
        "com.typesafe.akka" %% "akka-http-experimental"               % akkaStreamV,
        "com.typesafe.akka" %% "akka-http-spray-json-experimental"    % akkaStreamV,
        "com.typesafe.akka" %% "akka-http-testkit-experimental"       % akkaStreamV,
        "org.scalatest"     %% "scalatest"                            % scalaTestV % "test"
      )
    }
  )
