package org.inria.dmsp.test.TEST_opstackunstack;

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
import org.inria.dmsp.tools.Tools_dmsp;
import org.inria.jdbc.Connection;

import test.jdbc.Tools;
import test.runner.ITest;

public class Test extends Tools implements ITest
{
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
	/// Prints given message on console if Tools.perf == 0
	protected void perfMsg(String msg)
	{
		if (perf == 1)
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
	
	@Override
	public void run(PrintWriter out, String dbmsHost) throws Exception
	{
		this.out = out;
		perf = 1;

		initAll( dbmsHost );

		// load the DB schema
		Tools_dmsp t = new Tools_dmsp( out );
		byte[] schemaDesc = t.loadSchema( "/org/inria/dmsp/schemaV4.rsc" );
		int usedId = 404;
		Install_DBMS_MetaData( schemaDesc, usedId );
		schemaDesc = null;
		
		// load and install QEPs
		Class<?>[] executionPlans = new Class[] { org.inria.dmsp.EP_Synchro.class, org.inria.dmsp.schema.EP_TEST.class, 
				org.inria.dmsp.EP_UI.class, org.inria.dmsp.EP_PDS.class, org.inria.dmsp.EP_Debug.class };
		QEPng.loadExecutionPlans( org.inria.dmsp.tools.DMSP_QEP_IDs.class, executionPlans );
		QEPng.installExecutionPlans( db );

		// load the data into database
		DeltaLoader dl = new DeltaLoader( out, perf );
		InputStreamReader fr = new InputStreamReader( Test.class.getResourceAsStream("delta.csv") );
		dl.LoadDelta( fr, db );

		long start, delay, duration;
		// the prepared statement used for all inserts:
		java.sql.PreparedStatement ps;
		// the resultset used for all queries:
		java.sql.ResultSet rs;

		// Insert 100-16=84 events
		Generate_data( 1062/*first_us*/, 13/*nb_us*/, 1001/*first_fo*/, 16/*nb_fo*/, 2/*first_ep*/,
					   84/*nb_add_ev: 16 in the original delta, so 100-16*/);
		
		ps = ((org.inria.jdbc.Connection)db).prepareStatement(DMSP_QEP_IDs. EP_Debug.EP_SYSTEM_SKTEVENT_SELECT_FORMPOS_BY_EVENTPOS );
		logMsg( "========== Q1 ==========" );
		System.gc(); // runs the garbage collector *before*, it can't hurt
		((Connection) db).resetDBMSCallTime();
		start = System.currentTimeMillis();
		rs = Tools_dmsp.Test_SELECT_BY_INT( 9999, ps );
		lireResultSet( rs, out );
		delay = System.currentTimeMillis() - start;
		duration = ((Connection) db).getDBMSCallTime( "Q1" );
		perfMsg( "Q1;" + delay + ";" + duration );
		
		closeAll();
	}


	// generate event/info/comments with UI plans:
	private void Generate_data(
			// parameters for already existing tuples
			// assumption : idglobal values are continuous in each table
			int first_us, int nb_us, int first_fo, int nb_fo, int first_ep,
			// parameters for tuples to be created: must be > 0
			int nb_add_ev ) throws Exception
	{
		// loops and temp variables
		int i, res = 0;
		// the prepared statement used for parametric queries:
		java.sql.PreparedStatement ps ; 
		// the statement for non-parametric select:
		java.sql.Statement st = db.createStatement();
		// the resultset used for all queries:
		java.sql.ResultSet rs;
		// id of the spt (patient) and current ts_spt
		int id, ts_spt;

		logMsg("Start to generate data...");
		// set id of the spt (patient)
		id = ((org.inria.jdbc.Connection) db).getSptIdPatient();

		/*  INSERT INTO EVENT  */
		logMsg( "Insert into EVENT " );
		ps = ((org.inria.jdbc.Connection)db).prepareStatement(DMSP_QEP_IDs.EP_UI.OEP_EVENT_AC_INSERT, 
				java.sql.Statement.RETURN_GENERATED_KEYS);
		String date, date2;
		for(i=0; i<nb_add_ev; ++i)
		{
			ts_spt = ((org.inria.jdbc.Connection) db).getGlobalTimestamp();
			date = (2000+i%10)+"-"+((i%12+1)<10?"0":"")+(i%12+1)+"-"+((i%30+1)<10?"0":"")+(i%30+1);
			date2 = (2010+i%10)+"-"+((i%12+1)<10?"0":"")+(i%12+1)+"-"+((i%30+1)<10?"0":"")+(i%30+1);
			res += Tools_dmsp.Test_EVENT_INSERT( id, ts_spt, first_fo+i%nb_fo, first_us+i%nb_us,
												first_ep, java.sql.Date.valueOf(date),
												java.sql.Date.valueOf(date2), 12, ps );
		}
		logMsg( res + " events inserted.");

		logMsg( "EP_EVENT_SELECT_STAR" );
		rs = ((org.inria.jdbc.Statement)st).executeQuery(DMSP_QEP_IDs. EP_TEST.EP_EVENT_SELECT_STAR );
		lireResultSet( rs, out );
	}
}
