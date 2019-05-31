# Start a docker container with these file in /usr/src 
# From there follow the README file to compile (mvn compile) or package (mvn package)
docker run --rm -it -v "$($pwd.Path):/usr/src" maven:3.6.1-jdk-11-slim bash
