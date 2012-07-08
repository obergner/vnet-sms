Name:		graylog2-server
Version:	${rpm.version}
Release:	${buildNumber}%{?dist}
Summary:	Graylog2 is an open source log management solution that stores your logs in ElasticSearch.
Group:		System Environment/Daemons 
License:	GPLv2
URL:		http://www.graylog2.org/
Source0:	https://github.com/downloads/Graylog2/graylog2-server/%{name}-%{version}.tar.gz
Source1:    graylog2-server.drl
Source2:    graylog2-server.conf
Source3:    graylog2-server.init
BuildRoot:	%{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)

Requires:       jpackage-utils
Requires:       java
Requires:       elasticsearch
Requires:       mongodb-server

Requires(post): chkconfig initscripts
Requires(pre):  chkconfig initscripts

%description
Graylog2 is an open source syslog implementation that stores your logs in ElasticSearch. It consists of a server written in Java that accepts your syslog messages via TCP or UDP and stores it in the database. The second part is a Ruby on Rails web interface that allows you to view the log messages.


%prep
%setup -q -n %{name}-%{version}


%build


%install
rm -rf $RPM_BUILD_ROOT
# Directories
%{__install} -p -d -m 0755 %{buildroot}%{_sysconfdir}/%{name}
%{__install} -p -d -m 0755 %{buildroot}%{_sysconfdir}/%{name}/rules
%{__install} -p -d -m 0755 %{buildroot}%{_datadir}/%{name}
%{__install} -p -d -m 0755 %{buildroot}%{_localstatedir}/log/%{name}

# Files
%{__install} -p -D -m 0755 %{SOURCE3} %{buildroot}%{_initrddir}/%{name}
%{__install} -p -D -m 0644 %{SOURCE1} %{buildroot}%{_sysconfdir}/%{name}/rules/%{name}.drl
%{__install} -p -D -m 0644 %{SOURCE2} %{buildroot}%{_sysconfdir}/%{name}/%{name}.conf

%{__install} -p -D -m 0644 %{name}.jar %{buildroot}%{_datadir}/%{name}/%{name}.jar


%clean
rm -rf $RPM_BUILD_ROOT


%post 
/sbin/chkconfig --add %{name}


%preun
if [[ $1 -ge 1 ]]
then
    /sbin/service %{name} stop > /dev/null 2>&1
    /sbin/chkconfig --del %{name}
fi


%files
%defattr(-,root,root,-)
%dir %{_sysconfdir}/%{name}
%dir %{_sysconfdir}/%{name}/rules
%config(noreplace) %{_sysconfdir}/%{name}/*.conf
%config(noreplace) %{_sysconfdir}/%{name}/rules/*.drl

%{_initrddir}/%{name}
%dir %{_datadir}/%{name}
%{_datadir}/%{name}/%{name}.jar
%dir %{_localstatedir}/log/%{name}


%changelog
* Mon Feb 6 2012 Daniel Aharon <daharon@sazze.com> - 0.9.6
- Update to 0.9.6
- Fix permissions for files/dirs.

* Mon May 16 2011 Daniel Aharon <daharon@sazze.com> - 0.9.5sazze1
- Modified Graylog2-server to better handle multiple rules in streams.

* Mon May 16 2011 Daniel Aharon <daharon@sazze.com> - 0.9.5p1
- Initial packaging for Fedora.
