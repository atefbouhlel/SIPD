package org.inria.dmsp.test.TEST_Tearing;

import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Connection;

import org.inria.database.QEPng;
import org.inria.dmsp.tools.DeltaLoader;
import org.inria.dmsp.tools.Tools_dmsp;

import test.jdbc.Tools;
import test.runner.ITestTearing;

public abstract class TearingTestFramework extends Tools implements ITestTearing
{
	public abstract void prepareBeforeTearingPoint(Connection db, PrintWriter out, int perf) throws Exception;
	public abstract void placeAndReachTearingPoint(Connection db, PrintWriter out, int perf) throws Exception;

	@Override
	public void run(PrintWriter out, String dbmsHost, short partOfTheCode) throws Exception
	{
		// Initialize the common vars and connect to DBMS
		initCommon(out, dbmsHost);
		if (partOfTheCode == 1)
		{
			// Initialize the vars used in part 1 and load schema + data
			initPart1();

			// Prepare the data before the tearing.
			prepareBeforeTearingPoint(db, out, perf);

			// Commit, select star
			intermPart1();

			// Place the tearing point and reach it
			placeAndReachTearingPoint(db, out, perf);

			// Hence execution should never reach the current line
			if (perf == 0)
				out.println("The DBMS did not fall asleep, you should review your test...");

		}
		else if (partOfTheCode == 2)
		{
			launchPart2();
		}
		else
		{
			out.println("Wrong partOfTheCode value: " + partOfTheCode);
		}
		quit();
	}

	private void initCommon(PrintWriter out, String dbmsHost) throws Exception
	{
		// required on PC (durable variable on token):
		org.inria.jdbc.DBMS.initialized=true;

		// Disable DEBUG
		//System.setProperty("jdbc.debug", "false");

		// set output stream and instantiate the tools:
		this.out = out;

		perf = 0;
		init();

		openConnection(dbmsHost, null);
	}

	private void initPart1() throws Exception
	{
		Tools_dmsp t = new Tools_dmsp(this.out);

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
		InputStreamReader fr = new InputStreamReader(TearingTestFramework.class.getResourceAsStream("delta.csv"));
		// load the data into database
		dl.LoadDelta(fr, db);
	}

	private void intermPart1() throws Exception
	{
		//Commit
		Save_DBMS_on_disk();

		this.out.println(" == START SELECT STAR == ");
		TearingTools.select_star(db, this.out, perf);
		this.out.println(" == END SELECT STAR == ");
		this.out.flush();
	}

	private void launchPart2() throws Exception
	{
		this.out.println(" == START SELECT STAR == ");
		TearingTools.select_star(db, this.out, perf);
		this.out.println(" == END SELECT STAR == ");
	}

	private void quit() throws Exception
	{
		out.flush();
		Desinstall_DBMS_MetaData();
		Shutdown_DBMS();
	}
}
