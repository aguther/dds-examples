# change to directory of this script
Push-Location -Path $PSScriptRoot

# define version
$NDDS_VERSION = $args[0]

# ask if version is correct
$QUESTION = Read-Host "Library version will be '$NDDS_VERSION'. Continue (Y/[N])?"
if ($QUESTION -ne "Y") {
  # restore directory and exit
  Pop-Location
  exit 0
}

# copy needed idl from SDK
Copy-Item -Path "$env:NDDSHOME\resource\idl\dds_rtf2_dcps.idl" -Destination ".\src\main\idl\"
Copy-Item -Path "$env:NDDSHOME\resource\idl\monitoring.idl" -Destination ".\src\main\idl\"
Copy-Item -Path "$env:NDDSHOME\resource\idl\RecordingServiceMonitoring.idl" -Destination ".\src\main\idl\"
Copy-Item -Path "$env:NDDSHOME\resource\idl\RoutingServiceMonitoring.idl" -Destination ".\src\main\idl\"
Copy-Item -Path "$env:NDDSHOME\resource\idl\ServiceAdmin.idl" -Destination ".\src\main\idl\"
Copy-Item -Path "$env:NDDSHOME\resource\idl\ServiceCommon.idl" -Destination ".\src\main\idl\"
Copy-Item -Path "$env:NDDSHOME\resource\idl\ServiceMonitoring.idl" -Destination ".\src\main\idl\"

# install needed dependencies into local maven
& .\mvnw.cmd --batch-mode install:install-file -Dfile="$env:NDDSHOME\lib\java\nddsjava.jar" -DgroupId="com.rti" -DartifactId="nddsjava" -Dversion="$NDDS_VERSION" -Dpackaging="jar"
& .\mvnw.cmd --batch-mode install:install-file -Dfile="$env:NDDSHOME\lib\java\rticonnextmsg.jar" -DgroupId="com.rti" -DartifactId="rticonnextmsg" -Dversion="$NDDS_VERSION" -Dpackaging="jar"
& .\mvnw.cmd --batch-mode install:install-file -Dfile="$env:NDDSHOME\lib\java\rtiroutingservice.jar" -DgroupId="com.rti" -DartifactId="rtiroutingservice" -Dversion="$NDDS_VERSION" -Dpackaging="jar"
& .\mvnw.cmd --batch-mode install:install-file -Dfile="$env:NDDSHOME\lib\java\rtirsadapter.jar" -DgroupId="com.rti" -DartifactId="rtirsadapter" -Dversion="$NDDS_VERSION" -Dpackaging="jar"

# restore directory
Pop-Location
