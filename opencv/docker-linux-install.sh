#!/bin/bash

mkdir /app/.opencv
# shellcheck disable=SC2164
cd /app/.opencv
build_tools=(cmake g++ wget unzip ant zlib1g-dev python3-dev)
media_io_tools=(zlib1g-dev libjpeg-dev libwebp-dev libpng-dev libtiff-dev libjasper-dev libopenexr-dev)
# apt install build tool
apt-get update || true
# shellcheck disable=SC2068
apt-get install -y ${build_tools[@]}
# shellcheck disable=SC2068
apt-get install -y ${media_io_tools[@]}
# Download and unpack sources
wget -O opencv.zip https://github.com/opencv/opencv/archive/4.6.0.zip
unzip opencv.zip
# Create build directory
# shellcheck disable=SC2164
mkdir -p build && cd build
# Configure
# export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
cmake -D CMAKE_BUILD_TYPE=RELEASE -DBUILD_SHARED_LIBS=OFF ../opencv-4.6.0
# Build
make

# move lib and jar
cp bin/opencv*.jar lib/libopencv_java*.so /app

# apt remove build tool
# shellcheck disable=SC2068
apt-get remove --purge -y ${build_tools[@]}
apt-get autoclean && apt-get -y autoremove && apt-get clean

# remove file
# shellcheck disable=SC2103
rm -r /app/.opencv
