#!/usr/bin/env bash

# delete current folder and file
rm -f kafka.tgz

# download kafka 1.1.0
wget http://ftp-stud.hs-esslingen.de/pub/Mirrors/ftp.apache.org/dist/kafka/1.1.0/kafka_2.11-1.1.0.tgz -O kafka.tgz
