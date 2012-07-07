#!/bin/sh

set -e
set -o nounset

VERSION="$1"
RELEASE="$2"
DIST="$3"
ARCH="$4"
REPO="$5"

MODULEDIR=$( cd "$( dirname "$0" )" && pwd )
RPMBUILD=${HOME}/rpmbuild
PACKAGE=elasticsearch-${VERSION}-${RELEASE}.${DIST}.${ARCH}.rpm

echo ""
echo "------------------------------------------------------------------------"
echo "Building RPM for elasticsearch ${VERSION} ..."
echo "------------------------------------------------------------------------"

echo ""
echo "------------------------------------------------------------------------"
echo "Cleaning rpmbuild directory in ${RPMBUILD} (PRE) ..."
rm -rf ${RPMBUILD}
echo "rpmbuild directory ${RPMBUILD} cleaned (PRE)"
echo "------------------------------------------------------------------------"

echo ""
echo "------------------------------------------------------------------------"
echo "Creating fresh rpmbuild directory in ${RPMBUILD} ..."
/usr/bin/rpmdev-setuptree
echo "Fresh rpmbuild directory in ${RPMBUILD} created"
echo "------------------------------------------------------------------------"

pushd ${RPMBUILD}

echo ""
echo "------------------------------------------------------------------------"
echo "Symlinking sources from ${MODULEDIR} into ${RPMBUILD} ..."
ln -s ${MODULEDIR}/target/rpm/SPECS/elasticsearch.spec ${RPMBUILD}/SPECS/elasticsearch.spec
ln -s ${MODULEDIR}/target/rpm/SOURCES/* ${RPMBUILD}/SOURCES/
echo "Symlinked sources from ${MODULEDIR} into ${RPMBUILD} ..."
echo "------------------------------------------------------------------------"

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
/usr/bin/rpmbuild --define "dist .${DIST}" -bb ${RPMBUILD}/SPECS/elasticsearch.spec
echo "Finished building elasticsearch binary rpm ..."
echo "------------------------------------------------------------------------"

echo ""
echo "------------------------------------------------------------------------"
echo "Copying elasticsearch binary rpm to ${MODULEDIR}/target ..."
mkdir -p ${MODULEDIR}/target
cp -R ${RPMBUILD}/RPMS/* ${MODULEDIR}/target/
echo "Copied elasticsearch binary rpm to ${MODULEDIR}/target ..."
echo "------------------------------------------------------------------------"

echo ""
echo "------------------------------------------------------------------------"
echo "Uploading rpm to pulp server ..."
/usr/bin/pulp-admin -u admin -p admin content upload --nosig --verbose --dir ${MODULEDIR}/target/x86_64/
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
echo "Cleaning rpmbuild directory in ${RPMBUILD} (POST)..."
rm -rf ${RPMBUILD}
echo "rpmbuild directory ${RPMBUILD} cleaned (POST)"
echo "------------------------------------------------------------------------"

echo ""
echo "------------------------------------------------------------------------"
echo "Finished building RPM for elasticsearch ${VERSION}"
echo "------------------------------------------------------------------------"
echo ""
