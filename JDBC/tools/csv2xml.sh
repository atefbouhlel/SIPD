#!/bin/sh

OUTFILE=$(basename $1 .csv).xml
TMPFILE=$(basename $1 .csv).tmp
INTFILE=$(basename $1 .csv)_.tmp

rm -f $TMPFILE $INTFILE

echo "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" > $OUTFILE
echo "<Delta>" >> $OUTFILE
echo "	<Patient ID=\"1\"/>" >> $OUTFILE
echo "	<Routing>" >> $OUTFILE
echo "		<Source Serveur=\"0\"/>" >> $OUTFILE
echo "		<Dest Serveur=\"1\"/>" >> $OUTFILE
echo "	</Routing>" >> $OUTFILE
echo "	<ACK>12788</ACK>" >> $OUTFILE
echo "	<MAJ>" >> $OUTFILE

# Params: <input file> <table name>
run()
{
	TABLE=$2
	echo "		<Table name=\""$TABLE"\">" >> $OUTFILE
	grep "^"$TABLE";" $1 > $TMPFILE

	while [ $(cat $TMPFILE | wc -l) != 0 ]
	do
		ID=$(head -n 1 $TMPFILE | cut -d ";" -f 2)
		if [ -n $ID ]; then
			AUTHOR=$(head -n 1 $TMPFILE | cut -d ";" -f 3)
			TSSPT=$(head -n 1 $TMPFILE | cut -d ";" -f 4)
			TSS=$(head -n 1 $TMPFILE | cut -d ";" -f 5)
			echo "			<T i=\""$ID"\" a=\""$AUTHOR"\" p=\""$TSSPT"\" s=\""$TSS"\">" >> $OUTFILE
			grep "^"$TABLE";"$ID";" $TMPFILE | \
				sed "s/^"$TABLE";[0-9]*;[0-9]*;[0-9]*;[0-9]*;//g;s/\[NULL\]/\[\]/g;s/\\u/\\\\u/g;s/\x27/\\\'/g" | \
				while read i
			do
				echo "				<A>"$i"</A>" >> $OUTFILE
			done
			echo "			</T>" >> $OUTFILE
		fi
		grep -v "^"$TABLE";"$ID";" $TMPFILE > $INTFILE
		mv $INTFILE $TMPFILE
	done
	echo "		</Table>" >> $OUTFILE
}

run $1 UserDMSP
run $1 Role
run $1 Habilitation
run $1 Formulaire
run $1 Episode
run $1 Event
run $1 Comment
run $1 Info
run $1 Matrice_DMSP
run $1 Matrice_Patient

echo "	</MAJ>" >> $OUTFILE
echo "	<DELETE>" >> $OUTFILE
echo "	</DELETE>" >> $OUTFILE
echo "</Delta>" >> $OUTFILE

rm -f $TMPFILE $INTFILE
