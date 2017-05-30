package org.inria.dmsp.test.TEST_performance;

/////////////////////////////////////////////////////
//IMPORTS THAT CAN BE CHANGED TO TEST OTHER PLANS //
/////////////////////////////////////////////////////

///////////////////////////////
// DO NOT CHANGE AFTER HERE  //
///////////////////////////////

import java.io.InputStreamReader;
import java.io.PrintWriter;		// to produce the output of the test

import org.inria.dmsp.EP_UI;
import org.inria.dmsp.schema.EP_TEST;
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
		String schema = "/org/inria/dmsp/schema.rsc";
		byte[] schemaDesc = t.loadSchema(schema);
		int usedId = 404;
		Install_DBMS_MetaData(schemaDesc, usedId);
		schemaDesc = null;

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
		java.sql.PreparedStatement ps ;	// the prepared statement used for all inserts
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
		rs = st.executeQuery(EP_TEST.EP_QUERY_NOAC_SELECT_USER);
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

		ps = db.prepareStatement(EP_TEST.EP_QUERY_NOAC_SELECT_USER_BETWEEN_NOM);
		lireResultSet(Tools_dmsp.Test_SELECT_BY_STRING_AND_STRING("dd", "gg", ps), out);

		end = System.currentTimeMillis();
		duration = ((org.inria.jdbc.Connection) db).getDBMSCallTime("Q2");
		if (perf == 1)
		{
			System.out.println("Q2;" + (end - start) + ";" + duration);
		}

		//=======================================================//
		// Q3  - Select * from Fiche de liaison 
		//    Q3-a: select infos only (add conceptid) --> 900 lines (150 fiches de liaison)
		//    Q3-b: select a subset of Q3-a where yy < idevent < xx --> 90 lines (15 fiches de liaison)
		//    Q3-c: select infos+comments (witout conceptid, commentid)
		//    Q3-d: select a subset of Q3-c where yy < idevent < xx
		//=======================================================//	

		//  Q3-a: select infos only (add conceptid)
		logMsg("========== Q3-a ==========");
		System.gc(); // runs the garbage collector *before*, it can't hurt
		((org.inria.jdbc.Connection) db).resetDBMSCallTime(); 
		start = System.currentTimeMillis();

		logMsg("Q3-a: select infos only (add conceptid)");
		ps = db.prepareStatement(EP_UI.EP_QUERY_AC_SELECT_INFO_BY_FORM);
		rs = Tools_dmsp.Test_SELECT_BY_INT(1012, ps); 
		lireResultSet(rs, out); 

		end = System.currentTimeMillis();
		duration = ((org.inria.jdbc.Connection) db).getDBMSCallTime("Q3-a");
		if (perf == 1)
		{
			System.out.println("Q3-a;" + (end - start) + ";" + duration);
		}

		// Q3-b: select a subset of Q3-a where yy < idevent < xx
		logMsg("========== Q3-b ==========");
		System.gc(); // runs the garbage collector *before*, it can't hurt
		((org.inria.jdbc.Connection) db).resetDBMSCallTime(); 
		start = System.currentTimeMillis();

		logMsg("Q3-b: select a subset of Q3-a where yy < idevent < xx");
		ps = db.prepareStatement(EP_TEST.EP_QUERY_AC_SELECT_INFO_BY_FORM_BETWEEN_EVENT);
		rs = Tools_dmsp.Test_SELECT_BY_INT_AND_INT_AND_INT(1012, 2970, 3192, ps);
		lireResultSet(rs, out); 

		end = System.currentTimeMillis();
		duration = ((org.inria.jdbc.Connection) db).getDBMSCallTime("Q3-b");
		if (perf == 1)
		{
			System.out.println("Q3-b;" + (end - start) + ";" + duration);
		}

		// Q3-c: select infos+comments (witout conceptid, commentid)
		logMsg("========== Q3-c ==========");
		System.gc(); // runs the garbage collector *before*, it can't hurt
		((org.inria.jdbc.Connection) db).resetDBMSCallTime();
		start = System.currentTimeMillis();

		logMsg("Q3-c: select infos+comments (witout conceptid, commentid)");
		ps = db.prepareStatement(EP_TEST.EP_QUERY_AC_SELECT_INFO_COMMENT_BY_FORM);
		rs = Tools_dmsp.Test_SELECT_BY_INT(1012, ps);
		lireResultSet(rs, out);

		end = System.currentTimeMillis();
		duration = ((org.inria.jdbc.Connection) db).getDBMSCallTime("Q3-c");
		if (perf == 1)
		{
			System.out.println("Q3-c;" + (end - start) + ";" + duration);
		}

		// Q3-d: select a subset of Q3-c where yy < idevent < xx
		logMsg("========== Q3-d ==========");
		System.gc(); // runs the garbage collector *before*, it can't hurt
		((org.inria.jdbc.Connection) db).resetDBMSCallTime();
		start = System.currentTimeMillis();

		logMsg("Q3-d: select a subset of Q3-c where yy < idevent < xx");
		ps = db.prepareStatement(EP_TEST.EP_QUERY_AC_SELECT_INFO_COMMENT_BY_FORM_BETWEEN_EVENT);
		rs = Tools_dmsp.Test_SELECT_BY_INT_AND_INT_AND_INT(1012, 2970, 3192, ps);
		lireResultSet(rs, out);

		end = System.currentTimeMillis();
		duration = ((org.inria.jdbc.Connection) db).getDBMSCallTime("Q3-d");
		if (perf == 1)
		{
			System.out.println("Q3-d;" + (end - start) + ";" + duration);
		}

		Shutdown_DBMS();

		//=======================================================//
		// Q4 - query for la vue chronologique 
		//		Q4-a : without AC 
		//		Q4-b : with AC 
		//=======================================================//

		openConnection(dbmsHost,
				new String [] {"0", "1"}); // Willemant / Patient (no right to GIR Med, access to fiche de liaison)

		// without access control
		logMsg("========== Q4-a ==========");

		System.gc(); // runs the garbage collector *before*, it can't hurt
		((org.inria.jdbc.Connection) db).resetDBMSCallTime(); 
		start = System.currentTimeMillis();

		ps = db.prepareStatement(EP_UI.EP_QUERY_NOAC_SELECT_EVENT_FORM_USER_BY_FILTRE);
		lireResultSet(Tools_dmsp.Test_SELECT_BY_INT(1, ps), out);

		end = System.currentTimeMillis();
		duration = ((org.inria.jdbc.Connection) db).getDBMSCallTime("Q4-a");
		if (perf == 1)
		{
			System.out.println("Q4-noac;" + (end - start) + ";" + duration);
		}

		// with access control
		logMsg("========== Q4-b ==========");

		System.gc(); // runs the garbage collector *before*, it can't hurt
		((org.inria.jdbc.Connection) db).resetDBMSCallTime(); 
		start = System.currentTimeMillis();

		ps = db.prepareStatement(EP_UI.EP_QUERY_AC_SELECT_EVENT_FORM_USER_BY_FILTRE);
		lireResultSet(Tools_dmsp.Test_SELECT_BY_INT(1, ps), out);

		end = System.currentTimeMillis();
		duration = ((org.inria.jdbc.Connection) db).getDBMSCallTime("Q4-b");
		if (perf == 1)
		{
			System.out.println("Q4-ac;" + (end - start) + ";" + duration);
		}

		Shutdown_DBMS();

		//=======================================================//
		// Q5  - query for a small form (fiche de liaison)
		//		Q5-a : select first form with projection of event-info-comment
		//		Q5-b : select first form with projection of info-comment
		//		Q5-c : select last form with projection of event-info-comment
		//		Q5-d : select last form with projection of info-comment
		//=======================================================//	

		openConnection(dbmsHost,
				new String [] {"1022", "1018"}); // Dupont / Medecin

		// Q5-a
		logMsg("========== Q5-a ==========");

		System.gc(); // runs the garbage collector *before*, it can't hurt
		((org.inria.jdbc.Connection) db).resetDBMSCallTime(); 
		start = System.currentTimeMillis();

		ps = db.prepareStatement(EP_UI.EP_QUERY_AC_SELECT_EVENT_INFO_COMMENT_BY_EVENT);
		rs = Tools_dmsp.Test_SELECT_BY_INT(1332, ps); 	
		lireResultSet(rs, out); 

		end = System.currentTimeMillis();
		duration = ((org.inria.jdbc.Connection) db).getDBMSCallTime("Q5-a");
		if (perf == 1)
		{
			System.out.println("Q5-a;" + (end - start) + ";" + duration);
		}

		// Q5-b
		logMsg("========== Q5-b ==========");

		System.gc(); // runs the garbage collector *before*, it can't hurt
		((org.inria.jdbc.Connection) db).resetDBMSCallTime(); 
		start = System.currentTimeMillis();

		ps = db.prepareStatement(EP_TEST.EP_QUERY_AC_SELECT_EVENTnop_INFO_COMMENT_BY_EVENT);
		rs = Tools_dmsp.Test_SELECT_BY_INT(1332, ps); 	
		lireResultSet(rs, out); 

		end = System.currentTimeMillis();
		duration = ((org.inria.jdbc.Connection) db).getDBMSCallTime("Q5-b");
		if (perf == 1)
		{
			System.out.println("Q5-b;" + (end - start) + ";" + duration);
		}

		// Q5-c
		logMsg("========== Q5-c ==========");

		System.gc(); // runs the garbage collector *before*, it can't hurt
		((org.inria.jdbc.Connection) db).resetDBMSCallTime(); 
		start = System.currentTimeMillis();

		ps = db.prepareStatement(EP_UI.EP_QUERY_AC_SELECT_EVENT_INFO_COMMENT_BY_EVENT);
		rs = Tools_dmsp.Test_SELECT_BY_INT(3910, ps); 	
		lireResultSet(rs, out); 

		end = System.currentTimeMillis();
		duration = ((org.inria.jdbc.Connection) db).getDBMSCallTime("Q5-c");
		if (perf == 1)
		{
			System.out.println("Q5-c;" + (end - start) + ";" + duration);
		}

		// Q5-d
		logMsg("========== Q5-d ==========");

		System.gc(); // runs the garbage collector *before*, it can't hurt
		((org.inria.jdbc.Connection) db).resetDBMSCallTime(); 
		start = System.currentTimeMillis();

		ps = db.prepareStatement(EP_TEST.EP_QUERY_AC_SELECT_EVENTnop_INFO_COMMENT_BY_EVENT);
		rs = Tools_dmsp.Test_SELECT_BY_INT(3910, ps); 	
		lireResultSet(rs, out); 

		end = System.currentTimeMillis();
		duration = ((org.inria.jdbc.Connection) db).getDBMSCallTime("Q5-d");
		if (perf == 1)
		{
			System.out.println("Q5-d;" + (end - start) + ";" + duration);
		}

		//=======================================================//
		// Q6  - query for a big form (GIR)
		//		Q6-a : select first form with projection of event-info-comment
		//		Q6-b : select first form with projection of info-comment
		//		Q6-c : select last form with projection of event-info-comment
		//		Q6-d : select last form with projection of info-comment
		//=======================================================//	

		// Q6-a
		logMsg("========== Q6-a ==========");

		System.gc(); // runs the garbage collector *before*, it can't hurt
		((org.inria.jdbc.Connection) db).resetDBMSCallTime(); 
		start = System.currentTimeMillis();

		ps = db.prepareStatement(EP_UI.EP_QUERY_AC_SELECT_EVENT_INFO_COMMENT_BY_EVENT);
		rs = Tools_dmsp.Test_SELECT_BY_INT(1412, ps); 	
		lireResultSet(rs, out); 

		end = System.currentTimeMillis();
		duration = ((org.inria.jdbc.Connection) db).getDBMSCallTime("Q6-a");
		if (perf == 1)
		{
			System.out.println("Q6-a;" + (end - start) + ";" + duration);
		}

		// Q6-b
		logMsg("========== Q6-b ==========");

		System.gc(); // runs the garbage collector *before*, it can't hurt
		((org.inria.jdbc.Connection) db).resetDBMSCallTime(); 
		start = System.currentTimeMillis();

		ps = db.prepareStatement(EP_TEST.EP_QUERY_AC_SELECT_EVENTnop_INFO_COMMENT_BY_EVENT);
		rs = Tools_dmsp.Test_SELECT_BY_INT(1412, ps); 	
		lireResultSet(rs, out); 

		end = System.currentTimeMillis();
		duration = ((org.inria.jdbc.Connection) db).getDBMSCallTime("Q6-b");
		if (perf == 1)
		{
			System.out.println("Q6-b;" + (end - start) + ";" + duration);
		}

		// Q6-c
		logMsg("========== Q6-c ==========");

		System.gc(); // runs the garbage collector *before*, it can't hurt
		((org.inria.jdbc.Connection) db).resetDBMSCallTime(); 
		start = System.currentTimeMillis();

		ps = db.prepareStatement(EP_TEST.EP_QUERY_AC_SELECT_EVENT_INFO_COMMENT_BY_EVENT);
		rs = Tools_dmsp.Test_SELECT_BY_INT(3918, ps); 	
		lireResultSet(rs, out); 

		end = System.currentTimeMillis();
		duration = ((org.inria.jdbc.Connection) db).getDBMSCallTime("Q6-c");
		if (perf == 1)
		{
			System.out.println("Q6-c;" + (end - start) + ";" + duration);
		}

		// Q6-d
		logMsg("========== Q6-d ==========");

		System.gc(); // runs the garbage collector *before*, it can't hurt
		((org.inria.jdbc.Connection) db).resetDBMSCallTime(); 
		start = System.currentTimeMillis();

		ps = db.prepareStatement(EP_TEST.EP_QUERY_AC_SELECT_EVENTnop_INFO_COMMENT_BY_EVENT);
		rs = Tools_dmsp.Test_SELECT_BY_INT(3918, ps); 	
		lireResultSet(rs, out); 

		end = System.currentTimeMillis();
		duration = ((org.inria.jdbc.Connection) db).getDBMSCallTime("Q6-d");
		if (perf == 1)
		{
			System.out.println("Q6-d;" + (end - start) + ";" + duration);
		}

		//--
		// Q7: Insert a small form (Fiche de liaison)
		//--
		logMsg("========== Q7 ==========");

		System.gc(); // runs the garbage collector *before*, it can't hurt
		((org.inria.jdbc.Connection) db).resetDBMSCallTime(); 
		start = System.currentTimeMillis();

		insert(db, 1 /* fiche de liaison */, 0 /* GIR medical */);

		end = System.currentTimeMillis();
		duration = ((org.inria.jdbc.Connection) db).getDBMSCallTime("Q7");
		if (perf == 1)
		{
			System.out.println("Q7;" + (end - start) + ";" + duration);
		}

		//--
		// Q8: Insert a big form (GIR)
		//--
		logMsg("========== Q8 ==========");

		System.gc(); // runs the garbage collector *before*, it can't hurt
		((org.inria.jdbc.Connection) db).resetDBMSCallTime(); 
		start = System.currentTimeMillis();

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
	private void insert(java.sql.Connection db, int nb_sform, int nb_bform) throws Exception
	{
		java.sql.PreparedStatement ps;
		int t2, t3, tmp_co, tmp_ev, // temporary vars
			no_co = 3;	// idglobal of no comment is 3

		// insert small forms (fiche de liaison)
		for (int i = 0; i < nb_sform; i++)
		{
			// insert 1 tuple into EVENT 
			logMsg("// Insertion in table EVENT");
			ps = db.prepareStatement(EP_UI.OEP_EVENT_AC_INSERT, 
					java.sql.Statement.RETURN_GENERATED_KEYS);
			int nb = Tools_dmsp.Test_EVENT_INSERT(0, 0, 1012 /*formid*/, 1021 /*userid*/, 
					2 /*no_ep*/, java.sql.Date.valueOf("2000-10-20"), 
					java.sql.Date.valueOf("2000-10-20"), 12 /*filtre*/, ps);
			logMsg(nb+" tuple inserted...");
			tmp_ev = getIdGlobalGetGeneratedKey(ps.getGeneratedKeys());	// ask for generated key

			// insert 1 tuple into COMMENT		
			logMsg("// Insertion in table COMMENT");
			ps = db.prepareStatement(EP_UI.OEP_COMMENT_NOAC_INSERT, 
					java.sql.Statement.RETURN_GENERATED_KEYS);
			nb = Tools_dmsp.Test_COMMENT_INSERT(0, 0,
					"comment can be big, but more than 450 bytes can't be written", ps);
			logMsg(nb+" tuple inserted...");
			tmp_co = getIdGlobalGetGeneratedKey(ps.getGeneratedKeys());	// ask for generated key

			// insert 6 tuples into INFO, the first is linked to the comment
			t3 = 0; // current value of field info.position 

			for (int j = 0; j < 6; j++)
			{
				// at each (nb_in/nb_co) infos:
				if (j == 0)
					t2 = tmp_co; // link info to the last comment
				else 
					t2 = no_co; // link info to the comment no_comment

				logMsg("// Insertion in table INFO");
				ps = db.prepareStatement(EP_UI.OEP_INFO_AC_INSERT, java.sql.Statement.RETURN_GENERATED_KEYS);
				nb = Tools_dmsp.Test_INFO_INSERT(
					0, 0, tmp_ev /*event*/, t2 /*comment*/, "char value", i++,
					java.sql.Date.valueOf("2000-10-20"), 
					t3++ /*position*/, 1 /*filtre*/, j, ps);
				logMsg(nb+" tuple inserted...");
			}
			Save_DBMS_on_disk();	// commit
			// check form insertion:
			if (perf==0){
				logMsg("SELECT INSERTED FORM (fiche de liaison):");
				ps = db.prepareStatement(EP_UI.EP_QUERY_AC_SELECT_EVENT_INFO_COMMENT_BY_EVENT);
				lireResultSet(Tools_dmsp.Test_SELECT_BY_INT(tmp_ev, ps), out); 
			}
		}

		// insert big forms (GIR Med)
		for (int i = 0; i < nb_bform; i++)
		{
			// one big form is generated after several small forms
			// insert 1 tuple into EVENT
			logMsg("// Insertion in table EVENT");
			ps = db.prepareStatement(EP_UI.OEP_EVENT_AC_INSERT, java.sql.Statement.RETURN_GENERATED_KEYS);
			int nb = Tools_dmsp.Test_EVENT_INSERT(
					0, 0, 1006 /*GIR medical*/, 1021, 2, 
					java.sql.Date.valueOf("2000-10-20"), 
					java.sql.Date.valueOf("2000-10-20"), 12, ps);
			logMsg(nb+" tuple inserted...");
			tmp_ev = getIdGlobalGetGeneratedKey(ps.getGeneratedKeys());

			// insert 73 tuples into INFO
			t3 = 0; // current value of field info.position 
			for (int j = 0; j < 73; j++)
			{
				// 24 tuples are linked to newly inserted comments:
				if (j >= 24) {
					t2 = no_co; // link info to the comment no_comment
				}
				else
				{	// insert 24 tuples into COMMENT
					logMsg("// Insertion in table COMMENT");
					ps = db.prepareStatement(EP_UI.OEP_COMMENT_NOAC_INSERT, java.sql.Statement.RETURN_GENERATED_KEYS);
					nb = Tools_dmsp.Test_COMMENT_INSERT(0, 0,
							"comment can be big, but more than 450 bytes can't be written", ps);
					logMsg(nb+" tuple inserted...");
					t2 = getIdGlobalGetGeneratedKey(ps.getGeneratedKeys()); // link info to one inserted comment
				}

				logMsg("// Insertion in table INFO");
				ps = db.prepareStatement(EP_UI.OEP_INFO_AC_INSERT, java.sql.Statement.RETURN_GENERATED_KEYS);
				nb = Tools_dmsp.Test_INFO_INSERT(
					0, 0, tmp_ev /*event*/, t2 /*comment*/, "char value", i,
					java.sql.Date.valueOf("2000-10-20"), 
					t3++ /*position*/, 1 /*filtre*/, j, ps);
				logMsg(nb+" tuple inserted...");
			}
			Save_DBMS_on_disk();	// commit
			// check form insertion:
			if (perf==0)
			{
				logMsg("SELECT INSERTED FORM (GIR Medical):");
				ps = db.prepareStatement(EP_UI.EP_QUERY_AC_SELECT_EVENT_INFO_COMMENT_BY_EVENT);
				lireResultSet(Tools_dmsp.Test_SELECT_BY_INT(tmp_ev, ps), out); 
			}
		}
	}
}
