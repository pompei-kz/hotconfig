package kz.pompei.hotconfig.gradle

import org.gradle.api.publish.maven.MavenPomScm

class ScmSpec {

  String connection
  String developerConnection
  String url

  void applyTo(MavenPomScm scm) {
    scm.connection = connection
    scm.developerConnection = developerConnection
    scm.url = url
  }
}
