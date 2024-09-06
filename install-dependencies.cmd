@ECHO OFF
SETLOCAL

:: set version of libraries
SET NDDS_VERSION=%1

:: echo version
SET /P QUESTION=Library version will be '%NDDS_VERSION%'. Continue (Y/[N])?
IF /I "%QUESTION%" NEQ "Y" GOTO END

:: install nddsjava
CALL .\mvnw.cmd --batch-mode install:install-file -Dfile="%NDDSHOME%\lib\java\nddsjava.jar" -DgroupId=com.rti -DartifactId=nddsjava -Dversion=%NDDS_VERSION% -Dpackaging=jar

:: install messaging
CALL .\mvnw.cmd --batch-mode install:install-file -Dfile="%NDDSHOME%\lib\java\rticonnextmsg.jar" -DgroupId=com.rti -DartifactId=rticonnextmsg -Dversion=%NDDS_VERSION% -Dpackaging=jar

:: install routing service
CALL .\mvnw.cmd --batch-mode install:install-file -Dfile="%NDDSHOME%\lib\java\rtiroutingservice.jar" -DgroupId=com.rti -DartifactId=rtiroutingservice -Dversion=%NDDS_VERSION% -Dpackaging=jar

:: install routing service adapter
CALL .\mvnw.cmd --batch-mode install:install-file -Dfile="%NDDSHOME%\lib\java\rtirsadapter.jar" -DgroupId=com.rti -DartifactId=rtirsadapter -Dversion=%NDDS_VERSION% -Dpackaging=jar

:END
ENDLOCAL
