/**
 * Test to check data corruption during massive updating activity
 */
package org.inria.dmsp.test.TEST_5;

import java.io.InputStreamReader;
import java.io.PrintWriter;		// to produce the output of the test
import java.sql.Date;

import org.inria.database.QEPng;
import org.inria.dmsp.tools.DMSP_QEP_IDs;
import org.inria.dmsp.tools.DeltaLoader;
import org.inria.dmsp.tools.Tools_dmsp;
import org.inria.jdbc.Connection;

import test.jdbc.Tools;
import test.runner.ITest;

/**
 * @author atr
 */
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
	public void run( PrintWriter out, String dbmsHost ) throws Exception
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
				org.inria.dmsp.EP_UI.class, org.inria.dmsp.EP_PDS.class };
		QEPng.loadExecutionPlans( org.inria.dmsp.tools.DMSP_QEP_IDs.class, executionPlans );
		QEPng.installExecutionPlans( db );

		// load the data into database
		DeltaLoader dl = new DeltaLoader( out, perf );
		InputStreamReader fr = new InputStreamReader( Test.class.getResourceAsStream("delta.csv") );
		dl.LoadDelta( fr, db );

		for( int i=0; i<5; ++i)		iterate_once();

//		closeAll();
//		initAll( dbmsHost );
//		dl.LoadDelta( fr, db );

//		for( int i=0; i<3; ++i)		iterate_once1();

		closeAll();
	}
	///
	/// Performs one iteration, measuring the time of select and update
	/// operations and fills update journal with 50 updates.
	/// Standard update implementation is used. 
	///
	private void iterate_once() throws Exception
	{
		int idX = 3623,
			idY = 3622,
			ts_spt,
			res;
		long start;
		java.sql.ResultSet rs = null;
		//java.sql.PreparedStatement
			// SELECT * FROM Info WHERE IdGlobal = ?
		//	psSelect = ((org.inria.jdbc.Connection)db).prepareStatement(DMSP_QEP_IDs. EP_Synchro.EP_INFO_SELECT_BY_ID ),
			// UPDATE Info SET TSSPT=?, ValChar=?, ValNum=?, ValDate=?, Filtre=? WHERE IdGlobal=?
		//	psUpdate = ((org.inria.jdbc.Connection)db).prepareStatement(DMSP_QEP_IDs. EP_UI.INFO_AC_UPDATE_BY_ID );

		java.sql.PreparedStatement ps = ((org.inria.jdbc.Connection)db).prepareStatement(DMSP_QEP_IDs. EP_Synchro.EP_INFO_SELECT_BY_ID );
		//--
		// Q1: Select record X from the end of Info table
		//--
		logMsg("========== Q1 ==========");
		System.gc(); // runs the garbage collector *before*, it can't hurt
		((Connection) db).resetDBMSCallTime();
		start = System.currentTimeMillis();
		rs = Tools_dmsp.Test_SELECT_BY_INT( idX, ps );
		lireResultSet( rs, out );
		long delay = System.currentTimeMillis() - start;
		long duration = ((Connection) db).getDBMSCallTime("Q1");
		perfMsg("Q1;" + delay + ";" + duration);

		//--
		// Q2: Select another record Y from the end of Info table
		//--
		logMsg("========== Q2 ==========");
		System.gc(); // runs the garbage collector *before*, it can't hurt
		((Connection) db).resetDBMSCallTime();
		start = System.currentTimeMillis();
		rs = Tools_dmsp.Test_SELECT_BY_INT( idY, ps );
		lireResultSet( rs, out );
		delay = System.currentTimeMillis() - start;
		duration = ((Connection) db).getDBMSCallTime("Q2");
		perfMsg("Q2;" + delay + ";" + duration);

		ps = ((org.inria.jdbc.Connection)db).prepareStatement(DMSP_QEP_IDs. EP_UI.INFO_AC_UPDATE_BY_ID );
		//--
		// Q3: Update record X from the end of Info table
		//--
		logMsg("========== Q3 ==========");
		System.gc(); // runs the garbage collector *before*, it can't hurt
		((Connection) db).resetDBMSCallTime();
		ts_spt = ((Connection) db).getGlobalTimestamp();
		start = System.currentTimeMillis();
		res = Tools_dmsp.Test_INFO_UPDATE( ts_spt,
			"NEW INFOX",				// ValChar
			1010101,					// ValNum
			Date.valueOf("2010-11-01"),	// ValDate
			888,						// Filtre
			idX, ps );
		delay = System.currentTimeMillis() - start;
		duration = ((Connection) db).getDBMSCallTime("Q3");
		logMsg("number of tuples updated: " + res);
		perfMsg("Q3;" + delay + ";" + duration);

		//--
		// Q4: Update record Y from the end of Info table
		//--
		logMsg("========== Q4 ==========");
		System.gc(); // runs the garbage collector *before*, it can't hurt
		((Connection) db).resetDBMSCallTime();
		ts_spt = ((Connection) db).getGlobalTimestamp();
		start = System.currentTimeMillis();
		res = Tools_dmsp.Test_INFO_UPDATE( ts_spt,
			"NEW INFOY",				// ValChar
			1010101,					// ValNum
			Date.valueOf("2010-11-01"),	// ValDate
			888,						// Filtre
			idY, ps );
		delay = System.currentTimeMillis() - start;
		duration = ((Connection) db).getDBMSCallTime("Q4");
		logMsg("number of tuples updated: " + res);
		perfMsg("Q4;" + delay + ";" + duration);

		//--
		// Q5: 50 updates of the record X from the end of Info table
		//--
		logMsg("========== Q5 ==========");
		ts_spt = ((Connection) db).getGlobalTimestamp();
		res = 0;
		for (int i=0; i<50; ++i)
		{
			res += Tools_dmsp.Test_INFO_UPDATE( ts_spt,
				"INFO X 50",				// ValChar
				1010101,					// ValNum
				Date.valueOf("2010-11-05"),	// ValDate
				888,						// Filtre
				idX, ps );
		}
		logMsg("number of tuples updated: " + res);
	}
	//===========================================
	///
	/// The same as iterate_once(), but using delete+insert instead of update
	///
