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
PACKAGE=graylog2-server-${VERSION}-${RELEASE}.${DIST}.${ARCH}.rpm

echo ""
echo "------------------------------------------------------------------------"
echo "Building RPM for graylog2-server ${VERSION} ..."
echo "Name:           graylog2-server"
echo "Version:        ${VERSION}"
echo "Release:        ${RELEASE}"
echo "Distribution:   ${DIST}"
echo "Architecture:   ${ARCH}"
echo "Repository:     ${REPO}"
echo "------------------------------------------------------------------------"

pushd ${RPMBUILD}

echo ""
echo "------------------------------------------------------------------------"
echo "Downloading graylog2-server sources ..."
/usr/bin/spectool --directory ${RPMBUILD}/SOURCES --get-files ${RPMBUILD}/SPECS/graylog2-server.spec
echo "Finished downloading graylog2-server sources"
echo "------------------------------------------------------------------------"

echo ""
echo "------------------------------------------------------------------------"
echo "Building graylog2-server binary rpm ..."
/usr/bin/rpmbuild --define "dist .${DIST}" --define "_topdir ${RPMBUILD}" --define "_tmppath ${TMPPATH}" -bb ${RPMBUILD}/SPECS/graylog2-server.spec
echo "Finished building graylog2-server binary rpm ..."
echo "------------------------------------------------------------------------"

echo ""
echo "------------------------------------------------------------------------"
echo "Uploading rpm to pulp server ..."
/usr/bin/pulp-admin -u admin -p admin content upload --nosig --verbose --dir ${RPMBUILD}/RPMS/${ARCH}
echo "Uploaded rpm to pulp server"
echo "------------------------------------------------------------------------"

echo ""
echo "------------------------------------------------------------------------"
echo "Associating generated rpm ${PACKAGE} with pulp repository ${REPO} ..."
/usr/bin/pulp-admin -u admin -p admin repo add_package --id ${REPO} --package=${PACKAGE}
echo "Associated generated rpm ${PACKAGE} with pulp repository ${REPO}"
echo "------------------------------------------------------------------------"

echo ""
echo "------------------------------------------------------------------------"
echo "Scheduling metadata update for pulp repository ${REPO} ..."
/usr/bin/pulp-admin -u admin -p admin repo generate_metadata --id ${REPO}
echo "Scheduled metadata update for pulp repository ${REPO}"
echo "------------------------------------------------------------------------"

popd

echo ""
echo "------------------------------------------------------------------------"
echo "Finished building RPM for graylog2-server ${VERSION}"
echo "------------------------------------------------------------------------"
echo ""
