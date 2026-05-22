print-hello:
	echo "Hello"

goto-maven:
	./gradlew clean :publication:goto-maven --info

docker-restart:
	bash docker/docker-restart.bash
