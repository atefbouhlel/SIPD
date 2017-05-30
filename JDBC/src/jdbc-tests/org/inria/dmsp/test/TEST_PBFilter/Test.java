package org.inria.dmsp.test.TEST_PBFilter;

/////////////////////////////////////////////////////
//IMPORTS THAT CAN BE CHANGED TO TEST OTHER PLANS //
/////////////////////////////////////////////////////


///////////////////////////////
// DO NOT CHANGE AFTER HERE  //
///////////////////////////////

import java.io.PrintWriter;		// to produce the output of the test

import org.inria.database.QEPng;
import org.inria.dmsp.tools.Tools_dmsp;

import test.jdbc.Tools;
import test.runner.ITest;

public class Test extends Tools implements ITest
{
	@Override
	public void run(PrintWriter out, String dbmsHost) throws Exception
	{
		String key;
		this.out = out;
		perf = 1;
		// TODO: values were reduced for testing only...
		int max_num_tuples = 10000; //300000;
		int tuples_each_run = 1000; //30000;
		int runs = max_num_tuples/tuples_each_run;
		
		Tools_dmsp t = new Tools_dmsp(out);
		long start, end, duration;
		
		init();
		// connect without authentication
		// openConnection(dbmsHost, null);
		openConnection(dbmsHost, null);
		
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

		// initialize the PBFilter structure
	    // 0: without partitioning; 1: with partitioning
		PBFilter_Init( (byte)0, (byte)7 );
		
		//--
		// Q1-1: Insert max_num_tuples keys into PBFilter without partitioning
		// Q1-2: do tuples_each_run/10 random lookups of existing keys for each run
		// Q1-3: do tuples_each_run/10 random lookups of non-existing keys for each run
		//--
		for (int i = 1; i <= runs; i++ )
		{
			logMsg("========== Q1-1-" + i + " ==========");
			System.gc(); // runs the garbage collector *before*, it can't hurt
			((org.inria.jdbc.Connection) db).resetDBMSCallTime(); 
			start = System.currentTimeMillis();
			// insert keys into the PBFilter index
			for ( int j = 1; j <= tuples_each_run; j++ )
			{
				key = "/*EP   " + Integer.toString((i-1)*tuples_each_run+j) + "\u0000";
				PBFilter_Insert_Key(key);
				if(perf == 0)
				{
					out.println(Integer.toString((i-1)*tuples_each_run+j));
				}
			}
			perf = 0;
			print_perfs((byte)20);
			perf = 1;
			end = System.currentTimeMillis();
			duration = ((org.inria.jdbc.Connection) db).getDBMSCallTime("Q1-1-" + i);
			if (perf == 1)
			{
				System.out.println("Q1-1-" + i + ";" + (end - start) + ";" + duration);
			}
			PBFilter_Close((byte)0);
			
			logMsg("========== Q1-2-" + i + " ==========");
			System.gc(); // runs the garbage collector *before*, it can't hurt
			((org.inria.jdbc.Connection) db).resetDBMSCallTime(); 
			start = System.currentTimeMillis();
			// lookup existing keys from the PBFilter index
			for ( int j = 1; j <= i*tuples_each_run; j = j + i*10)
			{
				key = "/*EP   " + Integer.toString(j) + "\u0000";
				PBFilter_Lookup_Key(key);
				if(perf == 0)
				{
					out.println(Integer.toString(j));
				}
			}
			perf = 0;
			print_perfs((byte)21);
			perf = 1;
			end = System.currentTimeMillis();
			duration = ((org.inria.jdbc.Connection) db).getDBMSCallTime("Q1-2-" + i);
			if (perf == 1)
			{
				System.out.println("Q1-2-" + i + ";" + (end - start) + ";" + duration);
			}
			PBFilter_Close((byte)0);
			
			logMsg("========== Q1-3-" + i + " ==========");
			System.gc(); // runs the garbage collector *before*, it can't hurt
			((org.inria.jdbc.Connection) db).resetDBMSCallTime(); 
			start = System.currentTimeMillis();
			// lookup non-existing keys from the PBFilter index
			for ( int j = 1; j <= i*tuples_each_run; j = j + i*10)
			{
				key = "/*EP   " + Integer.toString(max_num_tuples+j) + "\u0000";
				PBFilter_Lookup_Key(key);
				if(perf == 0)
				{
					out.println(Integer.toString(max_num_tuples+j));
				}
			}
			end = System.currentTimeMillis();
			duration = ((org.inria.jdbc.Connection) db).getDBMSCallTime("Q1-3-" + i);
			if (perf == 1)
			{
				System.out.println("Q1-3-" + i + ";" + (end - start) + ";" + duration);
			}
			PBFilter_Close((byte)0);
		}
		// close the PBFilter structure
		PBFilter_Close((byte)1);
		
		// initialize the PBFilter structure
	    // 0: without partitioning; 1: with partitioning
		PBFilter_Init( (byte)1, (byte)7 );
		
		//--
		// Q2-1: Insert max_num_tuples keys into PBFilter without partitioning
		// Q2-2: do tuples_each_run/10 random lookups of existing keys for each run
		// Q2-3: do tuples_each_run/10 random lookups of non-existing keys for each run
		//--
		for (int i = 1; i <= runs; i++ )
		{
			logMsg("========== Q2-1-" + i + " ==========");
			System.gc(); // runs the garbage collector *before*, it can't hurt
			((org.inria.jdbc.Connection) db).resetDBMSCallTime(); 
			start = System.currentTimeMillis();
			// insert keys into the PBFilter index
			for ( int j = 1; j <= tuples_each_run; j++ )
			{
				key = "/*EP   " + Integer.toString((i-1)*tuples_each_run+j) + "\u0000";
				PBFilter_Insert_Key(key);
				if(perf == 0)
				{
					out.println(Integer.toString((i-1)*tuples_each_run+j));
				}
			}
			end = System.currentTimeMillis();
			duration = ((org.inria.jdbc.Connection) db).getDBMSCallTime("Q2-1-" + i);
			if (perf == 1)
			{
				System.out.println("Q2-1-" + i + ";" + (end - start) + ";" + duration);
			}
			PBFilter_Close((byte)0);
			
			logMsg("========== Q2-2-" + i + " ==========");
			System.gc(); // runs the garbage collector *before*, it can't hurt
			((org.inria.jdbc.Connection) db).resetDBMSCallTime(); 
			start = System.currentTimeMillis();
			// lookup existing keys from the PBFilter index
			for ( int j = 1; j <= i*tuples_each_run; j = j + i*10)
			{
				key = "/*EP   " + Integer.toString(j) + "\u0000";
				PBFilter_Lookup_Key(key);
				if(perf == 0)
				{
					out.println(Integer.toString(j));
				}
			}
			end = System.currentTimeMillis();
			duration = ((org.inria.jdbc.Connection) db).getDBMSCallTime("Q2-2-" + i);
			if (perf == 1)
			{
				System.out.println("Q2-2-" + i + ";" + (end - start) + ";" + duration);
			}
			PBFilter_Close((byte)0);
			
			logMsg("========== Q2-3-" + i + " ==========");
			System.gc(); // runs the garbage collector *before*, it can't hurt
			((org.inria.jdbc.Connection) db).resetDBMSCallTime(); 
			start = System.currentTimeMillis();
			// lookup non-existing keys from the PBFilter index
			for ( int j = 1; j <= i*tuples_each_run; j = j + i*10)
			{
				key = "/*EP   " + Integer.toString(max_num_tuples+j) + "\u0000";
				PBFilter_Lookup_Key(key);
				if(perf == 0)
				{
					out.println(Integer.toString(max_num_tuples+j));
				}
			}
			end = System.currentTimeMillis();
			duration = ((org.inria.jdbc.Connection) db).getDBMSCallTime("Q2-3-" + i);
			if (perf == 1)
			{
				System.out.println("Q2-3-" + i + ";" + (end - start) + ";" + duration);
			}
			PBFilter_Close((byte)0);
		}
		// close the PBFilter structure
		PBFilter_Close((byte)1);
		
		//--
		// Q3: tune number of hash functions from 3 to 13
		//--
		for(int k = 3; k <= 13; k++)
		{
			// initialize the PBFilter structure
		    // 0: without partitioning; 1: with partitioning
			PBFilter_Init( (byte)1, (byte)k );
			
			logMsg("========== Q3-1-" + k + " ==========");
			System.gc(); // runs the garbage collector *before*, it can't hurt
			((org.inria.jdbc.Connection) db).resetDBMSCallTime(); 
			start = System.currentTimeMillis();
			// insert keys into the PBFilter index
			for ( int j = 1; j <= max_num_tuples; j++ )
			{
				key = "/*EP   " + Integer.toString(j) + "\u0000";
				PBFilter_Insert_Key(key);
				if(perf == 0)
				{
					out.println(Integer.toString(j));
				}
			}
			end = System.currentTimeMillis();
			duration = ((org.inria.jdbc.Connection) db).getDBMSCallTime("Q3-1-" + k);
			if (perf == 1)
			{
				System.out.println("Q3-1-" + k + ";" + (end - start) + ";" + duration);
			}
			PBFilter_Close((byte)0);
			
			logMsg("========== Q3-2-" + k + " ==========");
			System.gc(); // runs the garbage collector *before*, it can't hurt
			((org.inria.jdbc.Connection) db).resetDBMSCallTime(); 
			start = System.currentTimeMillis();
			// lookup existing keys from the PBFilter index
			for ( int j = 1; j <= max_num_tuples; j = j + 100)
			{
				key = "/*EP   " + Integer.toString(j) + "\u0000";
				PBFilter_Lookup_Key(key);
				if(perf == 0)
				{
					out.println(Integer.toString(j));
				}
			}
			end = System.currentTimeMillis();
			duration = ((org.inria.jdbc.Connection) db).getDBMSCallTime("Q3-2-" + k);
			if (perf == 1)
			{
				System.out.println("Q3-2-" + k + ";" + (end - start) + ";" + duration);
			}
			PBFilter_Close((byte)0);
			
			logMsg("========== Q3-3-" + k + " ==========");
			System.gc(); // runs the garbage collector *before*, it can't hurt
			((org.inria.jdbc.Connection) db).resetDBMSCallTime(); 
			start = System.currentTimeMillis();
			// lookup non-existing keys from the PBFilter index
			for ( int j = 1; j <= max_num_tuples; j = j + 100)
			{
				key = "/*EP   " + Integer.toString(max_num_tuples+j) + "\u0000";
				PBFilter_Lookup_Key(key);
				if(perf == 0)
				{
					out.println(Integer.toString(max_num_tuples+j));
				}
			}
			end = System.currentTimeMillis();
			duration = ((org.inria.jdbc.Connection) db).getDBMSCallTime("Q3-3-" + k);
			if (perf == 1)
			{
				System.out.println("Q3-3-" + k + ";" + (end - start) + ";" + duration);
			}
			PBFilter_Close((byte)0);
			// close the PBFilter structure
			PBFilter_Close((byte)1);
		}
		
		//--
		// Q4: update keys in the PBFilter index
		//--
		
		// initialize the PBFilter structure
	    // 0: without partitioning; 1: with partitioning
		PBFilter_Init( (byte)1, (byte)7 );
		// insert keys into the PBFilter index
		for ( int j = 1; j <= max_num_tuples; j++ )
		{
			key = "/*EP   " + Integer.toString(j) + "\u0000";
			PBFilter_Insert_Key(key);
			if(perf == 0)
			{
				out.println(Integer.toString(j));
			}
		}
		PBFilter_Close((byte)0);
		// update 1% tuples each time and do lookups: 10 loops
		for (int i = 1; i <= runs; i++ )
		{
			// update 1% keys
			for ( int j = 1; j <= tuples_each_run; j = j + 100 )
			{
				key = "/*EP   " + Integer.toString((i-1)*tuples_each_run+j) + "\u0000";
				PBFilter_Delete_key(key);
				PBFilter_Insert_Key(key);
				if(perf == 0)
				{
					out.println(Integer.toString((i-1)*tuples_each_run+j));
				}
			}
			PBFilter_Close((byte)0);
			
			logMsg("========== Q4-1-" + i + " ==========");
			System.gc(); // runs the garbage collector *before*, it can't hurt
			((org.inria.jdbc.Connection) db).resetDBMSCallTime(); 
			start = System.currentTimeMillis();
			// lookup existing keys from the PBFilter index
			for ( int j = 1; j <= i*tuples_each_run; j = j + i*10)
			{
				key = "/*EP   " + Integer.toString(j) + "\u0000";
				PBFilter_Lookup_Key(key);
				if(perf == 0)
				{
					out.println(Integer.toString(j));
				}
			}
			end = System.currentTimeMillis();
			duration = ((org.inria.jdbc.Connection) db).getDBMSCallTime("Q4-1-" + i);
			if (perf == 1)
			{
				System.out.println("Q4-1-" + i + ";" + (end - start) + ";" + duration);
			}
			PBFilter_Close((byte)0);
			
			logMsg("========== Q4-2-" + i + " ==========");
			System.gc(); // runs the garbage collector *before*, it can't hurt
			((org.inria.jdbc.Connection) db).resetDBMSCallTime(); 
			start = System.currentTimeMillis();
			// lookup non-existing keys from the PBFilter index
			for ( int j = 1; j <= i*tuples_each_run; j = j + i*10)
			{
				key = "/*EP   " + Integer.toString(max_num_tuples+j) + "\u0000";
				PBFilter_Lookup_Key(key);
				if(perf == 0)
				{
					out.println(Integer.toString(max_num_tuples+j));
				}
			}
			end = System.currentTimeMillis();
			duration = ((org.inria.jdbc.Connection) db).getDBMSCallTime("Q4-2-" + i);
			if (perf == 1)
			{
				System.out.println("Q4-2-" + i + ";" + (end - start) + ";" + duration);
			}
			PBFilter_Close((byte)0);
		}
		// update 10% tuples each time and do lookups: 9 loops
		for (int i = 1; i < runs; i++ )
		{
			// update 10% keys
			for ( int j = 1; j <= tuples_each_run; j = j + 10 )
			{
				key = "/*EP   " + Integer.toString((i-1)*tuples_each_run+j) + "\u0000";
				PBFilter_Delete_key(key);
				PBFilter_Insert_Key(key);
				if(perf == 0)
				{
					out.println(Integer.toString((i-1)*tuples_each_run+j));
				}
			}
			PBFilter_Close((byte)0);
			
			logMsg("========== Q4-3-" + i + " ==========");
			System.gc(); // runs the garbage collector *before*, it can't hurt
			((org.inria.jdbc.Connection) db).resetDBMSCallTime(); 
			start = System.currentTimeMillis();
			// lookup existing keys from the PBFilter index
			for ( int j = 1; j <= i*tuples_each_run; j = j + i*10)
			{
				key = "/*EP   " + Integer.toString(j) + "\u0000";
				PBFilter_Lookup_Key(key);
				if(perf == 0)
				{
					out.println(Integer.toString(j));
				}
			}
			end = System.currentTimeMillis();
			duration = ((org.inria.jdbc.Connection) db).getDBMSCallTime("Q4-3-" + i);
			if (perf == 1)
			{
				System.out.println("Q4-3-" + i + ";" + (end - start) + ";" + duration);
			}
			PBFilter_Close((byte)0);
			
			logMsg("========== Q4-4-" + i + " ==========");
			System.gc(); // runs the garbage collector *before*, it can't hurt
			((org.inria.jdbc.Connection) db).resetDBMSCallTime(); 
			start = System.currentTimeMillis();
			// lookup non-existing keys from the PBFilter index
			for ( int j = 1; j <= i*tuples_each_run; j = j + i*10)
			{
				key = "/*EP   " + Integer.toString(max_num_tuples+j) + "\u0000";
				PBFilter_Lookup_Key(key);
				if(perf == 0)
				{
					out.println(Integer.toString(max_num_tuples+j));
				}
			}
			end = System.currentTimeMillis();
			duration = ((org.inria.jdbc.Connection) db).getDBMSCallTime("Q4-4-" + i);
			if (perf == 1)
			{
				System.out.println("Q4-4-" + i + ";" + (end - start) + ";" + duration);
			}
			PBFilter_Close((byte)0);
		}
		// close the PBFilter structure
		PBFilter_Close((byte)1);
		
		Desinstall_DBMS_MetaData();
		Shutdown_DBMS();
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
}
		