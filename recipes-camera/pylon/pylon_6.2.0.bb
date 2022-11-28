SUMMARY = "Basler pylon Camera Software Suite"
DESCRIPTION = "A software package comprised of an easy-to-use SDK and tools that you can use to operate any Basler camera."
LICENSE = "Basler-pylon-6.2 & LGPLv3 & LGPLv2.1 & BSD-3-Clause & BSD-2-Clause & bzip2 & Libpng & Zlib & GenICam-1.1 & NI & xxHash & Apache-2.0"
LIC_FILES_CHKSUM = "file://pylon/share/pylon/licenses/License.html;md5=6a23d6496f15e590f32b3d3954297683 \
                    file://pylon/share/pylon/licenses/pylon_Third-Party_Licenses.html;md5=3ed299ffc665be3cebd634d25a41de80"

PR = "r2"

PYLON_FILE_NAME="pylon_6.2.0.21487_aarch64.selfsh"


SRC_URI = "https://artifacts.baslerweb.com/artifactory/embedded-vision-public/packages/${BPN}/${PYLON_FILE_NAME} \
           file://pylon-wrapper \
           file://patches/01-fix-cpp20-in-headers.patch \
"
SRC_URI[md5sum] = "88710973fd2562fc7a6c2402c54334ac"
SRC_URI[sha256sum] = "e652c29b687a4cdb256860a1738f6d21f86a44ef0466f62df1051e011443cf67"

PACKAGE_BEFORE_PN = "${PN}-runtime"


################### dev package

FILES:${PN}-dev =     "/opt/pylon/include \
                       /opt/pylon/bin/pylon-config \
                       /opt/pylon/share/pylon/Samples"
# Samples contain libBconAdapterSample-3.0.so
INSANE_SKIP:${PN}-dev += "dev-elf"

################### doc package

FILES:${PN}-doc =     "/opt/pylon/share/pylon/doc"

################### Runtime Package

FILES:${PN}-runtime = "/opt/pylon/lib/*.so \
                       /opt/pylon/lib/gentlproducer \
                       /opt/pylon/share/pylon/licenses \
                       /opt/pylon/share/pylon/log \
                       /opt/pylon/share/pylon/* "
RDEPENDS:${PN}-runtime += "glibc libstdc++ libgcc"

RRECOMMENDS:${PN}-runtime = "python3-pypylon"
# The runtime contains some symlinks, that should be left in
INSANE_SKIP:${PN}-runtime += "dev-so"

PRIVATE_LIBS:${PN}-runtime = "lib?xapi* *libusb* libpylon_TL*"

################### Main pylon Package (Viewer)

FILES:${PN} =          "/opt/pylon \
                        /usr/bin"
RDEPENDS:${PN} += "${PN}-runtime"
# Default to having the full pylon package installed
RRECOMMENDS:${PN} += "${PN}-dev ${PN}-doc"

# pylon viewer is based on qt with different platform plugins
# As we don't know the exact setup of the customer the strategy is as follows:
# - ignore QA file-rdeps issues (reenable this check to analyze dependency issues)
# - install all plugins
# - be conservative in RDEPENDS, if in doubt add it to RRECOMMENDS

INSANE_SKIP:${PN} += "file-rdeps"

# Dependencies for platform/libqlinuxfb
RRECOMMENDS:${PN} += "libxkbcommon libudev libinput libdrm"

# Dependencies for platform/libqwebgl
RRECOMMENDS:${PN} += "fontconfig freetype"

# Dependencies for platform/libqxcb
# If distro contains x11 add all dependencies
RDEPENDS:${PN} += " ${@bb.utils.contains('DISTRO_FEATURES', 'x11', 'libx11 libx11-xcb libxcb libxcb-glx libxcb-xfixes libsm libice libxkbcommon ', '', d)}"
# We don't know if the platform provides libgl and libegl and can therefore not depend on it
# xauth is recommended for everyone using the viewer via remote x11
RRECOMMENDS:${PN} += " ${@bb.utils.contains('DISTRO_FEATURES', 'x11', 'xauth libegl libgl', '', d)}"

# Dependencies for platform/wayland
RDEPENDS_${PN} += " ${@bb.utils.contains('DISTRO_FEATURES', 'wayland', 'wayland', '', d)}"

INSANE_SKIP:${PN} += "already-stripped"
# pylon viewer contains some symlinks, that should be left in
INSANE_SKIP:${PN} += "dev-so"

# pylon viewer does not provide any library to the outside
PRIVATE_LIBS:${PN} = "*"

###################### end of packages

# We want the complete pylon inside the sysroot to be able to build against it
SYSROOT_DIRS = "/opt"


python check_basler_eula() {
    eula = d.getVar('ACCEPT_BASLER_EULA')
    pkg = d.getVar('PN')
    if eula is None:
        bb.fatal("To use '%s' you need to accept the Basler Licenses at 'meta-basler-*/licenses'. "
                 "Please read them and in case you accept them, write: "
                 "ACCEPT_BASLER_EULA = \"1\" in your local.conf." % (pkg,))
    elif eula == '0':
        bb.fatal("To use '%s' you need to accept the Basler Licensess." % pkg)
    else:
        bb.note("Basler Licenses have been accepted for '%s'" % pkg)
}


# Extract the archive in unpack instead of do_install so that the license files are available for LIC_FILE checking
python do_unpack:append() {
    bb.build.exec_func("check_basler_eula", d)

    pylon_archive = d.getVar('PYLON_FILE_NAME')
    workdir = d.getVar('WORKDIR')
    srcdir = d.getVar('S')

    bb.utils.remove(srcdir+'/pylon', recurse=True)
    bb.process.run('sh %s/%s  --quiet --accept' % (workdir, pylon_archive), cwd=srcdir)
}


do_install[dirs] += "${D}/opt"

do_install() {
    cp -a --no-preserve=ownership ${S}/pylon ${D}/opt/
    # fix permissions for group and others
    find ${D}/opt -perm 700 -exec chmod 755 {} \;
    find ${D}/opt -perm 600 -exec chmod 644 {} \;

    install -D ${WORKDIR}/pylon-wrapper ${D}/usr/bin/pylon
}

