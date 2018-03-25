import com.typesafe.sbt.packager.SettingsHelper.makeDeploymentSettings
import sbt.inConfig

val commonSettings = Seq(
  organization := "com.evojam",

  scalacOptions ++= Seq(
    "-deprecation",
    "-encoding", "UTF-8",
    "-feature",
    "-unchecked",
    "-Xfatal-warnings",
    "-Ywarn-dead-code",
    "-Ywarn-value-discard",
    "-Xfuture"
  ),

  coverageMinimum := 90,
  coverageFailOnMinimum := true,
  coverageHighlighting := true,

  dockerBaseImage := "williamyeh/java8:latest",
  maintainer := "EVOJAM Team <dl-EVOJAM-TEAMevojam.com>",
  dockerRepository := Some("localhost"),
  dockerUsername := Some("evojam"),
  dockerUpdateLatest := true,

  publishMavenStyle := true,
  publishArtifact in Compile := true,
  publishArtifact in Test := false,
  publishArtifact in Universal := true,

  executableScriptName := "run",

  publish := publish.dependsOn(publish in Universal).value,
  publishLocal := publishLocal.dependsOn(publishLocal in Universal).value
) ++ inConfig(Compile)(
  compile := compile.dependsOn(scalafmt).value
) ++ inConfig(Test)(
  test := test.dependsOn(scalafmt).value
)

val disableDockerSettings = Seq(
  publishLocal in Docker := {},
  publish in Docker := {}
)

val `scala_2.10` = "2.10.7"
val `scala_2.11` = "2.11.12"
val `scala_2.12` = "2.12.5"

lazy val `evojam-invitation-service-api` = project
  .enablePlugins(ScalafmtPlugin)
  .settings(commonSettings: _*)
  .settings(disableDockerSettings: _*)
  .settings(
    name := "evojam-invitation-service-api",
    scalaVersion := `scala_2.10`,
    crossScalaVersions := Seq(`scala_2.10`, `scala_2.11`, `scala_2.12`),
    libraryDependencies ++= Dependencies.invitationApi
  )

lazy val `evojam-invitation-service-api_2.10` = `evojam-invitation-service-api`.cross(`scala_2.10`)
lazy val `evojam-invitation-service-api_2.11` = `evojam-invitation-service-api`.cross(`scala_2.11`)
lazy val `evojam-invitation-service-api_2.12` = `evojam-invitation-service-api`.cross(`scala_2.12`)

lazy val `evojam-invitation-service` = project
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(UniversalPlugin, UniversalDeployPlugin)
  .enablePlugins(ScalafmtPlugin)
  .enablePlugins(BuildInfoPlugin)
  .settings(commonSettings: _*)
  .settings(
    name := "evojam-invitation-service",
    scalaVersion := `scala_2.12`,
    crossScalaVersions := Seq(`scala_2.12`),
    libraryDependencies ++= Dependencies.invitationService,

    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion, licenses),
    buildInfoKeys ++= Seq[BuildInfoKey](
      "team" -> "EVOJAM TEAM",
      "teamPage" -> "https://wiki.evojam/evojam-invitation-service",
      "teamEmail" -> "dl-EVOJAM-TEAMevojam.com",
      "projectUrl" -> "https://bitbucket.evojam/evojam-services/evojam-invitation-service"
    ),
    buildInfoPackage := "com.evojam.invitationservice.config",
    buildInfoOptions += BuildInfoOption.ToMap,

    Revolver.enableDebugging(port = 5005, suspend = false),

    makeDeploymentSettings(Universal, packageBin in Universal, "zip")
  ).dependsOn(`evojam-invitation-service-api_2.12`)

lazy val root = project.in(file("."))
  .enablePlugins(CrossPerProjectPlugin)
  .settings(
    organization := "com.evojam",
    name := "evojam-services"
  )
  .settings(disableDockerSettings: _*)
  .aggregate(
    `evojam-invitation-service-api`,
    `evojam-invitation-service`
  )
