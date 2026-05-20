package kz.pompei.gradle

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
