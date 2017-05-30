rem *********************
rem DBMS
rem *********************
set DBMS_PROCESSNAME=dbmsD.exe
set DBMS_EXE=..\%DBMS_PROCESSNAME%
set DBMS_HOST=localhost

rem If simulated on a PC, need to erase these files to simulate the DBMS reset
set DBMS_NANDPHY=.\NAND_PHY_on_Disk.bin
set DBMS_NANDFTL=.\NAND_FTL_on_Disk.bin
set DBMS_NOR=.\NOR_on_Disk.bin

rem *********************
rem JDBC
rem *********************
set TEST_HOME=%CD%\tearing_tests
set TEST_CLASSES=%TEST_HOME%\..\classes
set TEST_REFOUTPUT=%TEST_HOME%\..\src\jdbc-tests\org\inria\dmsp\test\TEST_Tearing
set TEST_OUTPUT=%TEST_HOME%\..\tmp
set TEST_RUNNER=test.runner.MainRunner
set TEST_PKG=org.inria.dmsp.test.TEST_Tearing
set JAVA_PROCESSNAME=java.exe

rem *********************
rem PATHS
rem *********************
rem Please take PSKill from SysInternals suite...
set PSKILL=..\tools\pskill.exe
set SLEEP_TEARINGTIME=40
set SLEEP_SETUPTIME=20
set SLEEP_SELECTSTARTIME=1
