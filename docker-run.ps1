# Start a docker container with these files in the /usr/src folder
# Follow the README file to compile (mvn compile) or package (mvn package)

$containerName = "java-maven"
$containers = docker ps --all --format='{{json .Names}}'

if ($containers -Contains "`"$($containerName)`""){
	docker start -i $containerName
}
else {
	docker run -it -v "$($pwd.Path):/usr/src" -w /usr/src --name $containerName maven:3.6.1-jdk-11-slim bash
}

# While at the container bash, type 'exit' to stop the container, running this script again will pick up where you left