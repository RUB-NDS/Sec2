#!/bin/sh
mvn install --activate-profiles Android
adb install -r sec2-android/target/sec2-android-1.0-SNAPSHOT.apk
