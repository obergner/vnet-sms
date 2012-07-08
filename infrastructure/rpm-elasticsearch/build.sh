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
PACKAGE=elasticsearch-${VERSION}-${RELEASE}.${DIST}.${ARCH}.rpm

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
/usr/bin/spectool -g ${RPMBUILD}/SPECS/elasticsearch.spec
echo "Finished downloading elasticsearch sources"
echo "------------------------------------------------------------------------"

echo ""
echo "------------------------------------------------------------------------"
echo "Moving downloaded archive to ${RPMBUILD}/SOURCES ..."
mv ${RPMBUILD}/elasticsearch-${VERSION}.tar.gz ${RPMBUILD}/SOURCES
echo "Moved downloaded archive to ${RPMBUILD}/SOURCES"
echo "------------------------------------------------------------------------"

echo ""
echo "------------------------------------------------------------------------"
echo "Building elasticsearch binary rpm ..."
/usr/bin/rpmbuild --define "dist .${DIST}" --define "_topdir ${RPMBUILD}" -bb ${RPMBUILD}/SPECS/elasticsearch.spec
echo "Finished building elasticsearch binary rpm ..."
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
echo "Finished building RPM for elasticsearch ${VERSION}"
echo "------------------------------------------------------------------------"
echo ""
