#!/bin/sh

VERSION="$1"
MODULEDIR=$( cd "$( dirname "$0" )" && pwd )
RPMBUILD=${HOME}/rpmbuild

echo "Building RPM for elasticsearch ${VERSION} ..."

echo "Cleaning rpmbuild directory in ${RPMBUILD} ..."
rm -rf ${RPMBUILD}
echo "rpmbuild directory ${RPMBUILD} cleaned"

echo "Creating fresh rpmbuild directory in ${RPMBUILD} ..."
/usr/bin/rpmdev-setuptree || exit 1
echo "Fresh rpmbuild directory in ${RPMBUILD} created"

pushd ${RPMBUILD}

echo "Symlinking sources from ${MODULEDIR} into ${RPMBUILD} ..."
ln -s ${MODULEDIR}/target/rpm/SPECS/elasticsearch.spec ${RPMBUILD}/SPECS/elasticsearch.spec || exit 1
ln -s ${MODULEDIR}/target/rpm/SOURCES/* ${RPMBUILD}/SOURCES/ || exit 1
echo "Symlinked sources from ${MODULEDIR} into ${RPMBUILD} ..."

echo "Downloading elasticsearch sources ..."
/usr/bin/spectool -g ${RPMBUILD}/SPECS/elasticsearch.spec  || exit 1
echo "Finished downloading elasticsearch sources"

echo "Moving downloaded archive to ${RPMBUILD}/SOURCES ..."
mv ${RPMBUILD}/elasticsearch-${VERSION}.tar.gz ${RPMBUILD}/SOURCES || exit 1
echo "Moved downloaded archive to ${RPMBUILD}/SOURCES"

# echo "Building elasticsearch source rpm ..."
# /usr/bin/rpmbuild -bs --nodeps SPECS/elasticsearch.spec
# echo "Finished building elasticsearch source rpm"

echo "Building elasticsearch binary rpm ..."
/usr/bin/rpmbuild -bb ${RPMBUILD}/SPECS/elasticsearch.spec || exit 1
echo "Finished building elasticsearch binary rpm ..."

echo "Copying elasticsearch binary rpm to ${MODULEDIR}/target ..."
mkdir -p ${MODULEDIR}/target
#rm -rf ${MODULEDIR}/target/*
cp -R ${RPMBUILD}/RPMS/* ${MODULEDIR}/target/
echo "Copied elasticsearch binary rpm to ${MODULEDIR}/target ..."

echo "Uploading rpm to pulp repository ..."
/usr/bin/pulp-admin -u admin -p admin content upload --nosig --verbose --dir ${MODULEDIR}/target/x86_64/ || exit 1
echo "Uploaded rpm to pulp repository"

popd

echo "Finished building RPM for elasticsearch ${VERSION}"
