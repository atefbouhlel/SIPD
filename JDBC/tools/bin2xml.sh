#!/bin/sh

XMLFILE=$(basename $1 .bin).xml
TMPFILE=$(basename $1 .bin).tmp
MACFILE=$(basename $1 .bin).mac

rm -f $XMLFILE $TMPFILE $MACFILE
openssl enc -d -in $1 -out $TMPFILE -aes-128-cbc -K 4a6176612052756c65732121203a2d29 -nosalt -iv 00
sed 's/></>\n</g' $TMPFILE > $XMLFILE
openssl enc -e -in $1 -out $TMPFILE -aes-128-cbc -K 444d535020666f722065766572202121 -nopad -iv 00
xxd -s -16 -len 8 -p $TMPFILE > $MACFILE
rm -f $TMPFILE
printf "MAC = "
cat $MACFILE
printf "LastUpdate = "
grep " p=" $XMLFILE | sed 's/.*p=\"//g;s/\".*//g' | sort -r | head -1
