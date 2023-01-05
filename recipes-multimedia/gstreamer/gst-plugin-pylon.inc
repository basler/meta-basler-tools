SUMMARY = "Basler pylon gstreamer plugin"
HOMEPAGE = "https://github.com/basler/gst-plugin-pylon"
BUGTRACKER = "https://github.com/basler/gst-plugin-pylon/issues"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=ae3b821cf6c4b42fabeceeafd1df5f95"


SRCBRANCH = "main"
SRC_URI = "git://github.com/basler/gst-plugin-pylon.git;protocol=https;branch=${SRCBRANCH}"


S = "${WORKDIR}/git"

inherit meson pkgconfig

DEPENDS = "pylon glib-2.0 gstreamer1.0 gstreamer1.0-plugins-base cmake-native patchelf-native"
RDEPENDS:${PN} = "gstreamer1.0 pylon"
RPROVIDES:${PN} += "gst-plugin-pylon"

do_configure:prepend(){
    export PYLON_ROOT=${WORKDIR}/recipe-sysroot/opt/pylon
    export CMAKE_PREFIX_PATH=${PYLON_DEV_DIR}/share/pylon/cmake
}

# enable meson to use the pylon cmake finder
do_write_config:append() {
    if ! grep -qxF "cmake = 'cmake'" ${WORKDIR}/meson.cross; then
        sed -i "s/\[binaries\]/[binaries]\\ncmake = 'cmake'/" ${WORKDIR}/meson.cross
    fi
    if ! grep -qxF "cmake = 'cmake'" ${WORKDIR}/meson.native; then
        sed -i "s/\[binaries\]/[binaries]\\ncmake = 'cmake'/" ${WORKDIR}/meson.native
    fi
}

# patch the final runpath as workaround to meson/cmake rpath issue
do_install:append(){
    patchelf --set-rpath /opt/pylon/lib ${D}${libdir}/libgstpylon-1.0.so
    patchelf --set-rpath /opt/pylon/lib ${D}${libdir}/gstreamer-1.0/libgstpylon.so
}

FILES:${PN} = "${libdir}/gstreamer-1.0/libgstpylon.so ${libdir}/libgstpylon-1.0.so"
FILES:${PN}-dev = "/usr/include /usr/lib/pkgconfig"
