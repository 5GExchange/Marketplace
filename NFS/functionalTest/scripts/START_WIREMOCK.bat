@ECHO ON
SET BASEPATH=%~dp0..
SET JAVA_LIBS=%BASEPATH%\java.lib
SET OUTPUT_DIR=%BASEPATH%\output

REM =============================
REM CONFIGURAZIONE DA CUSTOMIZZARE
REM =============================
SET WIREMOCK_JAR=wiremock-1.56-standalone.jar
SET WIREMOCK_ARGS=--port 9000 --verbose --root-dir %BASEPATH%\test.suite\test.files\wiremock
SET CLASSPATH=%JAVA_LIBS%\*

START java -jar %JAVA_LIBS%\%WIREMOCK_JAR% %WIREMOCK_ARGS%

