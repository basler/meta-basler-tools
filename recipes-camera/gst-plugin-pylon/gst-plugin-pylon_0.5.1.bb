SUMMARY = "The official GStreamer plug-in for Basler cameras"
DESCRIPTION = "This plugin allows to use any Basler 2D camera (supported by Basler pylon Camera Software Suite) as source element in a GStreamer pipeline."
HOMEPAGE = "https://github.com/basler/gst-plugin-pylon"
LICENSE = "BSD-3-Clause"

DEPENDS = "gstreamer1.0 gstreamer1.0-plugins-base pylon cmake-native"

inherit pkgconfig meson

SRC_URI = "git://github.com/basler/gst-plugin-pylon.git;branch=main;name=git;protocol=https"
SRCREV_git = "ac4e0c29314109b1ffd97039a8a5167225fe2239"
LIC_FILES_CHKSUM = "file://LICENSE;md5=ae3b821cf6c4b42fabeceeafd1df5f95"

# basler build configuration
export PYLON_ROOT="${WORKDIR}/recipe-sysroot/opt/pylon"

S = "${WORKDIR}/git"

FILES:${PN} += "/usr/lib/gstreamer-1.0/libgstpylon.so"

do_configure:prepend() {
    # (dirty) workaround: meson uses cmake to find the pylon stuff, however
    # meson.cross file is not aware of the cmake binary, therefore add it
    sed -i "/\[binaries\]/a cmake = 'cmake'" "${WORKDIR}/meson.cross"
}

# QA Issue: gst-plugin-pylon rdepends on gst-plugin-pylon-dev [dev-deps]
# QA Issue: -dev package gst-plugin-pylon-dev contains non-symlink .so '/usr/lib/libgstpylon-1.0.so' [dev-elf]
INSANE_SKIP:${PN} = "dev-deps"
INSANE_SKIP:${PN}-dev = "dev-elf"
