package org.inria.dmsp.test.TEST_new_performance;

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
		run1();
		closeAll();

		initAll(dbmsHost);
		run2();
		closeAll();

		initAll(dbmsHost);
		run3();
		closeAll();

		run4( dbmsHost );
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

	protected void run1() throws Exception
	{
		// load the DB schema
		String schema = "/org/inria/dmsp/schemaV4.rsc";
		Tools_dmsp t = new Tools_dmsp(out);
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

	protected void run2() throws Exception
	{
		/*
		 *  LOAD DELTA FROM SANTEOS:
		 *  Can be replace by a call to synchro API on Gemalto
		 *  NB : delta_shaoyi091102.enc encrypted delta
		 *       delta_shaoyi091102.xml clear delta
		 *       fromSanteos-2009-11-02.csv same data under the form of csv file
		 */
		DeltaLoader dl = new DeltaLoader(out, perf);	// instantiate delta loader
		InputStreamReader fr =		// open the delta file
			new InputStreamReader(Test.class.getResourceAsStream("delta.csv"));
		dl.LoadDelta(fr, db);		// load the data into database
	}

	protected void run3() throws Exception
	{
		/*
		 *  Generate 200 more intervenants
		 */
		// instantiate the IntervenantsGenerator
		IntervenantsGenerator ig = new IntervenantsGenerator(out, perf);
		// generate another 20 intervenants
		ig.INSERT_Data_Generated(200000,20,db);
	}

	protected void run4(String dbmsHost) throws Exception
	{
		init();
		openConnection(dbmsHost,
				new String [] {"1022", "1018"}); // Dupont / Medecin

		/****************************************************************
		 *  Begin Performance Test
		 ****************************************************************/
		//--
		// Q8: Insert a big form (GIR)
		//--
		logMsg("========== Q8 ==========");

		System.gc(); // runs the garbage collector *before*, it can't hurt
		((org.inria.jdbc.Connection) db).resetDBMSCallTime(); 
		long start = System.currentTimeMillis(), end, duration;

		insert(db, 0 /* fiche de liaison */, 1 /* GIR medical */);

		end = System.currentTimeMillis();
		duration = ((org.inria.jdbc.Connection) db).getDBMSCallTime("Q8");
		if (perf == 1)
		{
			System.out.println("Q8;" + (end - start) + ";" + duration);
		}

		/*
		 * Commit and clean exit
		 */
		Shutdown_DBMS();
		openConnection(dbmsHost, null);
		Desinstall_DBMS_MetaData();
		Shutdown_DBMS();
	}
	protected void insert(java.sql.Connection db, int nb_sform, int nb_bform) throws Exception
	{
		java.sql.PreparedStatement ps;
		int t2, t3, tmp_ev, // temporary vars
			no_co = 3;		// idglobal of no comment is 3

		// insert small forms (fiche de liaison)
		for (int i = 0; i < nb_sform; i++)
		{
			// insert 1 tuple into EVENT 
			logMsg("// Insertion in table EVENT");
			ps = ((org.inria.jdbc.Connection)db).prepareStatement(DMSP_QEP_IDs.EP_UI.OEP_EVENT_AC_INSERT, java.sql.Statement.RETURN_GENERATED_KEYS);
			int nb = Tools_dmsp.Test_EVENT_INSERT(0, 0,
					1012 /*formid*/, 1021 /*userid*/, 
					2 /*no_ep*/, java.sql.Date.valueOf("2000-10-20"), 
					java.sql.Date.valueOf("2000-10-20"), 12 /*filtre*/, ps);
			logMsg(nb+" tuple inserted...");
			// ask for generated key
			tmp_ev = getIdGlobalGetGeneratedKey(ps.getGeneratedKeys());

			// insert 1 tuple into COMMENT
			logMsg("// Insertion in table COMMENT");
			ps = ((org.inria.jdbc.Connection)db).prepareStatement(DMSP_QEP_IDs.EP_UI.OEP_COMMENT_NOAC_INSERT, java.sql.Statement.RETURN_GENERATED_KEYS);
			nb = Tools_dmsp.Test_COMMENT_INSERT(0, 0,
					"comment can be big, but more than 450 bytes can't be written", ps);
			logMsg(nb+" tuple inserted...");

			// insert 6 tuples into INFO, the first is linked to the comment
			t3 = 0; // current value of field info.position 

			for (int j = 0; j < 6; j++)
			{
				// to simulate the index on <IdGlobal, IdIntern>, link every info to the first comment
				t2 = no_co; // link info to the comment no_comment

				logMsg("// Insertion in table INFO");
				ps = ((org.inria.jdbc.Connection)db).prepareStatement(DMSP_QEP_IDs.EP_UI.OEP_INFO_AC_INSERT, java.sql.Statement.RETURN_GENERATED_KEYS);
				nb = Tools_dmsp.Test_INFO_INSERT(
					0, 0, tmp_ev /*event*/, t2 /*comment*/, "char value", i++,
					java.sql.Date.valueOf("2000-10-20"), 
					t3++ /*position*/, 1 /*filtre*/, j, ps);
				logMsg(nb+" tuple inserted...");
			}
			// commit;
			Save_DBMS_on_disk();
			// check form insertion:
			if (perf==0)
			{
				logMsg("SELECT INSERTED FORM (fiche de liaison):");
				ps = ((org.inria.jdbc.Connection)db).prepareStatement(DMSP_QEP_IDs.EP_UI.EP_QUERY_AC_SELECT_EVENT_INFO_COMMENT_BY_EVENT);
				lireResultSet(Tools_dmsp.Test_SELECT_BY_INT(tmp_ev, ps), out); 
			}
		}

		// insert big forms (GIR Med)
		for (int i = 0; i < nb_bform; i++)
		{
			// one big form is generated after several small forms
			//insert 1 tuple into EVENT
			logMsg("// Insertion in table EVENT");
			ps = ((org.inria.jdbc.Connection)db).prepareStatement(DMSP_QEP_IDs.EP_UI.OEP_EVENT_AC_INSERT, java.sql.Statement.RETURN_GENERATED_KEYS);
			int nb = Tools_dmsp.Test_EVENT_INSERT(
					0, 0, 1006 /*GIR medical*/, 1021, 2, 
					java.sql.Date.valueOf("2000-10-20"), 
					java.sql.Date.valueOf("2000-10-20"), 12, ps);
			logMsg(nb+" tuple inserted...");
			tmp_ev = getIdGlobalGetGeneratedKey(ps.getGeneratedKeys());

			// to simulate the index on <IdGlobal, IdIntern>, link every info to the first comment
			t2 = no_co; // link info to the comment no_comment

			// insert 73 tuples into INFO
			t3 = 0; // current value of field info.position 
			for (int j = 0; j < 73; j++)
			{
				logMsg("// Insertion in table INFO");
				ps = ((org.inria.jdbc.Connection)db).prepareStatement(DMSP_QEP_IDs.EP_UI.OEP_INFO_AC_INSERT, java.sql.Statement.RETURN_GENERATED_KEYS);
				nb = Tools_dmsp.Test_INFO_INSERT(
					0, 0, tmp_ev /* event*/, t2 /*comment*/, "char value", i,
					java.sql.Date.valueOf("2000-10-20"), 
					t3++ /*position*/, 1 /*filtre*/, j, ps);
				logMsg(nb+" tuple inserted...");
			}

			// insert 24 tuples into COMMENT
			logMsg("// Insertion in table COMMENT");
			ps = ((org.inria.jdbc.Connection)db).prepareStatement(DMSP_QEP_IDs.EP_UI.OEP_COMMENT_NOAC_INSERT, java.sql.Statement.RETURN_GENERATED_KEYS);
			nb = Tools_dmsp.Test_COMMENT_INSERT(0, 0,
					"comment can be big, but more than 450 bytes can't be written", ps);
			logMsg(nb+" tuple inserted...");

			Save_DBMS_on_disk();	// commit
			// check form insertion:
			if (perf==0)
			{
				logMsg("SELECT INSERTED FORM (GIR Medical):");
				ps = ((org.inria.jdbc.Connection)db).prepareStatement(DMSP_QEP_IDs.EP_UI.EP_QUERY_AC_SELECT_EVENT_INFO_COMMENT_BY_EVENT);
				lireResultSet(Tools_dmsp.Test_SELECT_BY_INT(tmp_ev, ps), out);
			}
		}
	}
}
