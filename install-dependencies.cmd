@ECHO OFF
SETLOCAL

:: change to directory of this script
PUSHD %~dp0

:: set version of libraries
SET NDDS_VERSION=%1

:: echo version
SET /P QUESTION=Library version will be '%NDDS_VERSION%'. Continue (Y/[N])?
IF /I "%QUESTION%" NEQ "Y" GOTO END

:: copy IDLs from SDK
COPY %NDDSHOME%\resource\idl\dds_rtf2_dcps.idl %CD%/src/main/idl/
COPY %NDDSHOME%\resource\idl\monitoring.idl %CD%/src/main/idl/
COPY %NDDSHOME%\resource\idl\RecordingServiceMonitoring.idl %CD%/src/main/idl/
COPY %NDDSHOME%\resource\idl\RoutingServiceMonitoring.idl %CD%/src/main/idl/
COPY %NDDSHOME%\resource\idl\ServiceAdmin.idl %CD%/src/main/idl/
COPY %NDDSHOME%\resource\idl\ServiceCommon.idl %CD%/src/main/idl/
COPY %NDDSHOME%\resource\idl\ServiceMonitoring.idl %CD%/src/main/idl/

:: install nddsjava
CALL .\mvnw.cmd --batch-mode install:install-file -Dfile="%NDDSHOME%\lib\java\nddsjava.jar" -DgroupId=com.rti -DartifactId=nddsjava -Dversion=%NDDS_VERSION% -Dpackaging=jar

:: install messaging
CALL .\mvnw.cmd --batch-mode install:install-file -Dfile="%NDDSHOME%\lib\java\rticonnextmsg.jar" -DgroupId=com.rti -DartifactId=rticonnextmsg -Dversion=%NDDS_VERSION% -Dpackaging=jar

:: install routing service
CALL .\mvnw.cmd --batch-mode install:install-file -Dfile="%NDDSHOME%\lib\java\rtiroutingservice.jar" -DgroupId=com.rti -DartifactId=rtiroutingservice -Dversion=%NDDS_VERSION% -Dpackaging=jar

:: install routing service adapter
CALL .\mvnw.cmd --batch-mode install:install-file -Dfile="%NDDSHOME%\lib\java\rtirsadapter.jar" -DgroupId=com.rti -DartifactId=rtirsadapter -Dversion=%NDDS_VERSION% -Dpackaging=jar

:END

:: restore directory
POPD

ENDLOCAL
