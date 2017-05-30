@echo off

echo ****************************************************
echo Runs the tearing tests simples:
echo tear the token during DB modifications then recover
echo ****************************************************

echo - Launching the configuration...
call tearing_tests\vars.cmd
echo - done.

echo MAKE SURE YOU HAVE
echo %DBMS_EXE% and %PSKILL% compiled and installed to proceed with the tests
echo You can stop now or continue this script
pause

mkdir %TEST_OUTPUT%

for /f "eol=#" %%i in ('dir /b /ad %TEST_HOME%') do if exist %TEST_HOME%\%%i\1pass.lst (
	for /f "eol=#" %%j in (%TEST_HOME%\%%i\1pass.lst) do (
		echo **
		echo ** %%i.%%j
		echo **

		echo =========== 1 STAGE ===========
		echo - Launch the DBMS from scratch and wait for its setup...
		start /min %DBMS_EXE%
		for /l %%a in (%SLEEP_SETUPTIME%,-1,1) do (title %title% continue in %%a seconds & ping -n 2 -w 1 127.0.0.1>NUL)
		echo - done.

		echo - Fork %TEST_PKG%.%%i.%%j.Test, 1st part of the code...
rem Here we use CMD to be able to redirect the output. START alone does not support redirecting the output of the inner command
		start /min cmd /C %JAVA_PROCESSNAME% -cp %TEST_CLASSES% %TEST_RUNNER% %TEST_PKG%.%%i.%%j.Test %DBMS_HOST% 1 ^> %TEST_OUTPUT%\%%i_%%j_output-1.txt
		for /l %%a in (%SLEEP_TEARINGTIME%,-1,1) do (title %title% continue in %%a seconds & ping -n 2 -w 1 127.0.0.1>NUL)
		echo - done, both processes should be suspended now.

		echo - Kill %JAVA_PROCESSNAME% then %DBMS_PROCESSNAME%...
		%PSKILL% %JAVA_PROCESSNAME%
		%PSKILL% %DBMS_PROCESSNAME%
		for /l %%a in (%SLEEP_SETUPTIME%,-1,1) do (title %title% continue in %%a seconds & ping -n 2 -w 1 127.0.0.1>NUL)
		echo - done.

		echo =========== 2 STAGE ===========
		echo - Launch the DBMS again and let it recover itself...
rem 1st argument tells DBMS that metadata is already installed.
		start /min %DBMS_EXE% -n
		for /l %%a in (%SLEEP_SETUPTIME%,-1,1) do (title %title% continue in %%a seconds & ping -n 2 -w 1 127.0.0.1>NUL)
		echo - done.

		echo - Launch %TEST_PKG%.%%i.%%j.Test, 2nd part of the code...
		%JAVA_PROCESSNAME% -cp %TEST_CLASSES% %TEST_RUNNER% %TEST_PKG%.%%i.%%j.Test %DBMS_HOST% 2 > %TEST_OUTPUT%\%%i_%%j_output-2.txt
		for /l %%a in (%SLEEP_SELECTSTARTIME%,-1,1) do (title %title% continue in %%a seconds & ping -n 2 -w 1 127.0.0.1>NUL)
		echo - done.

		echo - Kill %DBMS_PROCESSNAME%...
		%PSKILL% %DBMS_PROCESSNAME%
		echo - done.

		echo - Diff...
		fc %TEST_REFOUTPUT%\%%i\%%j\output-1.txt %TEST_OUTPUT%\%%i_%%j_output-1.txt > %TEST_OUTPUT%\%%i_%%j_output-1.diff
		fc %TEST_REFOUTPUT%\%%i\%%j\output-2.txt %TEST_OUTPUT%\%%i_%%j_output-2.txt > %TEST_OUTPUT%\%%i_%%j_output-2.diff
		echo - done.

		echo - Cleaning up...
		del %DBMS_NANDPHY%
		del %DBMS_NANDFTL%
		del %DBMS_NOR%
		echo - done.
	)
)

echo **
echo **
echo ** DONE (tearing tests simples)
echo ***********************************

pause
