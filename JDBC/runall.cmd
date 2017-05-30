@echo off

rem call build.cmd clean
rem call build.cmd build

for /f "eol=#" %%i in (tests.lst) do (

rem del /q NAND_FTL_on_Disk.bin NAND_PHY_on_Disk.bin NOR_on_Disk.bin
rem start ..\dbms.exe

call build.cmd test %%i

rem ..\tools\pskill -t dbms.exe

)
