package kz.pompei.gradle

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy

class GotoMavenPlugin implements Plugin<Project> {

  @Override
  void apply(Project project) {
    project.pluginManager.apply('org.jreleaser')

    def extension = project.extensions.create('gotoMaven', GotoMavenExtension)
    def copyStagingDeploy = registerCopyStagingDeploy(project)

    configureJReleaser(project)

    project.tasks.matching { it.name == 'jreleaserDeploy' }.configureEach {
      dependsOn copyStagingDeploy
    }

    project.tasks.register('goto-maven') {
      group = 'pompei'
      description = 'Deploys staged artifacts to Maven Central.'
      dependsOn project.tasks.matching { it.name == 'jreleaserDeploy' }
    }

    project.afterEvaluate {
      configureCopyStagingDeploy(project, copyStagingDeploy, extension)
    }
  }

  private static Object registerCopyStagingDeploy(Project project) {
    project.tasks.register('copyStagingDeploy', Copy) {
      group = 'pompei'
      description = 'Copies module staging deploy files into my build/staging-deploy.'

      into(project.layout.buildDirectory.dir('staging-deploy'))
      includeEmptyDirs = false
    }
  }

  private static void configureCopyStagingDeploy(Project project, Object copyStagingDeploy, GotoMavenExtension extension) {
    validateExtension(project, extension)

    copyStagingDeploy.configure { Copy task ->
      extension.publicationModules.each { publicationModule ->
        task.dependsOn "${publicationModule}:publish"
        task.from(project.project(publicationModule).layout.buildDirectory.dir('staging-deploy'))
      }
    }
  }

  private static void configureJReleaser(Project project) {
    project.extensions.configure('jreleaser') { jreleaser ->
      jreleaser.gitRootSearch = true

      jreleaser.signing {
        active = 'ALWAYS'
        armored = true
      }

      jreleaser.deploy {
        maven {
          mavenCentral {
            sonatype {
              active = 'ALWAYS'
              url = 'https://central.sonatype.com/api/v1/publisher'
              stagingRepository('build/staging-deploy')
              applyMavenCentralRules = true
              namespace = 'kz.pompei'
            }
          }
        }
      }
    }
  }

  private static void validateExtension(Project project, GotoMavenExtension extension) {
    if (extension.publicationModules == null || extension.publicationModules.empty) {
      throw new GradleException("${project.path}: gotoMaven.publicationModules must not be empty")
    }

    extension.publicationModules.eachWithIndex { publicationModule, index ->
      if (publicationModule == null || publicationModule.trim().empty) {
        throw new GradleException("${project.path}: gotoMaven.publicationModules[${index}] must not be blank")
      }
    }
  }
}
