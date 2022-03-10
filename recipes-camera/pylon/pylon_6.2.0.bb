SUMMARY = "Basler pylon Camera Software Suite"
DESCRIPTION = "A software package comprised of an easy-to-use SDK and tools that you can use to operate any Basler camera."
LICENSE = "Basler-pylon-6.2 & LGPLv3 & LGPLv2.1 & BSD-3-Clause & BSD-2-Clause & bzip2 & Libpng & Zlib & GenICam-1.1 & NI & xxHash & Apache-2.0"
LIC_FILES_CHKSUM = "file://pylon/share/pylon/licenses/License.html;md5=6a23d6496f15e590f32b3d3954297683 \
                    file://pylon/share/pylon/licenses/pylon_Third-Party_Licenses.html;md5=3ed299ffc665be3cebd634d25a41de80"

PR = "r1"

PYLON_FILE_NAME="pylon_6.2.0.21487_aarch64.selfsh"


SRC_URI = "https://artifacts.baslerweb.com/artifactory/embedded-vision-public/packages/${BPN}/${PYLON_FILE_NAME} \
           file://pylon-wrapper \
"
SRC_URI[md5sum] = "88710973fd2562fc7a6c2402c54334ac"
SRC_URI[sha256sum] = "e652c29b687a4cdb256860a1738f6d21f86a44ef0466f62df1051e011443cf67"



RDEPENDS_${PN} = "fontconfig freetype libsm libdrm libxcb-glx"

RRECOMMENDS_${PN} = "xauth python3-pypylon"

FILES_${PN} += "/opt/ /usr/bin/pylon"

INSANE_SKIP_${PN} += "already-stripped"
INSANE_SKIP_${PN} += "dev-so"
INSANE_SKIP_${PN} += "dev-elf"

# Work around for RDEPENDS to avoid 'no libGL.so.1()(64bit) provider found' problem
SKIP_FILEDEPS_${PN} = "1"

# Without this option yocto checks every .so included in pylon and treats it as possible RPROVIDES.
# This lead to spurious dependencies of gstreamer packages on pylon.
EXCLUDE_FROM_SHLIBS = "1"

# We want the complete pylon inside the sysroot to be able to build against it
SYSROOT_DIRS = "/opt"

PACKAGES_${PN} = "${PN}"


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


# extract the archive in unpack instead of do_install so that the license files are available for LIC_FILE checking
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

