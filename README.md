# GeneXus Standard Classes for Java

Note that this is a **Work in Progress**. **These classes are not intended to be used as release library yet**.

## Modules

| Name  | Description
|---|---
| common | Classes common to Android and Java
| gxcryptocommon | Classes common to Android and Java related to Cryptography
| apacheandroid | Android apache class dependence. These were separated by size issues. They are candidates to disappear in the near future
| gxmail | Classes related to mail handling
| java | Java standard classes
| gxoffice | Formerly Java classes are now separated to be included only when using office. 
| gxsearch | Formerly in Java classes are now separated to be included only when using search.
| gxandroidpublisher and javapns | They are necessary for when you have Push Notifications in your old implementation. These are projects that should disappear in the short term.
| android | The standard Android classes. **Note that this is not the full runtime for Android, the full runtime can be created by using the Android Flexible Client project**.

The dependencies between the projects are specified in each pom.xml within their directory.

# How to compile

## Requirements
- JDK 9 or greater
- Maven 3.6 or greater

# Instructions

## How to build all projects?
- ```mvn compile```

## How to build a specific project?
- ```cd <specific project dir>```
- ```mvn compile```

## How to package all or some project?
- ```mvn package```

## How to copy dependencies jar files to the dependency directory?
- ```cd java```
- ```mvn dependency:copy-dependencies```

### Bulid with Docker container.
There's a PowerShell script called [docker-run.ps1](./docker-run.ps1) that will spin up a container with the needed runtime to compile the sources.
Running the script will pull the [maven:3.6.1-jdk-11-slim](https://hub.docker.com/_/maven?tab=description) image an create a container with these files mounte under /usr/src. From there you can follow the instructions from above to compile and/or package the classes.  
Once you compiled or packaged everything you needed, you can stop the container by typing 'exit' at the bash command prompt or executing `docker stop java-maven`. Running the docker-run.ps1 script again will spin up the stopped container, so what had been downloaded in previous runs will still be there.

## How to create a Site with the specification of each module?
- ```mvn site```

 (Android site is failing)
 (when processing xmlsec exception are raised but the process continue)

 The site for each module can be found at <project dir>\target\site\index.html

  ![Site](site.png)
  
  ## Agreement on Compatibility from now on
  
  With interfaces now in place since the v16 Upgrade 4 release, GeneXus SA commits to evolve them while maintaining compatibility.
