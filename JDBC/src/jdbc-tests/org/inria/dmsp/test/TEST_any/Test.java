package org.inria.dmsp.test.TEST_any;

import java.io.InputStreamReader;
import java.io.PrintWriter;

import org.inria.database.QEPng;
import org.inria.dmsp.tools.DMSP_QEP_IDs;
import org.inria.dmsp.tools.DataGenerator;
import org.inria.dmsp.tools.DeltaLoader;
import org.inria.dmsp.tools.Tools_dmsp;

import test.jdbc.Tools;
import test.runner.ITestTearing;

/*
 * This is NOT A TEARING TEST !!!
 */
public class Test extends Tools implements ITestTearing
{
	protected void InitMeta() throws Exception
	{
		int usedId = 404;
		Tools_dmsp t = new Tools_dmsp( out );
		// load the DB schema
		byte[] schemaDesc = t.loadSchema( "/org/inria/dmsp/schemaV4.rsc" );
		Install_DBMS_MetaData( schemaDesc, usedId );
		schemaDesc = null;
		
		// load and install QEPs
		Class<?>[] executionPlans = new Class[] { org.inria.dmsp.EP_Synchro.class, org.inria.dmsp.schema.EP_TEST.class, 
				org.inria.dmsp.EP_UI.class, org.inria.dmsp.EP_PDS.class };
		QEPng.loadExecutionPlans( org.inria.dmsp.tools.DMSP_QEP_IDs.class, executionPlans );
		QEPng.installExecutionPlans( db );
	}
	@Override
	public void run( PrintWriter out, String dbmsHost, short cmd ) throws Exception
	{
		DeltaLoader dl = null;
		DataGenerator dg = null;
		short param = (short)( ( cmd & 0xff00 ) >> 8 );	// take second byte as a param
		byte i = 0;
		//java.sql.Statement st = db.createStatement();
		java.sql.PreparedStatement ps;
		java.sql.ResultSet rs;
		// required on PC (durable variable on token):
		org.inria.jdbc.DBMS.initialized = true;
		this.out = out;	// set output stream and instantiate the tools
		perf = 0;
		init();
		openConnection( dbmsHost, null );
		out.println(" cmd: " + (cmd & 0xff) + ", param: " + param);

		switch ( cmd & 0xff )
		{
		case 0:
			InitMeta();
			dl = new DeltaLoader( out, perf );	// instantiate delta loader
			dl.LoadDelta(	// open the delta file
					new InputStreamReader( Test.class.getResourceAsStream("delta.csv") ),
					false, db );	// load the data into database
			break;
		case 1:
			InitMeta();
			dl = new DeltaLoader( out, perf );	// instantiate delta loader
			dl.LoadDelta(	// open the delta file
					new InputStreamReader( Test.class.getResourceAsStream("delta.csv") ),
					true, db );	// load the data into database
			break;
		case 2:
			InitMeta();
			// Call the data generator
			dg = new DataGenerator( out );
			dg.perf = perf;
			dg.INSERT_Data_Generated( param, false, db );
			break;
		case 3:
			InitMeta();
			// Call the data generator
			dg = new DataGenerator( out );
			dg.perf = perf;
			dg.INSERT_Data_Generated( param, true, db );
			break;
		case 4:
		case 5:
			Desinstall_DBMS_MetaData();
			break;
		case 6:
		case 7:
			select_star();
			break;
		case 8:
			ps = ((org.inria.jdbc.Connection)db).prepareStatement(DMSP_QEP_IDs. EP_UI.EP_QUERY_NOAC_SELECT_EVENT_INFO_COMMENT_BY_FORM );
			rs = Tools_dmsp.Test_SELECT_BY_INT( 1000 + give_tuple_number( param ), ps);
			lireResultSet_perf0( rs, out );
			while ( i < 20 )	print_perfs( i++ );
			break;
		case 10:
			ps = ((org.inria.jdbc.Connection)db).prepareStatement(DMSP_QEP_IDs. EP_UI.EP_QUERY_NOAC_SELECT_EVENT_INFO_COMMENT_BY_FORM_OPT );
			rs = Tools_dmsp.Test_SELECT_BY_INT( 1000 + give_tuple_number( param ), ps);
			lireResultSet_perf0( rs, out );
			while ( i < 20 )	print_perfs( i++ );
			break;
		case 9:
		case 11:
			ps = ((org.inria.jdbc.Connection)db).prepareStatement(DMSP_QEP_IDs. EP_UI.EP_QUERY_NOAC_SELECT_EVENT_INFO_COMMENT_BY_FORM_SIGMOD );
			rs = Tools_dmsp.Test_SELECT_BY_INT( 1000 + give_tuple_number( param ), ps);
			lireResultSet_perf0( rs, out );
			while ( i < 20 )	print_perfs( i++ );
			break;
		case 12:
			ps = ((org.inria.jdbc.Connection)db).prepareStatement(DMSP_QEP_IDs. EP_Synchro.EP_INFO_INSERT_OLD,
					java.sql.Statement.NO_GENERATED_KEYS );
			Tools_dmsp.Test_INFO_INSERT( 99998, 0, 2, 0, 99996, 99997, "INSERTED HERE HAHA!",
					12345, java.sql.Date.valueOf("2010-06-06"), 13, 1, 100, ps);
			while ( i < 20 )	print_perfs( i++ );
			break;
		case 13:
			ps = ((org.inria.jdbc.Connection)db).prepareStatement(DMSP_QEP_IDs. EP_Synchro.EP_INFO_INSERT_SIGMOD,
					java.sql.Statement.NO_GENERATED_KEYS );
			Tools_dmsp.Test_INFO_INSERT( 99998, 0, 2, 0, 99996, 99997, "INSERTED HERE HAHA!",
					12345, java.sql.Date.valueOf("2010-06-06"), 13, 1, 100, ps);
			while ( i < 20 )	print_perfs( i++ );
			break;
		case 14:
			ps = ((org.inria.jdbc.Connection)db).prepareStatement(DMSP_QEP_IDs. EP_Synchro.EP_INFO_INSERT_OLD,
					java.sql.Statement.NO_GENERATED_KEYS );
			Tools_dmsp.Test_INFO_INSERT( 99999, 0, 2, 0, 99994, 99995, "INSERTED HERE!",
					12345, java.sql.Date.valueOf("2010-06-06"), 13, 1, 100, ps);
			while ( i < 20 )	print_perfs( i++ );
			break;
		case 15:
			ps = ((org.inria.jdbc.Connection)db).prepareStatement(DMSP_QEP_IDs. EP_Synchro.EP_INFO_INSERT_SIGMOD,
					java.sql.Statement.NO_GENERATED_KEYS );
			Tools_dmsp.Test_INFO_INSERT( 99999, 0, 2, 0, 99994, 99995, "INSERTED HERE!",
					12345, java.sql.Date.valueOf("2010-06-06"), 13, 1, 100, ps);
			while ( i < 20 )	print_perfs( i++ );
			break;

		// perf = 1
		case 16: // copy of 8
			ps = ((org.inria.jdbc.Connection)db).prepareStatement(DMSP_QEP_IDs. EP_UI.EP_QUERY_NOAC_SELECT_EVENT_INFO_COMMENT_BY_FORM );
			rs = Tools_dmsp.Test_SELECT_BY_INT( 1000 + give_tuple_number( param ), ps);
			lireResultSet_perf1( rs, out );
			while ( i < 20 )	print_perfs( i++ );
			break;
		case 18: // copy of 10
			ps = ((org.inria.jdbc.Connection)db).prepareStatement(DMSP_QEP_IDs. EP_UI.EP_QUERY_NOAC_SELECT_EVENT_INFO_COMMENT_BY_FORM_OPT );
			rs = Tools_dmsp.Test_SELECT_BY_INT( 1000 + give_tuple_number( param ), ps);
			lireResultSet_perf1( rs, out );
			while ( i < 20 )	print_perfs( i++ );
			break;
		case 17: // copy of 9
		case 19: // copy of 11
			ps = ((org.inria.jdbc.Connection)db).prepareStatement(DMSP_QEP_IDs. EP_UI.EP_QUERY_NOAC_SELECT_EVENT_INFO_COMMENT_BY_FORM_SIGMOD );
			rs = Tools_dmsp.Test_SELECT_BY_INT( 1000 + give_tuple_number( param ), ps);
			lireResultSet_perf1( rs, out );
			while ( i < 20 )	print_perfs( i++ );
			break;

		default:
			out.println( "Wrong command value: " + cmd );
		}
		if ( ( cmd & 0xff ) != 4 &&
			 ( cmd & 0xff ) != 5 )
		{
			Save_DBMS_on_disk();
		}
		Shutdown_DBMS();
		out.flush();
	}

