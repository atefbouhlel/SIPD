package org.inria.dmsp.test.TEST_emptydb;
/*
J'ai un problème avec la commande CMD_EMPTY_DATABASE (14)
TRACE_CALL;14;;;0;;;

Dès que je fait un eraseData pour reloader un delta initial, il y a un arret sur un EP(voir pièce jointe), le SPT reste bloqué sur le dernier EP du fichier, il manque le ;1 0 0 0 ;
Lorsque la database est vierge et que je passe le delta sans eraseData, pas de problème.
 */

// IMPORTS THAT CAN BE CHANGED TO TEST OTHER PLANS //
/////////////////////////////////////////////////////


/////////////////////////////////////////////////////////////////////////
// TEST CODE WHICH SHOULD REMAIN THE SAME WHATEVER THE IMPORTED PLANS  //
/////////////////////////////////////////////////////////////////////////

import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;		// to produce the output of the test

import org.inria.database.QEPng;
//import org.inria.dmsp.EP_Debug;
//import org.inria.dmsp.EP_Synchro;
//import org.inria.dmsp.EP_UI;
import org.inria.dmsp.debug.TracesReplayer;
//import org.inria.dmsp.schema.EP_TEST;
import org.inria.dmsp.tools.DeltaLoader;
import org.inria.dmsp.tools.Tools_dmsp;

import test.jdbc.Tools;			// tools specified for any DB schema
import test.runner.ITest;		// super class

//import java.io.PrintWriter;

//import test.jdbc.Tools;
//import test.runner.ITest;

public class Test extends Tools implements ITest
{
	@Override
	public void run(PrintWriter out, String dbmsHost) throws Exception
	{
		// set output stream and instantiate the tools:		
		this.out = out;

		perf = 1;	// set it to !=0 to avoid logging in DeltaLoader.loadDelta()

		//========= Load initial database dump
		init();							// initialize the driver
//		openConnection(dbmsHost, null);	// connect without authentication
//		run_loaddelta( out );
//		Save_DBMS_on_disk();	// commit
//		Shutdown_DBMS();		// exit

		perf = 0;	// this not a performance test ==> output is produced

		//========= Dump its content
//		openConnection(dbmsHost, new String [] {"1026","1018"}); // user 1026(SAGE-CH0162), role 1018(medecin)
//		openConnection(dbmsHost, new String [] {"1024","1019"}); // user 1024(Lefebvre), role 1019(referentSocial)
//		openConnection(dbmsHost, null);

		//========= Replay traces to update some records
		run_traces( dbmsHost );

//		select_star();	// select * from *
//		run_select( dbmsHost );
		Save_DBMS_on_disk();	// commit
		Shutdown_DBMS();		// exit
	}

	protected void run_loaddelta(PrintWriter out) throws Exception
	{
		Tools_dmsp t = new Tools_dmsp(out);
		// load the DB schema
		String schema = "/org/inria/dmsp/schemaV4.rsc";
		byte[] schemaDesc = t.loadSchema(schema);
		int usedId = 404;
		Install_DBMS_MetaData(schemaDesc, usedId);
		schemaDesc = null;
		
		// load and install QEPs
		Class<?>[] executionPlans = new Class[] { org.inria.dmsp.EP_Synchro.class, org.inria.dmsp.schema.EP_TEST.class, 
				org.inria.dmsp.EP_UI.class, org.inria.dmsp.EP_PDS.class };
		QEPng.loadExecutionPlans( org.inria.dmsp.tools.DMSP_QEP_IDs.class, executionPlans );
		QEPng.installExecutionPlans( db );

		// instantiate delta loader
		DeltaLoader dl = new DeltaLoader(out, perf); 
		// open the delta file
		InputStreamReader fr = new InputStreamReader(Test.class.getResourceAsStream("delta.csv"));
		dl.LoadDelta(fr, db);	// load the data into database
	}

	///
	/// load the traces and replay the process
	///
	protected void run_traces(String dbmsHost) throws Exception
	{
		// open the traces file
//		InputStreamReader fr = new InputStreamReader(Test.class.getResourceAsStream("traces.txt"));
		FileReader fr = new FileReader("D:/dev/JDBC/src/jdbc-tests/org/inria/dmsp/test/TEST_emptydb/traces.txt");
		// replay traces
		TracesReplayer tr = new TracesReplayer();
		tr.replayTraces(fr, dbmsHost);
	}
}
