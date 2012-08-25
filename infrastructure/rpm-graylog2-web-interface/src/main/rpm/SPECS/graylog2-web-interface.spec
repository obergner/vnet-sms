Name:           graylog2-web-interface
Version:        ${rpm.version}
Release:        ${buildNumber}%{?dist}
Summary:        A web interface for graylog2-server 
# Some of the gems compile, and thus this can't be noarch
BuildArch:      x86_64
Group:          Application/Internet
License:        GPL
URL:            http://www.graylog2.org/
# XXX You'll have to create the logrotate script for your application
Source0:        https://github.com/downloads/Graylog2/graylog2-web-interface/%{name}-%{version}.tar.gz
Source1:        %{name}-logrotate.conf
Source2:        %{name}-email.yml
Source3:        %{name}-general.yml
Source4:        %{name}-indexer.yml
Source5:        %{name}-monogid.yml
BuildRoot:      %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)

# XXX Building this rpm requires that bundle is available on the path of the user
# that is running rpmbuild. But I'm not sure how to require that there is a 
# bundle in the path. 
#BuildRequires: /usr/bin/bundle
# XXX Also require gem on the PATH
# BuildRequires: /usr/bin/gem

# From https://github.com/bernd/fpm-recipes/blob/master/graylog2-web/recipe.rb
BuildRequires: rubygems

Requires: graylog2-server
# XXX Remove if not using apache/passenger
# Assuming will run via passenger + apache
Requires: mod_passenger, httpd
# In order to rotate the logs
Requires: logrotate

#
# DIRS
# - Trying to follow Linux file system hierarchy
#
%define appdir %{_datarootdir}/%{name}
%define libdir %{_libdir}/%{name}
%define logdir /var/log/%{name}
%define configdir /etc/%{name}
%define cachedir /var/cache/%{name}
%define datadir /var/lib/%{name}
%define logrotatedir /etc/logrotate.d/

%description
Some description of the application

%prep
%setup -q -n %{name}-%{version}

%build
# Install all required gems into ./vendor/bundle using the handy bundle commmand
bundle install --deployment

# For some reason bundler doesn't install itself, this is probably right,
# but I guess it expects bundler to be on the server being deployed to 
# already. But the rails-helloworld app crashes on passenger looking for
# bundler, so it would seem to me to be required. So, I used gem to install
# bundler after bundle deployment. :) And the app then works under passenger.

PWD=`pwd`
cat > gemrc <<EOGEMRC
gemhome: $PWD/vendor/bundle/ruby/1.8
gempath:
- $PWD/vendor/bundle/ruby/1.8
EOGEMRC
gem --config-file ./gemrc install bundler
# Don't need the gemrc any more...
rm ./gemrc

# Some of the files in here have /usr/local/bin/ruby set as the bang
# but that won't work, and makes the rpmbuild process add /usr/local/bin/ruby
# to the dependencies. So I'm changing that here. Either way it prob won't
# work. But at least this rids us of the dependencie that we can never meet.
for f in `grep -ril "\/usr\/local\/bin\/ruby" ./vendor`; do
        sed -i "s|/usr/local/bin/ruby|/usr/bin/ruby|g" $f
        head -1 $f
done


%install
# Create all the defined directories
mkdir -p $RPM_BUILD_ROOT/%{appdir}
mkdir -p $RPM_BUILD_ROOT/%{libdir}
mkdir -p $RPM_BUILD_ROOT/%{logdir}
mkdir -p $RPM_BUILD_ROOT/%{configdir}
mkdir -p $RPM_BUILD_ROOT/%{cachedir}
mkdir -p $RPM_BUILD_ROOT/%{datadir}
mkdir -p $RPM_BUILD_ROOT/%{logrotatedir}


# Start moving files into the proper place in the build root

# 
# Config
#

cp %{SOURCE2} $RPM_BUILD_ROOT/%{configdir}/email.yml
cp %{SOURCE3} $RPM_BUILD_ROOT/%{configdir}/general.yml
cp %{SOURCE4} $RPM_BUILD_ROOT/%{configdir}/indexer.yml
cp %{SOURCE5} $RPM_BUILD_ROOT/%{configdir}/mongoid.yml
pushd config
    ln -s %{configdir}/email.yml ./email.yml
    ln -s %{configdir}/general.yml ./general.yml
    ln -s %{configdir}/indexer.yml ./indexer.yml
    ln -s %{configdir}/mongoid.yml ./mongoid.yml
popd

#
# lib
# 

mv ./vendor $RPM_BUILD_ROOT/%{libdir}
ln -s %{libdir}/vendor ./vendor

#
# tmp/cache
#

mv ./tmp $RPM_BUILD_ROOT/%{cachedir}
ln -s %{cachedir}/tmp ./tmp

#
# log
#

# Only do logdir not logdir/log
rm -rf ./log
ln -s %{logdir} ./log

#
# Everything left goes in appdir
#

mv ./* $RPM_BUILD_ROOT/%{appdir} 

#
# logrotate
#
cp %{SOURCE1} $RPM_BUILD_ROOT/%{logrotatedir}/%{name}

%clean
rm -rf $RPM_BUILD_ROOT

%files
%defattr(-,root,root,-)
%{appdir}
%{libdir}
%config %{configdir}/email.yml
%config %{configdir}/general.yml
%config %{configdir}/indexer.yml
%config %{configdir}/mongoid.yml
# passenger runs as nobody apparently and then http as apache, and I'm not sure which
# needs which...so for now do nobody:apache...wonder if it should be set to run as apache?
%attr(770,nobody,apache) %{logdir}
%attr(770,nobody,apache) %{cachedir}
# %dir allows an empty directory, which this will be at an initial install
%attr(770,nobody,apache) %dir %{datadir}
%{logrotatedir}/%{name}
%doc

