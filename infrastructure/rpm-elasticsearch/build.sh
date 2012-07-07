#!/bin/sh

VERSION="$1"
MODULEDIR=$( cd "$( dirname "$0" )" && pwd )
RPMBUILD=${HOME}/rpmbuild

echo "Building RPM for elasticsearch ${VERSION} ..."

echo "Cleaning rpmbuild directory in ${RPMBUILD} ..."
rm -rf ${HOME}/rpmbuild
echo "rpmbuild directory ${RPMBUILD} cleaned"

echo "Creating fresh rpmbuild directory in ${RPMBUILD} ..."
/usr/bin/rpmdev-setuptree
echo "Fresh rpmbuild directory in ${RPMBUILD} created"

pushd ${RPMBUILD}

echo "Symlinking sources from ${MODULEDIR} into ${RPMBUILD} ..."
ln -s ${MODULEDIR}/target/rpm/SPECS/elasticsearch.spec ${RPMBUILD}/SPECS/elasticsearch.spec
ln -s ${MODULEDIR}/target/rpm/SOURCES/* ${RPMBUILD}/SOURCES/
echo "Symlinked sources from ${MODULEDIR} into ${RPMBUILD} ..."

echo "Downloading elasticsearch sources ..."
/usr/bin/spectool -g SPECS/elasticsearch.spec
echo "Finished downloading elasticsearch sources"

echo "Moving downloaded archive to ${RPMBUILD}/SOURCES ..."
mv ${RPMBUILD}/elasticsearch-${VERSION}.tar.gz ${RPMBUILD}/SOURCES
echo "Moved downloaded archive to ${RPMBUILD}/SOURCES"

# echo "Building elasticsearch source rpm ..."
# /usr/bin/rpmbuild -bs --nodeps SPECS/elasticsearch.spec
# echo "Finished building elasticsearch source rpm"

echo "Building elasticsearch binary rpm ..."
/usr/bin/rpmbuild -bb SPECS/elasticsearch.spec
echo "Finished building elasticsearch binary rpm ..."

echo "Copying elasticsearch binary rpm to ${MODULEDIR}/target ..."
mkdir -p ${MODULEDIR}/target
#rm -rf ${MODULEDIR}/target/*
cp -R ${RPMBUILD}/RPMS/* ${MODULEDIR}/target/
echo "Copied elasticsearch binary rpm to ${MODULEDIR}/target ..."

popd

echo "Finished building RPM for elasticsearch ${VERSION}"
