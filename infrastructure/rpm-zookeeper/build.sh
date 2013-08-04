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
PACKAGE=zookeeper-${VERSION}-${RELEASE}.${DIST}.${ARCH}.rpm

echo ""
echo "------------------------------------------------------------------------"
echo "Building RPM for elasticsearch ${VERSION} ..."
echo "Name:           elasticsearch"
echo "Version:        ${VERSION}"
echo "Release:        ${RELEASE}"
echo "Distribution:   ${DIST}"
echo "Architecture:   ${ARCH}"
echo "Repository:     ${REPO}"
echo "------------------------------------------------------------------------"

pushd ${RPMBUILD}

echo ""
echo "------------------------------------------------------------------------"
echo "Downloading elasticsearch sources ..."
/usr/bin/spectool --directory ${RPMBUILD}/SOURCES --get-files ${RPMBUILD}/SPECS/elasticsearch.spec
echo "Finished downloading elasticsearch sources"
echo "------------------------------------------------------------------------"

echo ""
echo "------------------------------------------------------------------------"
echo "Building elasticsearch binary rpm ..."
/usr/bin/rpmbuild --define "dist .${DIST}" --define "_topdir ${RPMBUILD}" --define "_tmppath ${TMPPATH}" -bb ${RPMBUILD}/SPECS/elasticsearch.spec
echo "Finished building elasticsearch binary rpm ..."
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
echo "Finished building RPM for elasticsearch ${VERSION}"
echo "------------------------------------------------------------------------"
echo ""