	public short give_tuple_number (short num) {
		short ret = 1;

		if (num != 1)
			ret = (short) (num*4);

		return ret;
	}

	public void select_star() throws Exception {

		// the statement for non-parametric select:
		java.sql.Statement st = db.createStatement();
		// the resultset used for the queries:
		java.sql.ResultSet rs;

		if(perf==0){out.println("EP_EPISODE_SELECT_STAR");}
		rs = ((org.inria.jdbc.Statement)st).executeQuery(DMSP_QEP_IDs.EP_TEST.EP_EPISODE_SELECT_STAR);
		lireResultSet(rs, out);
		if(perf==0){out.println("EP_FORMULAIRE_SELECT_STAR");}
		rs = ((org.inria.jdbc.Statement)st).executeQuery(DMSP_QEP_IDs.EP_TEST.EP_FORMULAIRE_SELECT_STAR);
		lireResultSet(rs, out);
		if(perf==0){out.println("EP_USER_SELECT_STAR");}
		rs = ((org.inria.jdbc.Statement)st).executeQuery(DMSP_QEP_IDs.EP_TEST.EP_USER_SELECT_STAR);
		lireResultSet(rs, out);
		if(perf==0){out.println("EP_EVENT_SELECT_STAR");}
		rs = ((org.inria.jdbc.Statement)st).executeQuery(DMSP_QEP_IDs.EP_TEST.EP_EVENT_SELECT_STAR);
		lireResultSet(rs, out);
		if(perf==0){out.println("EP_INFO_SELECT_STAR");}
		rs = ((org.inria.jdbc.Statement)st).executeQuery(DMSP_QEP_IDs.EP_TEST.EP_INFO_SELECT_STAR);
		lireResultSet(rs, out);
		if(perf==0){out.println("EP_COMMENT_SELECT_STAR");}
		rs = ((org.inria.jdbc.Statement)st).executeQuery(DMSP_QEP_IDs.EP_TEST.EP_COMMENT_SELECT_STAR);
		lireResultSet(rs, out);
		if(perf==0){out.println("EP_ROLE_SELECT_STAR");}
		rs = ((org.inria.jdbc.Statement)st).executeQuery(DMSP_QEP_IDs.EP_TEST.EP_ROLE_SELECT_STAR);
		lireResultSet(rs, out);
		if(perf==0){out.println("EP_HABILITATION_SELECT_STAR");}
		rs = ((org.inria.jdbc.Statement)st).executeQuery(DMSP_QEP_IDs.EP_TEST.EP_HABILITATION_SELECT_STAR);
		lireResultSet(rs, out);
		if(perf==0){out.println("EP_PATIENT_SELECT_STAR");}
		rs = ((org.inria.jdbc.Statement)st).executeQuery(DMSP_QEP_IDs.EP_TEST.EP_PATIENT_SELECT_STAR);
		lireResultSet(rs, out);

		// SELECT * from SKT info
		if(perf==0){out.println("-----/////// EP_SKTINFO_SELECT_STAR /////////---------");}
		rs = ((org.inria.jdbc.Statement)st).executeQuery(DMSP_QEP_IDs.EP_TEST.EP_SKTINFO_SELECT_STAR);
		lireResultSet(rs, out);

		//SELECT * from SKT event
		if(perf==0){out.println("-----/////// EP_SKTEVENT_SELECT_STAR /////////---------");}
		rs = ((org.inria.jdbc.Statement)st).executeQuery(DMSP_QEP_IDs.EP_TEST.EP_SKTEVENT_SELECT_STAR);
		lireResultSet(rs, out);

	}
}
