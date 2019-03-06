call mvn install:install-file -Dfile=nonlibs\sapjco3.jar -DgroupId=sapjco -DartifactId=sapjco -Dversion=3.0 -Dpackaging=jar -DgeneratePom=true
call mvn install:install-file -Dfile=nonpomlibs\Tidy.jar -DgroupId=org.w3c.tidy -DartifactId=com.springsource.org.w3c.tidy -Dversion=1.0 -Dpackaging=jar -DgeneratePom=true