/*	private void iterate_once1() throws Exception
	{
		int idX = 3623,
			idY = 3622,
			ts_spt,
			res;
		long start;
		java.sql.ResultSet rs = null;

		//java.sql.PreparedStatement
		//	// UPDATE Info SET TSSPT=?, ValChar=?, ValNum=?, ValDate=?, Filtre=? WHERE IdGlobal=?
		//	psUpdate = ((org.inria.jdbc.Connection)db).prepareStatement(DMSP_QEP_IDs. EP_UI.INFO_AC_UPDATE_BY_ID ),
		java.sql.PreparedStatement	psSelect = null,	// SELECT * FROM Info WHERE IdGlobal = ?
									psInsert = null,	// Insert into info values (?,?,?,?,?, ?,?,?,?,?, ?,?)
									psDelete = null;	// DELETE FROM Info WHERE IdGlobal = ?

		psSelect = ((org.inria.jdbc.Connection)db).prepareStatement(DMSP_QEP_IDs. EP_Synchro.EP_INFO_SELECT_BY_ID );
		psInsert = null;
		psDelete = null;
		//--
		// Q6: Select record X from the end of Info table
		//--
		logMsg("========== Q6 ==========");
		System.gc(); // runs the garbage collector *before*, it can't hurt
		((Connection) db).resetDBMSCallTime();
		start = System.currentTimeMillis();
		rs = Tools_dmsp.Test_SELECT_BY_INT( idX, psSelect );
		lireResultSet( rs, out );
		long delay = System.currentTimeMillis() - start;
		long duration = ((Connection) db).getDBMSCallTime("Q6");
		perfMsg("Q6;" + delay + ";" + duration);

		//--
		// Q7: Select another record Y from the end of Info table
		//--
		logMsg("========== Q7 ==========");
		System.gc(); // runs the garbage collector *before*, it can't hurt
		((Connection) db).resetDBMSCallTime();
		start = System.currentTimeMillis();
		rs = Tools_dmsp.Test_SELECT_BY_INT( idY, psSelect );
		lireResultSet( rs, out );
		delay = System.currentTimeMillis() - start;
		duration = ((Connection) db).getDBMSCallTime("Q7");
		perfMsg("Q7;" + delay + ";" + duration);

		//--
		// Q8: Update (select->delete->insert) record X from the end of Info table
		//--
		logMsg("========== Q8 ==========");
		System.gc(); // runs the garbage collector *before*, it can't hurt
		((Connection) db).resetDBMSCallTime();
		start = System.currentTimeMillis();
		// select and save current data first...
		rs = Tools_dmsp.Test_SELECT_BY_INT( idX, psSelect );
		rs.next();
//		int		vIdGlobal = rs.getInt("IdGlobal");
		int		vAuthor = rs.getInt("Author");
//		int		vTSSPT = rs.getInt("TSSPT");
		int		vTSSanteos = rs.getInt("TSSanteos");
		int		vIdEvent = rs.getInt("IdEvent");
		int		vIdComment = rs.getInt("IdComment");
//		String	vValChar = rs.getString("ValChar");
//		int		vValNum = rs.getInt("ValNum");
//		Date	vValDate = rs.getDate("ValDate");
		int		vPosition = rs.getInt("Position");
//		int		vFiltre = rs.getInt("Filtre");
		int		vIdConcept = rs.getInt("IdConcept");
		rs.close();
		// delete record...
		psSelect = null;
		psInsert = null;
		psDelete = ((org.inria.jdbc.Connection)db).prepareStatement(DMSP_QEP_IDs. EP_UI.EP_INFO_AC_DELETE_BY_ID );
		res = Tools_dmsp.Test_DELETE_BY_ID( idX, psDelete );
		// insert updated record...
		psSelect = null;
		psInsert = ((org.inria.jdbc.Connection)db).prepareStatement(DMSP_QEP_IDs. EP_Synchro.EP_INFO_INSERT );
		psDelete = null;
		ts_spt = ((Connection) db).getGlobalTimestamp();
		Tools_dmsp.Test_INFO_INSERT( idX,	// IdGlobal
				vAuthor,					// Author
				ts_spt,						// TSSPT
				vTSSanteos,					// TSSanteos
				vIdEvent,					// IdEvent
				vIdComment,					// IdComment
				"NEW INFOX",				// ValChar
				1010101,					// ValNum
				Date.valueOf("2010-11-01"),	// ValDate
				vPosition,					// Position
				888,						// Filtre
				vIdConcept,					// IdConcept
				psInsert );
		delay = System.currentTimeMillis() - start;
		duration = ((Connection) db).getDBMSCallTime("Q8");
		logMsg("number of tuples updated: " + res);
		perfMsg("Q8;" + delay + ";" + duration);

		psSelect = ((org.inria.jdbc.Connection)db).prepareStatement(DMSP_QEP_IDs. EP_Synchro.EP_INFO_SELECT_BY_ID );
		psInsert = null;
		psDelete = null;
		//--
		// Q9: Update (select->delete->insert) record Y from the end of Info table
		//--
		logMsg("========== Q9 ==========");
		System.gc(); // runs the garbage collector *before*, it can't hurt
		((Connection) db).resetDBMSCallTime();
		start = System.currentTimeMillis();
		// select and save current data first...
		rs = Tools_dmsp.Test_SELECT_BY_INT( idY, psSelect );
		rs.next();
		vAuthor = rs.getInt("Author");
		vTSSanteos = rs.getInt("TSSanteos");
		vIdEvent = rs.getInt("IdEvent");
		vIdComment = rs.getInt("IdComment");
		vPosition = rs.getInt("Position");
		vIdConcept = rs.getInt("IdConcept");
		rs.close();
		// delete record...
		psSelect = null;
		psInsert = null;
		psDelete = ((org.inria.jdbc.Connection)db).prepareStatement(DMSP_QEP_IDs. EP_UI.EP_INFO_AC_DELETE_BY_ID );
		res = Tools_dmsp.Test_DELETE_BY_ID( idY, psDelete );
		// insert updated record...
		psSelect = null;
		psInsert = ((org.inria.jdbc.Connection)db).prepareStatement(DMSP_QEP_IDs. EP_Synchro.EP_INFO_INSERT );
		psDelete = null;
		ts_spt = ((Connection) db).getGlobalTimestamp();
		Tools_dmsp.Test_INFO_INSERT( idY,	// IdGlobal
				vAuthor,					// Author
				ts_spt,						// TSSPT
				vTSSanteos,					// TSSanteos
				vIdEvent,					// IdEvent
				vIdComment,					// IdComment
				"NEW INFOY",				// ValChar
				1010101,					// ValNum
				Date.valueOf("2010-11-01"),	// ValDate
				vPosition,					// Position
				888,						// Filtre
				vIdConcept,					// IdConcept
				psInsert );
		delay = System.currentTimeMillis() - start;
		duration = ((Connection) db).getDBMSCallTime("Q9");
		logMsg("number of tuples updated: " + res);
		perfMsg("Q9;" + delay + ";" + duration);

		//--
		// Q10: 50 updates of the record X from the end of Info table
		//--
		logMsg("========== Q10 ==========");
		res = 0;
		for (int i=0; i<50; ++i)
		{
			psSelect = ((org.inria.jdbc.Connection)db).prepareStatement(DMSP_QEP_IDs. EP_Synchro.EP_INFO_SELECT_BY_ID );
			psInsert = null;
			psDelete = null;
			// select and save current data first...
			rs = Tools_dmsp.Test_SELECT_BY_INT( idX, psSelect );
			rs.next();
			vAuthor = rs.getInt("Author");
			vTSSanteos = rs.getInt("TSSanteos");
			vIdEvent = rs.getInt("IdEvent");
			vIdComment = rs.getInt("IdComment");
			vPosition = rs.getInt("Position");
			vIdConcept = rs.getInt("IdConcept");
			rs.close();
			// delete record...
			psSelect = null;
			psInsert = null;
			psDelete = ((org.inria.jdbc.Connection)db).prepareStatement(DMSP_QEP_IDs. EP_UI.EP_INFO_AC_DELETE_BY_ID );
			res += Tools_dmsp.Test_DELETE_BY_ID( idX, psDelete );
			// insert updated record...
			psSelect = null;
			psInsert = ((org.inria.jdbc.Connection)db).prepareStatement(DMSP_QEP_IDs. EP_Synchro.EP_INFO_INSERT );
			psDelete = null;
			ts_spt = ((Connection) db).getGlobalTimestamp();
			Tools_dmsp.Test_INFO_INSERT( idX,	// IdGlobal
					vAuthor,					// Author
					ts_spt,						// TSSPT
					vTSSanteos,					// TSSanteos
					vIdEvent,					// IdEvent
					vIdComment,					// IdComment
					"INFO X 50",				// ValChar
					1010101,					// ValNum
					Date.valueOf("2010-11-05"),	// ValDate
					vPosition,					// Position
					888,						// Filtre
					vIdConcept,					// IdConcept
					psInsert );
		}
		logMsg("number of tuples updated: " + res);
	}*/
}
