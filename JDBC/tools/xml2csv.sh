#!/bin/sh

# Transforms given XML delta file to CSV format
# Run:
#   xml2csv <xml file>
# Needs Xalan and JRE installed (check these paths)

XALAN_PATH=../../tools/xalan-j_2_7_1
#XALAN_PATH=d:/dev/tools/xalan-j_2_7_1

#
# Resulting CSV file will be placed to <xml file>.csv
#

CLASSPATH=$XALAN_PATH/xalan.jar:$XALAN_PATH/serializer.jar:$XALAN_PATH/xercesImpl.jar:$XALAN_PATH/xml-apis.jar
OUTFILE=$(basename $1 .xml).csv

echo Deleting output file...
rm -f $OUTFILE
echo File deleted OK. Start transformation...

java -cp $CLASSPATH org.apache.xalan.xslt.Process -DIAG -IN $1 -XSL xml2csv.xsl -OUT $OUTFILE
echo Transformation ended OK...
