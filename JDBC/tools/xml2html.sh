#!/bin/sh

# Transforms given XML delta file to HTML format
# Run:
#   xml2html <xml file>
# Needs Xalan and JRE installed (check these paths)

XALAN_PATH=../../tools/xalan-j_2_7_1
#XALAN_PATH=d:/dev/tools/xalan-j_2_7_1

#
# Resulting HTML file will be placed to <xml file>.html
#

CLASSPATH=$XALAN_PATH/xalan.jar:$XALAN_PATH/serializer.jar:$XALAN_PATH/xercesImpl.jar:$XALAN_PATH/xml-apis.jar
OUTFILE=$(basename $1 .xml).html

echo Deleting output file...
rm -f $OUTFILE
echo File deleted OK. Start transformation...

java -cp $CLASSPATH org.apache.xalan.xslt.Process -DIAG -IN $1 -XSL xml2html.xsl -OUT $OUTFILE
echo Transformation ended OK...
