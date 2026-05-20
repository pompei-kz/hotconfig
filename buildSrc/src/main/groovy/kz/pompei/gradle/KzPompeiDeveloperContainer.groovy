package kz.pompei.gradle

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
