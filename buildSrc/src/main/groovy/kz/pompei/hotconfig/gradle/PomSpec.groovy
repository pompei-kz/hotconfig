package kz.pompei.hotconfig.gradle

import org.gradle.api.publish.maven.MavenPom

class PomSpec {

  String name
  String description
  String url

  void applyTo(MavenPom pom) {
    pom.name = name
    pom.description = description
    pom.url = url
  }
}
