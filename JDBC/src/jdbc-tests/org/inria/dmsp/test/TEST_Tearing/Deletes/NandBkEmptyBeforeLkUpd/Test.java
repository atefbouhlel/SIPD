package org.inria.dmsp.test.TEST_Tearing.Deletes.NandBkEmptyBeforeLkUpd;

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
 * deletes.
 *
 * Do not launch the NandBkEmpty* delete tests because deletes are
 * very slow do to expensive table scans.
 *
 * @author TA
 */
public class Test extends TearingTestFramework
{
	@Override
	public void prepareBeforeTearingPoint(Connection db, PrintWriter out, int perf) throws Exception
	{
		// Do some deletes to avoid being in the peculiar case of the first delete
		TearingTools.deleteSome(db, out, perf, 2);

		// Do some inserts otherwise there will not be enough tuples to delete
		// A block is 128kB, a deleteTuple is 12B so we need to delete ~10700 tuples to generate the tearing case
		// 1 big form = (1 EVENT + 24 COMMENT +  73 INFO)
		// ==> We need to insert about 110 big forms
		TearingTools.insert(db, out, perf, 0, 110);
	}

	// 1536
	@Override
	public void placeAndReachTearingPoint(Connection db, PrintWriter out, int perf) throws Exception
	{
		TearingTools.Insert_Tearing_Point(db, out, perf, Macro.TPOINT_DELETE_NANDBKEMPTYBEFORELKUPD);
		TearingTools.deleteStar(db);
	}
}
