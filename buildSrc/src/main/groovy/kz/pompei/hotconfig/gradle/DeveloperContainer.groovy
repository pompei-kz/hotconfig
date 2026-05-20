package kz.pompei.hotconfig.gradle

import org.gradle.api.Action

class DeveloperContainer {

  final List<DeveloperSpec> items = []

  void developer(Action<? super DeveloperSpec> action) {
    DeveloperSpec developer = new DeveloperSpec()
    action.execute(developer)
    items.add(developer)
  }

  void developer(Closure closure) {
    DeveloperSpec developer = new DeveloperSpec()
    Closure configured = closure.rehydrate(developer, closure.owner, closure.thisObject)
    configured.resolveStrategy = Closure.DELEGATE_FIRST
    configured.call()
    items.add(developer)
  }
}
