package kz.pompei.gradle

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.plugins.signing.SigningExtension

class KzPompeiPublicationPlugin implements Plugin<Project> {

  @Override
  void apply(Project project) {
    project.pluginManager.apply('maven-publish')
    project.pluginManager.apply('signing')

    def extension = project.extensions.create('kzPompeiPublication', KzPompeiPublicationExtension)

    project.pluginManager.withPlugin('java') {
      project.afterEvaluate {
        validateExtension(project, extension)
        configurePublishing(project, extension)
        configureSigning(project)
      }
    }
  }

  private static void configurePublishing(Project project, KzPompeiPublicationExtension extension) {
    project.extensions.configure(PublishingExtension) { publishing ->
      publishing.publications {
        mavenJava(MavenPublication) {
          from project.components.java

          pom {
            name = extension.pom.name
            description = extension.pom.description
            url = extension.pom.url

            licenses {
              license {
                name = 'MIT License'
                url = 'https://opensource.org/license/mit/'
              }
            }

            developers {
              extension.developers.developers.each { developerConfig ->
                developer {
                  id = developerConfig.id
                  name = developerConfig.name
                  email = developerConfig.email
                }
              }
            }

            scm {
              connection = extension.scm.connection
              developerConnection = extension.scm.developerConnection
              url = extension.scm.url
            }
          }
        }
      }

      publishing.repositories {
        maven {
          url = project.layout.buildDirectory.dir('staging-deploy')
        }
      }
    }
  }

  private static void configureSigning(Project project) {
    project.extensions.configure(SigningExtension) { signing ->
      signing.useGpgCmd()
      signing.sign(project.extensions.getByType(PublishingExtension).publications.mavenJava)
    }
  }

  private static void validateExtension(Project project, KzPompeiPublicationExtension extension) {
    def missing = []

    addIfBlank(missing, 'pom.name', extension.pom.name)
    addIfBlank(missing, 'pom.description', extension.pom.description)
    addIfBlank(missing, 'pom.url', extension.pom.url)

    if (extension.developers.developers.empty) {
      missing << 'developers.developer'
    } else {
      extension.developers.developers.eachWithIndex { developer, index ->
        addIfBlank(missing, "developers.developer[${index}].id", developer.id)
        addIfBlank(missing, "developers.developer[${index}].name", developer.name)
        addIfBlank(missing, "developers.developer[${index}].email", developer.email)
      }
    }

    addIfBlank(missing, 'scm.connection', extension.scm.connection)
    addIfBlank(missing, 'scm.developerConnection', extension.scm.developerConnection)
    addIfBlank(missing, 'scm.url', extension.scm.url)

    if (!missing.empty) {
      throw new GradleException("${project.path}: kzPompeiPublication is missing required values: ${missing.join(', ')}")
    }
  }

  private static void addIfBlank(List<String> missing, String propertyName, String value) {
    if (value == null || value.trim().empty) {
      missing << propertyName
    }
  }
}

class KzPompeiPublicationExtension {

  final KzPompeiPomConfig pom = new KzPompeiPomConfig()
  final KzPompeiDeveloperContainer developers = new KzPompeiDeveloperContainer()
  final KzPompeiScmConfig scm = new KzPompeiScmConfig()

  void pom(Closure closure) {
    configure(closure, pom)
  }

  void developers(Closure closure) {
    configure(closure, developers)
  }

  void scm(Closure closure) {
    configure(closure, scm)
  }

  private static void configure(Closure closure, Object target) {
    closure.delegate = target
    closure.resolveStrategy = Closure.DELEGATE_FIRST
    closure.call()
  }
}

class KzPompeiPomConfig {
  String name
  String description
  String url
}

class KzPompeiDeveloperContainer {

  final List<KzPompeiDeveloperConfig> developers = []

  void developer(Closure closure) {
    def developer = new KzPompeiDeveloperConfig()
    configure(closure, developer)
    developers << developer
  }

  private static void configure(Closure closure, Object target) {
    closure.delegate = target
    closure.resolveStrategy = Closure.DELEGATE_FIRST
    closure.call()
  }
}

class KzPompeiDeveloperConfig {
  String id
  String name
  String email
}

class KzPompeiScmConfig {
  String connection
  String developerConnection
  String url
}
