#!/bin/bash
 
function scandir() {
	local cur_dir parent_dir workdir
	workdir=$1
	cd ${workdir}
	if [ ${workdir} = "/" ]
	then
		cur_dir=""
	else
		cur_dir=$(pwd)
	fi
 
	for dirlist in $(ls ${cur_dir})
	do
		if test -d ${dirlist};then
			cd ${dirlist}
			scandir ${cur_dir}/${dirlist}
			cd ..
		else
			if [ "${dirlist##*.}" = "java" ]; then
				echo ${cur_dir}/${dirlist}
				echo "\n"
			fi

		fi
	done
}

function usage() {
	echo "usage: sh build.sh [debug|release]"
}

# Verification
if [ $# != "1" ]; then
	usage
	exit 1
fi

# Clean up
echo 'Cleanning up...'
rm -rf build

# Init
echo 'Initializing build environment...'
mkdir build
mkdir build/gen
mkdir build/bin
mkdir build/bin/classes

# Configurations
res="-S res -S libs/appcompat/res" # Resources
ext_pkg='android.support.v7.appcompat' # Package that needs resources
src='src build/gen' # Sources
jar="$ANDROID_JAR:libs/android-support-v4.jar:libs/android-support-v7-appcompat.jar:libs/pinyin4j-2.5.0.jar" # JARs
manifest='AndroidManifest.xml' # Manifest
assets='assets' # Assets
pkgs='com.paperairplane.music.share' # Packages that needs to generate BuildConfig
aidl='src/com/sina/sso/RemoteSSO.aidl' # Aidls that needs building

# Run aapt
echo 'Compiling resources...'
aapt p -m -M $manifest -A $assets -I $ANDROID_JAR $res --extra-packages $ext_pkg --auto-add-overlay -J build/gen -F build/bin/build.apk

# Generate BuildConfig
echo 'Generating BuildConfig...'
if [ ${1} = "debug" ]; then
	flag="true"
elif [ ${1} = "release" ]; then
	flag="false"
fi
for pkg in $pkgs
do
	path="build/gen/${pkg//.//}"
	mkdir -p $path
	echo -e "package $pkg;\npublic class BuildConfig {\n	public static final boolean DEBUG=$flag;\n}" >> "$path/BuildConfig.java"
done

# Aidl
echo 'Converting Aidl...'
for f in $aidl
do
	aidl -obuild/gen $f
done

# Get list of sources
echo 'Generating list of sources...'
for dir in $src
do
	echo -e `scandir $dir` >> build/bin/sources.list
done

# Run javac
echo 'Compiling Java sources...'
javac -encoding utf-8 -cp $jar @build/bin/sources.list -d build/bin/classes

# Dex
echo 'Dexing...'
jar=${jar//:/ }
jar=${jar//$ANDROID_JAR/}
dx --dex --no-strict --output=build/bin/classes.dex build/bin/classes $jar

# Merge the dex and the apk
echo 'Merging...'
cd build/bin
aapt a build.apk classes.dex
cd ../..

# Sign
echo 'Signing...'
if [ ${1} = "debug" ]; then
	jarsigner -keystore keystore/debug.keystore -storepass android build/bin/build.apk my_alias
elif [ ${1} = "release" ]; then
	# Jarsigner will ask me for my passwords ^_^
	jarsigner -keystore keystore/publish.keystore build/bin/build.apk peter
fi

# Finished
echo "Apk built for $1: ${PWD}/build/bin/build.apk"
