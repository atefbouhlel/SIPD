package org.inria.dmsp.test.TEST_Tearing.Inserts.NandSrCommUncomm.Recover_101;

/////////////////////////////////////////////////////
//IMPORTS THAT CAN BE CHANGED TO TEST OTHER PLANS //
/////////////////////////////////////////////////////

///////////////////////////////
//DO NOT CHANGE AFTER HERE  //
///////////////////////////////

import java.io.PrintWriter;
import java.sql.Connection;

import org.inria.dmsp.test.TEST_Tearing.TearingTestFramework;
import org.inria.dmsp.test.TEST_Tearing.TearingTools;
import org.inria.jdbc.Macro;

/**
 * We test here the ability of the DBMS to recover after a user
 * wildly gets the token off the machine after some uncommitted
 * inserts.
 *
 * @author TA
 */
public class Test extends TearingTestFramework
{
	@Override
	public void prepareBeforeTearingPoint(Connection db, PrintWriter out, int perf) throws Exception
	{
	}

	@Override
	public void placeAndReachTearingPoint(Connection db, PrintWriter out, int perf) throws Exception
	{
		// The uncommited transaction allocates some NAND blocks.
		// Note that the recovery of NAND block links is the same
		// whether we allocate blocks during insertions or deletions or
		// updates. So we allocate them through inserts.
		// nb tuples = 200 * (1 event + 1 comment + 6 infos)
		TearingTools.insert(db, out, perf, 200, 0);
		TearingTools.Insert_Tearing_Point(db, out, perf, Macro.TPOINT_INSERT_NANDSRCOMMUNCOMM);
		TearingTools.insert(db, out, perf, 150, 50);
	}
}
