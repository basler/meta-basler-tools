LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=5209c60dd8406ab83075514af62800b6"

SUMMARY = "Python 3 bindings for pylon"

PR = "r0"

inherit setuptools3

# Github source drive
SRC_URI = "git://github.com/basler/pypylon.git;protocol=https"
SRCREV = "2bd0a6268dd5dd36a0aa8486c7158ffc0166abbc"
S = "${WORKDIR}/git"

DEPENDS = "pylon python3 swig-native python3-setuptools-native"
RDEPENDS:${PN} = "python3-numpy"

# pypylon packages pylon libs which needs a bit of support
# treat the libs as private
EXCLUDE_FROM_SHLIBS = "1"
# add dependencies from pylon-runtime
RDEPENDS:${PN} += "glibc libstdc++ libgcc"
INSANE_SKIP:${PN} += "already-stripped"

export PYLON_ROOT = "${WORKDIR}/recipe-sysroot/opt/pylon"
