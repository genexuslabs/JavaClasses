# JavaClasses
GeneXus Standard Classes for Java.

GeneXus Standard Classes for Android. *This is not the full runtime for Android, the full runtime can be created by using the Android Flexible Client project*

# Requirements

- JDK 6 or superior
- Maven 3.6 or superior

# Projects Description

he Maven directory is a project that allows you to build multiple modules.

## common
Classes common to Android and Java

## gxcryptocommon

Classes common to Android and Java related to Cryptography

## apacheandroid

Android apache class dependence. These were separated by size issues. They are candidates to disappear in the near future.

## gxmail

Classes related to mail handling.

## gx

Java standard classes

## gxoffice

Formerly Java classes are now separated to be included only when using office.

## gxsearch

Formerly in Java classes are now separated to be included only when using search.

## gxandroidpublisher and javapns

They are necessary for when you have Push Notifications in your old implementation. These are projects that should disappear in the short term.

## Android

The standard Android classes.

The dependencies between the projects are specified in each pom.xml within their directory.


# How to build

- Download or clone the project. 
- Open a windows command prompt and move to the <project dir>\makefile directory
- Open the build.bat and check the JAVA_HOME directory and the ANT_HOME directory are ok for your environment.
- run buildGxClasses.bat


