@echo off

rem Transforms given XML delta file to HTML format
rem Run:
rem   check <xml file>
rem Needs Xalan and JRE installed (check these paths)

set JAVA_HOME=C:\jdk6
set XALAN_PATH=..\..\tools\xalan-j_2_7_1

rem
rem Resulting HTML file will be placed to <xml file>.csv
rem

set PATH=%PATH%;%JAVA_HOME%\bin
set CLASSPATH=%CLASSPATH%;%XALAN_PATH%\xalan.jar;%XALAN_PATH%\serializer.jar;%XALAN_PATH%\xercesImpl.jar;%XALAN_PATH%\xml-apis.jar

echo Deleting output file...
del /f /q %1.html
echo File deleted OK. Start transformation...

java org.apache.xalan.xslt.Process -DIAG -IN %1 -XSL xml2html.xsl -OUT %1.html
echo Transformation ended OK...
