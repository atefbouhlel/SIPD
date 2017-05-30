package org.inria.dmsp.test.TEST_opnostacknorunstack;

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
		// the statement for non-parametric select:
		java.sql.Statement st;

		// pre-check (1 element)
		logMsg( "EP_EVENT_SELECT_STAR (1 tuple)" );
		st = db.createStatement();
		rs = ((org.inria.jdbc.Statement)st).executeQuery(DMSP_QEP_IDs. EP_TEST.EP_EVENT_SELECT_STAR );
		lireResultSet( rs, out );
		
		// Delete 1 event
		logMsg( "EP_EVENT_AC_DELETE_BY_ID" );
		ps = ((org.inria.jdbc.Connection)db).prepareStatement(DMSP_QEP_IDs.EP_UI.EP_EVENT_AC_DELETE_BY_ID);
		Tools_dmsp.Test_DELETE_BY_ID(1017, ps);
		
		// post-check (0 element)
		logMsg( "EP_EVENT_SELECT_STAR (0 tuple)" );
		st = db.createStatement();
		rs = ((org.inria.jdbc.Statement)st).executeQuery(DMSP_QEP_IDs. EP_TEST.EP_EVENT_SELECT_STAR );
		lireResultSet( rs, out );
		
		// Delete 100-1=99 infos
		Delete_Info( Build_List() );
		
		ps = ((org.inria.jdbc.Connection)db).prepareStatement(DMSP_QEP_IDs. EP_Debug.EP_SYSTEM_SKTEVENT_SELECT_FORMPOS_BY_EVENTPOS );
		logMsg( "========== Q2 ==========" );
		System.gc(); // runs the garbage collector *before*, it can't hurt
		((Connection) db).resetDBMSCallTime();
		start = System.currentTimeMillis();
		rs = Tools_dmsp.Test_SELECT_BY_INT( 1, ps );
		lireResultSet( rs, out );
		delay = System.currentTimeMillis() - start;
		duration = ((Connection) db).getDBMSCallTime( "Q1" );
		perfMsg( "Q1;" + delay + ";" + duration );
		
		closeAll();
	}


	private int[] Build_List ()
	{
		int i = 0, j;
		int[] list = new int[99];
		// populates the list with the keys to delete
		for (j = 1018; j <= 1057; ++j)
		{
			list[i++] = j;
		}
		for (j = 1146; j <= 1148; ++j)
		{
			list[i++] = j;
		}
		for (j = 1152; j <= 1169; ++j)
		{
			list[i++] = j;
		}
		for (j = 1185; j <= 1203; ++j)
		{
			list[i++] = j;
		}
		for (j = 1212; j <= 1230; ++j)
		{
			list[i++] = j;
		}
		return list;
	}
	
	
	private void Delete_Info( int[] list ) throws Exception
	{
		// loops and temp variables
		int res = 0;
		// the prepared statement used for parametric queries:
		java.sql.PreparedStatement ps ; 
		// the statement for non-parametric select:
		java.sql.Statement st = db.createStatement();
		// the resultset used for all queries:
		java.sql.ResultSet rs;

		// pre-check
		logMsg( "EP_INFO_SELECT_STAR (before)" );
		//st = db.createStatement();
		rs = ((org.inria.jdbc.Statement)st).executeQuery(DMSP_QEP_IDs. EP_TEST.EP_INFO_SELECT_STAR );
		lireResultSet( rs, out );
		
		logMsg("Start to delete infos...");

		/*  deletes an info  */
		ps = ((org.inria.jdbc.Connection)db).prepareStatement(DMSP_QEP_IDs.EP_UI.EP_INFO_AC_DELETE_BY_ID);
		for ( int i : list )
		{
			res += Tools_dmsp.Test_DELETE_BY_ID(i, ps);
		}
		logMsg( res + " infos deleted.");

		// post-check
		logMsg( "EP_INFO_SELECT_STAR (after)" );
		//st = db.createStatement();
		rs = ((org.inria.jdbc.Statement)st).executeQuery(DMSP_QEP_IDs. EP_TEST.EP_INFO_SELECT_STAR );
		lireResultSet( rs, out );
		
		logMsg( "EP_SYSTEM_LogDeleted" );
		//st = db.createStatement();
		rs = ((org.inria.jdbc.Statement)st).executeQuery(DMSP_QEP_IDs. EP_Debug.EP_SYSTEM_LogDeleted );
		lireResultSet( rs, out );
	}
}
