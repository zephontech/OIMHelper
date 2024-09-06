@echo off
setlocal

set CLASSPATH=.;C:\javalibs\OIM11GClient\oimclient.jar;C:\javalibs\WLClient10.3\wlfullclient.jar;.\dist\OIMWrapper.jar
set CLASSPATH=%CLASSPATH%;C:\javalibs\OIM11GClient\lib\spring.jar;C:\javalibs\OIM11GClient\lib\commons-logging.jar
set CLASSPATH=%CLASSPATH%;C:\javalibs\OIM11GClient\lib\eclipselink.jar;C:\javalibs\OIM11GExt\csv.jar
set CLASSPATH=%CLASSPATH%;C:\javalibs\jdbc\mysql-connector-java-5.0.5.jar;C:\javalibs\OIM11Gext\log4j-1.2.8.jar
set CLASSPATH=%CLASSPATH%;C:\javalibs\OIM11Gext\jakarta-commons\commons-lang-2.3.jar;C:\javalibs\OIM11Gext\jakarta-commons\commons-io-1.1.jar
set CLASSPATH=%CLASSPATH%;C:\javalibs\OIM11GClient\lib\commons-logging.jar
REM Properties and OIM Config

set CLASSPATH=%CLASSPATH%;C:\javalibs\OIM11GClient\conf
set SYSOPTS=-DXL.HomeDir=C:\javalibs\OIM11GClient -Djava.security.auth.login.config=C:\javalibs\OIM11GClient\conf\authwl.conf

REM set APIPATH=c:\javalibs\OIM11GLib\xlAPI.jar;c:\javalibs\OIM11GLib\xlDataObjectBeans.jar

REM java %SYSOPTS% -cp %CLASSPATH% org.oimwrapper.api.tester.ClientTesterConnection %1
REM java %SYSOPTS% -cp %CLASSPATH% org.oimwrapper.api.tester.ClientTesterForms %1
REM java %SYSOPTS% -cp %CLASSPATH% org.oimwrapper.api.tester.ClientTesterITRes %1
REM java %SYSOPTS% -cp %CLASSPATH% org.oimwrapper.api.tester.ClientTesterLookup %1
REM java %SYSOPTS% -cp %CLASSPATH% org.oimwrapper.api.tester.ClientTesterProps %1
REM java %SYSOPTS% -cp %CLASSPATH% org.oimwrapper.api.tester.ClientTesterRecon -filename testrecon_userpolicy.csv
REM java %SYSOPTS% -cp %CLASSPATH% org.oimwrapper.api.tester.ClientTesterRetryReconEvent %1
REM java %SYSOPTS% -cp %CLASSPATH% org.oimwrapper.api.tester.ClientTesterRoles %1
REM java %SYSOPTS% -cp %CLASSPATH% org.oimwrapper.api.tester.ClientTesterUser %1
REM java %SYSOPTS% -cp %CLASSPATH% org.oimwrapper.api.tester.ClientTesterExternalDB
REM java %SYSOPTS% -cp %CLASSPATH%;%APIPATH% org.oimwrapper.api.tester.ClientTesterInternalDB
REM java %SYSOPTS% -cp %CLASSPATH% org.oimwrapper.api.tools.ImportExportResourceProperties -oper import -filename itresprops.txt
REM java %SYSOPTS% -cp %CLASSPATH% org.oimwrapper.api.tools.ImportExportResourceProperties -oper export
java %SYSOPTS% -cp %CLASSPATH%;%APIPATH% org.oimwrapper.api.tester.ShowRequestData %1



endlocal
