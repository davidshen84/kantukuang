import sbt._
import sbt.Keys._

import android.Keys._
import android.Dependencies.{apklib,aar}


android.Plugin.androidBuild

platformTarget in Android := "android-19"

minSdkVersion in Android := 9

targetSdkVersion in Android := 19

resolvers +=  "sonatype" at "https://oss.sonatype.org/content/repositories/snapshots/"

name := "app"

version := "1.0"

scalaVersion := "2.10.3"

libraryDependencies ++= Seq(
  "com.android.support" % "support-v4" % "19.1.0",
  aar("com.android.support" % "appcompat-v7" % "19.1.0"),
  "com.nostra13.universalimageloader" % "universal-image-loader" % "1.9.1",
  apklib("com.viewpagerindicator" % "library" % "2.4.1"),
  "com.google.guava" % "guava" % "17.0",
  "com.squareup" % "otto" % "1.3.4",
  aar("com.github.castorflex.smoothprogressbar" % "library" % "0.5.1"),
  aar("com.github.chrisbanes.actionbarpulltorefresh" % "library" % "0.9.9"),
  aar("com.github.chrisbanes.actionbarpulltorefresh" % "extra-abc" % "0.9.9"),
  aar("com.github.chrisbanes.photoview" % "library" % "1.2.3"),
  "org.jsoup" % "jsoup" % "1.7.3",
  "com.google.oauth-client" % "google-oauth-client" % "1.17.0-rc",
  "com.google.http-client" % "google-http-client-jackson2" % "1.18.0-rc",
  "com.google.inject" % "guice" % "3.0" classifier "no_aop",
  aar("com.google.android.gms" % "play-services" % "4.3.23"))

unmanagedClasspath in Compile := (unmanagedClasspath in Compile).value filterNot (_.data.getName == "android-support-v4.jar")

javaOptions in Compile ++= Seq("file.encoding", "UTF-8")

// javacOptions in Compile += "-deprecation"

javacOptions in Compile ++= Seq("-encoding", "UTF-8")

// scalacOptions in Compile += "-deprecation"

proguardOptions in Android ++= io.Source.fromFile("C:\\Users\\shend\\AppData\\Local\\Android\\android-studio\\sdk\\tools\\proguard\\proguard-android.txt").getLines.toSeq

proguardOptions in Android ++= io.Source.fromFile("proguard-rules.txt").getLines.toSeq
