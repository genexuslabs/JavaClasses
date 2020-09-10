
SET lib=C:\Program Files\Apache Software Foundation\Tomcat 8.5\webapps\TestStoreJavaSqlIBM\WEB-INF\lib
echo %lib%
copy "java\target\gxclassR.jar" "%lib%\gxclassR.jar" /Y
copy "common\target\gxcommon.jar" "%lib%\gxcommon.jar" /Y
copy "gxexternalpobject_idroviders\target\gxexternalproviders.jar" "%lib%\gxexternalproviders.jar" /Y