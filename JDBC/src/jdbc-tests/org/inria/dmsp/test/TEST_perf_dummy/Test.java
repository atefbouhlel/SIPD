package org.inria.dmsp.test.TEST_perf_dummy;

/////////////////////////////////////////////////////
//IMPORTS THAT CAN BE CHANGED TO TEST OTHER PLANS //
/////////////////////////////////////////////////////

///////////////////////////////
// DO NOT CHANGE AFTER HERE  //
///////////////////////////////

import java.io.InputStreamReader;
import java.io.PrintWriter;		// to produce the output of the test

import org.inria.database.QEPng;
import org.inria.dmsp.tools.DMSP_QEP_IDs;
import org.inria.dmsp.tools.DeltaLoader;
import org.inria.dmsp.tools.IntervenantsGenerator;
import org.inria.dmsp.tools.Tools_dmsp;

import test.jdbc.Tools;
import test.runner.ITest;

public class Test extends Tools implements ITest
{
	@Override
	public void run(PrintWriter out, String dbmsHost) throws Exception
	{
		this.out = out;
		perf = 1;
		initAll(dbmsHost);
		run1(out);
		closeAll();

		initAll(dbmsHost);
		run2(out);
		closeAll();

		initAll(dbmsHost);
		run3(out);
		closeAll();

		run4(out, dbmsHost);
	}

	/// Utility method
	/// Prints given message on console if Tools.perf == 0
	protected void logMsg(String msg)
	{
		if (perf == 0)
		{
			out.println( msg );
		}
	}
	/// Utility method
	/// Initializes database driver and opens connection without authentication
	protected void initAll(String dbmsHost) throws Exception
	{
		init();							// initialize the driver
		openConnection(dbmsHost, null);	// connect without authentication
	}
	/// Utility method
	/// Saves database on disk and performs its shutdown
	protected void closeAll() throws Exception
	{
		Save_DBMS_on_disk();	// commit
		Shutdown_DBMS();		// exit
	}

	protected void run1(PrintWriter out) throws Exception
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

		/*
		 *  Generate the bench data
		 */
		// instantiate the IntervenantsGenerator
		IntervenantsGenerator ig = new IntervenantsGenerator(out, perf);
		ig.INSERT_Data_Generated(100000,20,db);	// generate 20 intervenants
	}

	protected void run2(PrintWriter out) throws Exception
	{
		/*
		 *  LOAD DELTA FROM SANTEOS:
		 *  Can be replace by a call to synchro API on Gemalto
		 *  NB : delta_shaoyi091102.enc encrypted delta
		 *       delta_shaoyi091102.xml clear delta
		 *       fromSanteos-2009-11-02.csv same data under the form of csv file
		 */
		DeltaLoader dl = new DeltaLoader(out, perf);	// instantiate delta loader
		InputStreamReader fr =	// open the delta file
			new InputStreamReader(Test.class.getResourceAsStream("delta.csv"));
		dl.LoadDelta(fr, db);	// load the data into database
	}

	protected void run3(PrintWriter out) throws Exception
	{
		/*
		 *  Generate 200 more intervenants
		 */
		// instantiate the IntervenantsGenerator
		IntervenantsGenerator ig = new IntervenantsGenerator(out, perf);
		ig.INSERT_Data_Generated(200000, 20, db);	// generate another 20 intervenants
	}

	protected void run4(PrintWriter out, String dbmsHost) throws Exception
	{
		init();
		openConnection(dbmsHost,
				new String [] {"1022", "1018"}); // Dupont / Medecin

		long start, end, duration;
		java.sql.ResultSet rs;			// the results' set used for the queries
		java.sql.Statement st;			// the statement for non-parametric select

		/****************************************************************
		 *  Begin Performance Test
		 ****************************************************************/

		st = db.createStatement();

		//--
		// Q1: Select 6 atts from Intervenant (ALL)
		//--
		logMsg("========== Q1 ==========");
		
		System.gc(); // runs the garbage collector *before*, it can't hurt
		((org.inria.jdbc.Connection) db).resetDBMSCallTime(); 
		start = System.currentTimeMillis();

		st = db.createStatement();
		rs = ((org.inria.jdbc.Statement)st).executeQuery(DMSP_QEP_IDs.EP_TEST.EP_QUERY_NOAC_SELECT_USER);
		lireResultSet(rs, out);

		end = System.currentTimeMillis();
		duration = ((org.inria.jdbc.Connection) db).getDBMSCallTime("Q1");
		if (perf == 1)
		{
			System.out.println("Q1;" + (end - start) + ";" + duration);
		}

		//--
		// Q2: Select 6 atts from Intervenant where name 'dd' < 'gg'
		//     (32 rows selected)
		//--
		logMsg("========== Q2 ==========");

		System.gc(); // runs the garbage collector *before*, it can't hurt
		((org.inria.jdbc.Connection) db).resetDBMSCallTime(); 
		start = System.currentTimeMillis();

		st = db.createStatement();
		rs = ((org.inria.jdbc.Statement)st).executeQuery(DMSP_QEP_IDs.EP_TEST.EP_QUERY_NOAC_SELECT_USER_DUMMY);
		lireResultSet(rs, out);

		end = System.currentTimeMillis();
		duration = ((org.inria.jdbc.Connection) db).getDBMSCallTime("Q2");
		if (perf == 1)
		{
			System.out.println("Q2;" + (end - start) + ";" + duration);
		}

		/*
		 * Commit and clean exit
		 */
		Shutdown_DBMS();
		openConnection(dbmsHost, null);
		Desinstall_DBMS_MetaData();
		Shutdown_DBMS();
	}
}
