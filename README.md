| Branch | Status
|---|---
|master |[![](https://github.com/genexuslabs/JavaClasses/workflows/Build/badge.svg)](https://github.com/genexuslabs/JavaClasses/actions?query=workflow%3ABuild+branch%3Amaster)
|beta   |[![](https://github.com/genexuslabs/JavaClasses/workflows/Build/badge.svg?branch=beta)](https://github.com/genexuslabs/JavaClasses/actions?query=workflow%3ABuild+branch%3Abeta)

# GeneXus Standard Classes for Java

These are the source of the GeneXus Standard Classes for Java, valid since GeneXus 16 Upgrade 4.

## Modules

| Name  | Description
|---|---
| common | Classes common to Android and Java
| gxcryptocommon | Classes common to Android and Java related to Cryptography
| gxmail | Classes related to mail handling
| java | Java standard classes,  output is gxclassr.jar
| wrappercommon | Interfaces to encapsulate Java EE and Jakarta EE support,  output is gxwrappercommon.jar
| wrapperjavax | Implement the interfaces defined in wrappercommon in Java EE,  output is gxwrapperjavax.jar
| wrapperjakarta | Implement the interfaces defined in wrappercommon in Jakarta EE,  output is gxwrapperjakarta.jar
| gxoffice | Formerly Java classes are now separated to be included only when using office. 
| gxsearch | Formerly in Java classes are now separated to be included only when using search.
| gxandroidpublisher and javapns | They are necessary for when you have Push Notifications in your old implementation. These are projects that should disappear in the short term.
| android | The standard Android classes. **Note that this is not the full runtime for Android, the full runtime can be created by using the Android Flexible Client project**.
| gxexternalproviders | Implements service provider for IBM Cloud, Google, Azure, Amazon
| gxgeospatial | Geography data type implementation
| gxodata | OData access

The dependencies between the projects are specified in each pom.xml within their directory.

# How to compile

## Requirements
- JDK 9 or greater
- Maven 3.6 or greater

In order to compile the *java* submodule, the [SAP JCo (SAP Java Connector)](https://support.sap.com/en/product/connectors/jco.html) needs to be installed in a Maven repository accessible while compiling (eg. the local Maven repository). This JAR can be downloaded from SAP's website (please note that a valid SAP Developer license may be required).

In order to install the connector into the Maven local repository, open a terminal and execute:
``` powershell
mvn install:install-file -DgroupId=com.sap.conn.jco -DartifactId=sapjco3 -Dversion=3.0.14 -Dpackaging=jar -Dfile=sapjco3.jar
```
(The required version of the JCo jar can be found in [the POM file of the java submodule](https://github.com/genexuslabs/JavaClasses/blob/master/java/pom.xml))

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
  
## License

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

  ## Agreement on Compatibility from now on
  
  With interfaces now in place since the v16 Upgrade 4 release, GeneXus SA commits to evolve them while maintaining compatibility.

