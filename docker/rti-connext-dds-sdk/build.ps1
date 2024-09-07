#!/usr/bin/env pwsh

# Copy needed files
#New-Item -ItemType Directory -Path sdk
#Copy-Item -Recurse -Path "$env:NDDSHOME\*" -Destination "sdk\"
#robocopy "$env:NDDSHOME" "sdk" /E /NFL /NDL /ETA
#Remove-Item -Recurse -Force "sdk\rti_license.dat"

# Set version
$VERSION = "6.1.2"

# Start build of Docker file
docker build -t "rti-connext-dds-sdk:$VERSION" .

# Clean up files
Remove-Item -Recurse -Force "sdk"

# Save Docker image
docker save -o "rti-connext-dds-sdk--$VERSION.tar" "rti-connext-dds-sdk:$VERSION"

# Gzip archive
Compress-Archive -Path "rti-connext-dds-sdk--$VERSION.tar" -DestinationPath "rti-connext-dds-sdk--$VERSION.tar.gz" -Force
