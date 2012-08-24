#!/bin/sh

set -e
set -o nounset

VERSION="$1"
RELEASE="$2"
DIST="$3"
ARCH="$4"
REPO="$5"

MODULEDIR=$( cd "$( dirname "$0" )" && pwd )
RPMBUILD=${MODULEDIR}/target/rpmbuild
TMPPATH=${MODULEDIR}/target/tmp
PACKAGE=graylog2-web-interface-${VERSION}-${RELEASE}.${DIST}.${ARCH}.rpm

echo ""
echo "------------------------------------------------------------------------"
echo "Building RPM for graylog2-web-interface ${VERSION} ..."
echo "Name:           graylog2-web-interface"
echo "Version:        ${VERSION}"
echo "Release:        ${RELEASE}"
echo "Distribution:   ${DIST}"
echo "Architecture:   ${ARCH}"
echo "Repository:     ${REPO}"
echo "------------------------------------------------------------------------"

pushd ${RPMBUILD}

echo ""
echo "------------------------------------------------------------------------"
echo "Downloading graylog2-web-interface sources ..."
/usr/bin/spectool --directory ${RPMBUILD}/SOURCES --get-files ${RPMBUILD}/SPECS/graylog2-web-interface.spec
echo "Finished downloading graylog2-web-interface sources"
echo "------------------------------------------------------------------------"

echo ""
echo "------------------------------------------------------------------------"
echo "Building graylog2-web-interface binary rpm ..."
/usr/bin/rpmbuild --define "dist .${DIST}" --define "_topdir ${RPMBUILD}" --define "_tmppath ${TMPPATH}" -bb ${RPMBUILD}/SPECS/graylog2-web-interface.spec
echo "Finished building graylog2-web-interface binary rpm ..."
echo "------------------------------------------------------------------------"

echo ""
echo "------------------------------------------------------------------------"
echo "Uploading rpm to pulp server ..."
/usr/bin/pulp-admin -u admin -p admin content upload --nosig --verbose --repoid=${REPO} ${RPMBUILD}/RPMS/${ARCH}/${PACKAGE}
echo "Uploaded rpm to pulp server"
echo "------------------------------------------------------------------------"

popd

echo ""
echo "------------------------------------------------------------------------"
echo "Finished building RPM for graylog2-web-interface ${VERSION}"
echo "------------------------------------------------------------------------"
echo ""
