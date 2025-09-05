# Binaural player
# © Nicolas George -- 2010
# Modified for modern Android SDK -- 2025
#
# This program is free software; you can redistribute it and/or modify it
# under the terms of the GNU General Public License as published by the
# Free Software Foundation; version 2 of the License.
#
# This program is distributed in the hope that it will be useful, but
# WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
# Public License for more details.

PP = org/cigaes/binaural_player
APP = Binaural_player

RESOURCES = \
	res/drawable/icon.png \
	res/layout/tab_sequence.xml \
	res/layout/tab_edit.xml \
	res/layout/tab_play.xml
NATIVE_LIBS = \
	tmp/apk/lib/armeabi-v7a/libsbagen.so
CLASSES = \
	tmp/$(PP)/Binaural_player_GUI.class \
	tmp/$(PP)/Browser.class \
	tmp/$(PP)/Binaural_player.class \
	tmp/$(PP)/Binaural_decoder.class

# Modern SDK paths
SDK           = $(CURDIR)/tools/android-sdk
ANDROID       = $(SDK)/platforms/android-33
NDK           = $(SDK)/ndk/25.2.9519653

HOST_ARCH = windows-x86_64
TOOLCHAIN = llvm

KEY_DEBUG     = -keystore debug.keystore \
	        -storepass android -keypass android
KEY_RELEASE   = -keystore release.keystore
BOOTCLASSPATH = $(ANDROID)/android.jar
TOOLCHAINPATH = $(NDK)/toolchains/$(TOOLCHAIN)/prebuilt/$(HOST_ARCH)/bin
CC            = $(TOOLCHAINPATH)/clang.exe
LIBGCC        = $(shell $(CC) -print-libgcc-file-name)


JAVA_COMPILE = \
	javac -g -bootclasspath $(BOOTCLASSPATH) -classpath tmp -d tmp \
	-source 1.8 -target 1.8 $(JAVACFLAGS) $<
NATIVE_BUILD = \
	$(CC) \
	-I$(NDK)/toolchains/llvm/prebuilt/$(HOST_ARCH)/sysroot/usr/include \
	-fpic -ffunction-sections -funwind-tables \
	-fstack-protector-strong -fno-short-enums \
	-march=armv7-a -mthumb \
	-fomit-frame-pointer -fno-strict-aliasing -finline-limit=64 \
	-DANDROID
NATIVE_LINK = \
	$(CC) -shared \
	-o $@ $^ \
	-L$(NDK)/toolchains/llvm/prebuilt/$(HOST_ARCH)/sysroot/usr/lib/arm-linux-androideabi \
	-llog \
	-lc \
	-lm \
	-Wl,--no-undefined
AAPT_PACKAGE_BUILD_APK = \
	$(SDK)/build-tools/33.0.2/aapt package -f -M AndroidManifest.xml -S res -I $(BOOTCLASSPATH)

PATH := $(SDK)/build-tools/33.0.2:$(SDK)/cmdline-tools/latest/bin:$(PATH)

all: $(APP)-debug.apk

release: $(APP).apk

clean:
	rm -f Binaural_player-debug.apk res/drawable/icon.png sbagen-test
	rm -rf tmp/*

# Create debug keystore
debug.keystore:
	keytool -genkey -v -keystore debug.keystore -storepass android -alias androiddebugkey -keypass android -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Android Debug,O=Android,C=US"

# Create directories
tmp/apk/lib/armeabi-v7a:
	mkdir -p tmp/apk/lib/armeabi-v7a

tmp/$(PP):
	mkdir -p tmp/$(PP)

# Convert SVG icon to PNG
res/drawable/icon.png: icon.svg
	mkdir -p res/drawable
	convert -background none -resize 48x48 icon.svg res/drawable/icon.png

# Generate R.java
tmp/$(PP)/R.java: AndroidManifest.xml $(RESOURCES) | tmp/$(PP)
	$(SDK)/build-tools/33.0.2/aapt package -m -J tmp -M AndroidManifest.xml -S res -I $(BOOTCLASSPATH)

# Compile Java classes
tmp/$(PP)/%.class: %.java tmp/$(PP)/R.java | tmp/$(PP)
	$(JAVA_COMPILE)

# Build native library
tmp/apk/lib/armeabi-v7a/libsbagen.so: sbagen.c | tmp/apk/lib/armeabi-v7a
	$(NATIVE_BUILD) -c -o tmp/sbagen.o $<
	$(NATIVE_LINK)

# Create DEX file
tmp/apk/classes.dex: $(CLASSES)
	$(SDK)/build-tools/33.0.2/dx --dex --output=$@ tmp

# Package APK
tmp/$(APP)-debug-unaligned.apk: AndroidManifest.xml $(RESOURCES) \
  	tmp/apk/classes.dex $(NATIVE_LIBS)
	$(AAPT_PACKAGE_BUILD_APK) -F $@ tmp/apk

# Debug APK
$(APP)-debug.apk: tmp/$(APP)-debug-unaligned.apk debug.keystore
	$(SDK)/build-tools/33.0.2/zipalign -f 4 $< $@
	$(SDK)/build-tools/33.0.2/apksigner sign --ks debug.keystore --ks-pass pass:android --key-pass pass:android $@

# Release APK
$(APP).apk: tmp/$(APP)-debug-unaligned.apk $(KEY_RELEASE)
	$(SDK)/build-tools/33.0.2/zipalign -f 4 $< $@
	$(SDK)/build-tools/33.0.2/apksigner sign --ks $(KEY_RELEASE) $@
