.PHONY: test-application

publishToMavenLocal:
	./gradlew publishToMavenLocal

test-application:
	./gradlew :test-application:test-application

goto-maven:
	./gradlew clean :publication:goto-maven

docker-restart:
	bash docker/docker-restart.bash
