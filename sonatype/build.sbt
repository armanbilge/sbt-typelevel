addSbtPlugin("com.typesafe" % "sbt-mima-plugin" % "1.0.1")
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "3.9.10")
lazy val http4sV = "0.23.9"
libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-ember-client" % http4sV,
  "org.http4s" %% "http4s-circe" % http4sV
)
