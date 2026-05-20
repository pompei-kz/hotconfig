package kz.pompei.hotconfig.gradle

import org.gradle.api.Action
import org.gradle.api.publish.maven.MavenPom

class KzPompeiMavenPublicationExtension {

  final PomSpec pom = new PomSpec()
  final DeveloperContainer developers = new DeveloperContainer()
  final ScmSpec scm = new ScmSpec()

  void pom(Action<? super PomSpec> action) {
    action.execute(pom)
  }

  void pom(Closure closure) {
    configure(pom, closure)
  }

  void developers(Action<? super DeveloperContainer> action) {
    action.execute(developers)
  }

  void developers(Closure closure) {
    configure(developers, closure)
  }

  void scm(Action<? super ScmSpec> action) {
    action.execute(scm)
  }

  void scm(Closure closure) {
    configure(scm, closure)
  }

  void applyTo(MavenPom target) {
    pom.applyTo(target)

    target.licenses { licenses ->
      licenses.license { license ->
        license.name = 'MIT License'
        license.url = 'https://opensource.org/license/mit/'
      }
    }

    List<DeveloperSpec> configuredDevelopers = developers.items
    target.developers { developerContainer ->
      configuredDevelopers.each { DeveloperSpec developerSpec ->
        developerContainer.developer { developer ->
          developerSpec.applyTo(developer)
        }
      }
    }

    target.scm { targetScm ->
      scm.applyTo(targetScm)
    }
  }

  private static void configure(Object target, Closure closure) {
    Closure configured = closure.rehydrate(target, closure.owner, closure.thisObject)
    configured.resolveStrategy = Closure.DELEGATE_FIRST
    configured.call()
  }
}
