package kz.pompei.hotconfig.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.plugins.signing.SigningExtension

class KzPompeiMavenPublicationPlugin implements Plugin<Project> {

  static final String EXTENSION_NAME = 'kzPompeiMavenPublication'

  @Override
  void apply(Project project) {
    project.pluginManager.apply('maven-publish')
    project.pluginManager.apply('signing')
    project.pluginManager.apply('org.jreleaser')

    KzPompeiMavenPublicationExtension extension = project.extensions.create(
      EXTENSION_NAME,
      KzPompeiMavenPublicationExtension
    )

    project.extensions.configure(PublishingExtension) { PublishingExtension publishing ->
      publishing.publications.create('mavenJava', MavenPublication) { MavenPublication publication ->
        publication.from(project.components.getByName('java'))
      }

      publishing.repositories.maven {
        it.url = project.layout.buildDirectory.dir('staging-deploy')
      }
    }

    project.afterEvaluate {
      project.extensions.getByType(PublishingExtension).publications.named('mavenJava', MavenPublication).configure {
        it.pom { pom ->
          extension.applyTo(pom)
        }
      }
    }

    project.extensions.configure(SigningExtension) { SigningExtension signing ->
      signing.useGpgCmd()
      signing.sign(project.extensions.getByType(PublishingExtension).publications.getByName('mavenJava'))
    }

    project.extensions.configure('jreleaser') { jreleaser ->
      jreleaser.gitRootSearch = true

      jreleaser.signing {
        it.active = 'ALWAYS'
        it.armored = true
      }

      jreleaser.deploy {
        it.maven {
          it.mavenCentral { mavenCentral ->
            mavenCentral.create('sonatype') { sonatype ->
              sonatype.active = 'ALWAYS'
              sonatype.url = 'https://central.sonatype.com/api/v1/publisher'
              sonatype.stagingRepository('build/staging-deploy')
              sonatype.applyMavenCentralRules = true
              sonatype.namespace = 'kz.pompei'
            }
          }
        }
      }
    }

    project.tasks.matching { it.name == 'jreleaserDeploy' }.configureEach {
      it.dependsOn(project.tasks.matching { task -> task.name == 'publish' })
    }
  }
}
