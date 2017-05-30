@echo off
set ANT_HOME=C:\eclipse\plugins\org.apache.ant_1.7.1.v20090120-1145
set JAVA_HOME=C:\jdk6

rem set TST=org.inria.dmsp.test.TEST_returnDELETE
set TST=%2

set TEST=
if %1==test set TEST=-DTSTCASE=%TST%.Test

%ANT_HOME%\bin\ant.bat -q -f build_inria.xml %TEST% %1 > log_%TST%.txt
