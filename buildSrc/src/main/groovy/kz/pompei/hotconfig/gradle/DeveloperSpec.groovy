package kz.pompei.hotconfig.gradle

import org.gradle.api.publish.maven.MavenPomDeveloper

class DeveloperSpec {

  String id
  String name
  String email

  void applyTo(MavenPomDeveloper developer) {
    developer.id = id
    developer.name = name
    developer.email = email
  }
}
