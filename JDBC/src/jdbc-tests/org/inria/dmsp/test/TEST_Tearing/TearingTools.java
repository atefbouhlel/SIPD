package org.inria.dmsp.test.TEST_Tearing;

import java.io.PrintWriter;
import java.sql.Connection;

import org.inria.dmsp.tools.DMSP_QEP_IDs;
import org.inria.dmsp.tools.Tools_dmsp;

import test.jdbc.Tools;

public class TearingTools
{
	// SELECT STAR FROM STAR
	public static void select_star(Connection db, PrintWriter out, int perf) throws Exception
	{
		// the statement for non-parametric select:
		java.sql.Statement st = db.createStatement();
		// the resultset used for the queries:
		java.sql.ResultSet rs;

		if(perf==0){out.println("EP_EPISODE_SELECT_STAR");}
		rs = ((org.inria.jdbc.Statement)st).executeQuery(DMSP_QEP_IDs.EP_TEST.EP_EPISODE_SELECT_STAR);
		Tools.lireResultSet(rs, out);
		if(perf==0){out.println("EP_FORMULAIRE_SELECT_STAR");}
		rs = ((org.inria.jdbc.Statement)st).executeQuery(DMSP_QEP_IDs.EP_TEST.EP_FORMULAIRE_SELECT_STAR);
		Tools.lireResultSet(rs, out);
		if(perf==0){out.println("EP_USER_SELECT_STAR");}
		rs = ((org.inria.jdbc.Statement)st).executeQuery(DMSP_QEP_IDs.EP_TEST.EP_USER_SELECT_STAR);
		Tools.lireResultSet(rs, out);
		if(perf==0){out.println("EP_EVENT_SELECT_STAR");}
		rs = ((org.inria.jdbc.Statement)st).executeQuery(DMSP_QEP_IDs.EP_TEST.EP_EVENT_SELECT_STAR);
		Tools.lireResultSet(rs, out);
		if(perf==0){out.println("EP_INFO_SELECT_STAR");}
		rs = ((org.inria.jdbc.Statement)st).executeQuery(DMSP_QEP_IDs.EP_TEST.EP_INFO_SELECT_STAR);
		Tools.lireResultSet(rs, out);
		if(perf==0){out.println("EP_COMMENT_SELECT_STAR");}
		rs = ((org.inria.jdbc.Statement)st).executeQuery(DMSP_QEP_IDs.EP_TEST.EP_COMMENT_SELECT_STAR);
		Tools.lireResultSet(rs, out);
		if(perf==0){out.println("EP_ROLE_SELECT_STAR");}
		rs = ((org.inria.jdbc.Statement)st).executeQuery(DMSP_QEP_IDs.EP_TEST.EP_ROLE_SELECT_STAR);
		Tools.lireResultSet(rs, out);
		if(perf==0){out.println("EP_HABILITATION_SELECT_STAR");}
		rs = ((org.inria.jdbc.Statement)st).executeQuery(DMSP_QEP_IDs.EP_TEST.EP_HABILITATION_SELECT_STAR);
		Tools.lireResultSet(rs, out);
		if(perf==0){out.println("EP_PATIENT_SELECT_STAR");}
		rs = ((org.inria.jdbc.Statement)st).executeQuery(DMSP_QEP_IDs.EP_TEST.EP_PATIENT_SELECT_STAR);
		Tools.lireResultSet(rs, out);

		// SELECT * from SKT info
		if(perf==0){out.println("-----/////// EP_SKTINFO_SELECT_STAR /////////---------");}
		rs = ((org.inria.jdbc.Statement)st).executeQuery(DMSP_QEP_IDs.EP_TEST.EP_SKTINFO_SELECT_STAR);
		Tools.lireResultSet(rs, out);

		//SELECT * from SKT event
		if(perf==0){out.println("-----/////// EP_SKTEVENT_SELECT_STAR /////////---------");}
		rs = ((org.inria.jdbc.Statement)st).executeQuery(DMSP_QEP_IDs.EP_TEST.EP_SKTEVENT_SELECT_STAR);
		Tools.lireResultSet(rs, out);
	}

	//Delete tuples
	public static void deleteStar(java.sql.Connection db) throws Exception
	{
		java.sql.PreparedStatement ps;

		ps = ((org.inria.jdbc.Connection)db).prepareStatement(DMSP_QEP_IDs.EP_TEST.EP_INFO_DELETE_ALL);
		ps.executeUpdate();
		ps = ((org.inria.jdbc.Connection)db).prepareStatement(DMSP_QEP_IDs.EP_TEST.EP_USER_DELETE_ALL);
		ps.executeUpdate();
		ps = ((org.inria.jdbc.Connection)db).prepareStatement(DMSP_QEP_IDs.EP_TEST.EP_HABILITATION_DELETE_ALL);
		ps.executeUpdate();
		ps = ((org.inria.jdbc.Connection)db).prepareStatement(DMSP_QEP_IDs.EP_TEST.EP_FORMULAIRE_DELETE_ALL);
		ps.executeUpdate();
		ps = ((org.inria.jdbc.Connection)db).prepareStatement(DMSP_QEP_IDs.EP_TEST.EP_COMMENT_DELETE_ALL);
		ps.executeUpdate();
		ps = ((org.inria.jdbc.Connection)db).prepareStatement(DMSP_QEP_IDs.EP_TEST.EP_ROLE_DELETE_ALL);
		ps.executeUpdate();
		ps = ((org.inria.jdbc.Connection)db).prepareStatement(DMSP_QEP_IDs.EP_TEST.EP_EPISODE_DELETE_ALL);
		ps.executeUpdate();
		ps = ((org.inria.jdbc.Connection)db).prepareStatement(DMSP_QEP_IDs.EP_TEST.EP_EVENT_DELETE_ALL);
		ps.executeUpdate();
	}

	// Delete the first tuple of table, and the last one.
	public static void deleteExtreme(java.sql.Connection db, PrintWriter out, int perf, String table) throws Exception
	{
		java.sql.Statement st = db.createStatement();
		java.sql.ResultSet rs;
		java.sql.PreparedStatement ps;
		int epsel, epdel;
		int first, last;

		if (table.equals("info"))
		{
			epsel = DMSP_QEP_IDs.EP_TEST.EP_INFO_SELECT_STAR;
			epdel = DMSP_QEP_IDs.EP_UI.EP_INFO_AC_DELETE_BY_ID;
		}
		else if (table.equals("event"))
		{
			epsel = DMSP_QEP_IDs.EP_TEST.EP_EVENT_SELECT_STAR;
			epdel = DMSP_QEP_IDs.EP_UI.EP_EVENT_AC_DELETE_BY_ID;
		}
		else
		{	// default
			epsel = DMSP_QEP_IDs.EP_TEST.EP_INFO_SELECT_STAR;
			epdel = DMSP_QEP_IDs.EP_UI.EP_INFO_AC_DELETE_BY_ID;
		}

		// Get the first INFO tuple
		rs = ((org.inria.jdbc.Statement)st).executeQuery(epsel);
		rs.next();
		first = rs.getInt("IdGlobal");

		// Get the last INFO tuple
		while(rs.next()){}
		last = rs.getInt("IdGlobal");
		rs.close();

		// Delete first
		ps = ((org.inria.jdbc.Connection)db).prepareStatement(epdel);
		Tools_dmsp.Test_DELETE_BY_ID(first, ps);

		// Delete last
		ps = ((org.inria.jdbc.Connection)db).prepareStatement(epdel);
		Tools_dmsp.Test_DELETE_BY_ID(last, ps);
	}

	public static void insert(java.sql.Connection db, PrintWriter out, int perf, int nb_sform, int nb_bform) throws Exception
	{
		java.sql.PreparedStatement ps;
		int t2, t3, tmp_ev, no_co; // temporary vars

		// idglobal of no comment is 3:
		no_co = 3;

		// insert small forms (fiche de liaison)
		for (int i = 0; i < nb_sform; i++)
		{
			// insert 1 tuple into EVENT
			if (perf == 0) {out.println("// Insertion in table EVENT");}
			ps = ((org.inria.jdbc.Connection)db).prepareStatement(DMSP_QEP_IDs.EP_UI.OEP_EVENT_AC_INSERT, java.sql.Statement.RETURN_GENERATED_KEYS);
			int nb = Tools_dmsp.Test_EVENT_INSERT(
					0,
					0,
					1012 /*formid*/,
					1021 /*userid*/,
					2 /*no_ep*/,
					java.sql.Date.valueOf("2000-10-20"),
					java.sql.Date.valueOf("2000-10-20"),
					12 /*filtre*/,
					ps);
			if (perf == 0) {out.println(nb+" tuple inserted...");}
			// ask for generated key
			tmp_ev = Tools.getIdGlobalGetGeneratedKey(ps.getGeneratedKeys());

			// insert 1 tuple into COMMENT
			if (perf == 0) {out.println("// Insertion in table COMMENT");}
			ps = ((org.inria.jdbc.Connection)db).prepareStatement(DMSP_QEP_IDs.EP_UI.OEP_COMMENT_NOAC_INSERT, java.sql.Statement.RETURN_GENERATED_KEYS);
			nb = Tools_dmsp.Test_COMMENT_INSERT(0, 0, "comment can be big, but more than 450 bytes can't be written", ps);
			if (perf == 0) {out.println(nb+" tuple inserted...");}

			// insert 6 tuples into INFO, the first is linked to the comment
			t3 = 0; // current value of field info.position

			for (int j = 0; j < 6; j++)
			{
				// to simulate the index on <IdGlobal, IdIntern>, link every info to the first comment
				t2 = no_co; // link info to the comment no_comment

				if (perf == 0) {out.println("// Insertion in table INFO");}
				ps = ((org.inria.jdbc.Connection)db).prepareStatement(DMSP_QEP_IDs.EP_UI.OEP_INFO_AC_INSERT, java.sql.Statement.RETURN_GENERATED_KEYS);
				nb = Tools_dmsp.Test_INFO_INSERT(
						0,
						0,
						tmp_ev /*event*/,
						t2 /*comment*/,
						"char value",
						i,
						java.sql.Date.valueOf("2000-10-20"),
						t3++ /*position*/,
						1 /*filtre*/,
						j,
						ps);
				if (perf == 0) {out.println(nb+" tuple inserted...");}
			}
		}

		// insert big forms (GIR Med)
		for (int i = 0; i < nb_bform; i++)
		{
			// one big form is generated after several small forms
			//insert 1 tuple into EVENT
			if (perf == 0) {out.println("// Insertion in table EVENT");}
			ps = ((org.inria.jdbc.Connection)db).prepareStatement(DMSP_QEP_IDs.EP_UI.OEP_EVENT_AC_INSERT, java.sql.Statement.RETURN_GENERATED_KEYS);
			int nb = Tools_dmsp.Test_EVENT_INSERT(
					0,
					0,
					1006 /*GIR medical*/,
					1021,
					2,
					java.sql.Date.valueOf("2000-10-20"),
					java.sql.Date.valueOf("2000-10-20"),
					12,
					ps);
			if (perf == 0) {out.println(nb+" tuple inserted...");}
			tmp_ev = Tools.getIdGlobalGetGeneratedKey(ps.getGeneratedKeys());

			// to simulate the index on <IdGlobal, IdIntern>, link every info to the first comment
			t2 = no_co; // link info to the comment no_comment

			// insert 73 tuples into INFO
			t3 = 0; // current value of field info.position
			for (int j = 0; j < 73; j++)
			{
				if (perf == 0) {out.println("// Insertion in table INFO");}
				ps = ((org.inria.jdbc.Connection)db).prepareStatement(DMSP_QEP_IDs.EP_UI.OEP_INFO_AC_INSERT, java.sql.Statement.RETURN_GENERATED_KEYS);
				nb = Tools_dmsp.Test_INFO_INSERT(
						0,
						0,
						tmp_ev /* event*/,
						t2 /*comment*/,
						"char value",
						i,
						java.sql.Date.valueOf("2000-10-20"),
						t3++ /*position*/,
						1 /*filtre*/,
						j,
						ps);
				if (perf == 0) {out.println(nb+" tuple inserted...");}
			}

			// insert 24 tuples into COMMENT
			if (perf == 0) {out.println("// Insertion in table COMMENT");}
			ps = ((org.inria.jdbc.Connection)db).prepareStatement(DMSP_QEP_IDs.EP_UI.OEP_COMMENT_NOAC_INSERT, java.sql.Statement.RETURN_GENERATED_KEYS);
			nb = Tools_dmsp.Test_COMMENT_INSERT(0, 0, "comment can be big, but more than 450 bytes can't be written", ps);
			if (perf == 0) {out.println(nb+" tuple inserted...");}
		}
	}

	public static void Insert_Tearing_Point(Connection db, PrintWriter out, int perf, byte function_id) throws Exception
	{
		if(perf==0) { out.println("Inserting Tearing Point..."); }
		((org.inria.jdbc.Connection)db).insert_tearingpoint(function_id);
		if(perf==0) { out.println("Done. "); }
	}

	public void Remove_Tearing_Point(Connection db, PrintWriter out, int perf) throws Exception
	{
		if(perf==0) { out.println("Removing Tearing Point..."); }
		((org.inria.jdbc.Connection)db).remove_tearingpoint();
		if(perf==0) { out.println("Done. "); }
	}

	// Delete n tuples of the INFO table
	public static void deleteSome(java.sql.Connection db, PrintWriter out, int perf, int n) throws Exception
	{
		// The statement for non-parametric select:
		java.sql.Statement select_stmt = db.createStatement();
		java.sql.PreparedStatement delete_stmt;

		// The resultset for the select
		java.sql.ResultSet rs;

		// The ids of the tuples we will delete
		int[] ids = new int[n];
		int i, max=0;
		
		rs = ((org.inria.jdbc.Statement)select_stmt).executeQuery(DMSP_QEP_IDs.EP_TEST.EP_INFO_SELECT_STAR);
		for (i=0; i<n && rs.next(); i++)
			ids[i] = rs.getInt("IdGlobal");
		max = i;
		rs.close();
		select_stmt.close();
		for (i=0; i<max; i++)
		{
			delete_stmt = ((org.inria.jdbc.Connection)db).prepareStatement(DMSP_QEP_IDs.EP_UI.EP_INFO_AC_DELETE_BY_ID);
			Tools_dmsp.Test_DELETE_BY_ID(ids[i], delete_stmt);
//			delete_stmt.close(); // TODO bug: if the result of the statement is null: throws a NullPointerException
		}
	}

	// Update nb times the first tuple of the INFO table
	public static void updateSome(java.sql.Connection db, PrintWriter out, int perf, int nb) throws Exception
	{
		// The statement for non-parametric select:
		java.sql.Statement st = db.createStatement();
		// The resultset for the select
		java.sql.ResultSet rs;
		java.sql.PreparedStatement ps;

		// The id of the tuple we will update
		int idg;

		// Get an INFO tuple (not the first)
		rs = ((org.inria.jdbc.Statement)st).executeQuery(DMSP_QEP_IDs.EP_TEST.EP_INFO_SELECT_STAR);
		rs.next();rs.next(); // the 2nd
		idg = rs.getInt("IdGlobal");
		rs.close();

		for (int i=0; i<nb; i++)
		{
			/* UDPATE INFO */
			// PRINT: select le tuple qu'on vient d'inserer
			ps = ((org.inria.jdbc.Connection)db).prepareStatement(DMSP_QEP_IDs.EP_UI.INFO_AC_UPDATE_BY_ID);
			Tools_dmsp.Test_INFO_UPDATE(
					0, /* new TSSPT value */
					"update", /* new char value */
					0, /* new Num value */
					java.sql.Date.valueOf("2000-01-01") /* new date value */,
					0,
					idg,
					ps);
		}
	}

	// Update the first tuple of INFO, and the last one.
	public static void updateExtreme(java.sql.Connection db, PrintWriter out, int perf, String table) throws Exception
	{
		// The statement for non-parametric select:
		java.sql.Statement st = db.createStatement();
		java.sql.ResultSet rs;
		java.sql.PreparedStatement ps;
		int epsel, epupd;
		int first, last;

		if (table.equals("info"))
		{
			epsel = DMSP_QEP_IDs.EP_TEST.EP_INFO_SELECT_STAR;
			epupd = DMSP_QEP_IDs.EP_UI.INFO_AC_UPDATE_BY_ID;
		}
		else if (table.equals("event"))
		{
			epsel = DMSP_QEP_IDs.EP_TEST.EP_EVENT_SELECT_STAR;
			epupd = DMSP_QEP_IDs.EP_UI.EVENT_AC_UPDATE_BY_ID;
		}
		else
		{	// default
			epsel = DMSP_QEP_IDs.EP_TEST.EP_INFO_SELECT_STAR;
			epupd = DMSP_QEP_IDs.EP_UI.INFO_AC_UPDATE_BY_ID;
		}

		// Get the first tuple
		rs = ((org.inria.jdbc.Statement)st).executeQuery(epsel);
		rs.next();
		first = rs.getInt("IdGlobal");

		// Get the last tuple
		while(rs.next()){}
		last = rs.getInt("IdGlobal");
		rs.close();

		if (table.equals("info"))
		{
			ps = ((org.inria.jdbc.Connection)db).prepareStatement(epupd);
			Tools_dmsp.Test_INFO_UPDATE(
					0, /* new TSSPT value */
					"update", /* new char value */
					0, /* new Num value */
					java.sql.Date.valueOf("2000-01-01") /* new date value */,
					0,
					first,
					ps);
			ps = ((org.inria.jdbc.Connection)db).prepareStatement(epupd);
			Tools_dmsp.Test_INFO_UPDATE(
					0, /* new TSSPT value */
					"update", /* new char value */
					0, /* new Num value */
					java.sql.Date.valueOf("2000-01-01") /* new date value */,
					0,
					last,
					ps);
		}
		else if (table.equals("event"))
		{
			ps = ((org.inria.jdbc.Connection)db).prepareStatement(epupd);
			Tools_dmsp.Test_EVENT_UPDATE(
					0 /* new TSSPT value */,
					java.sql.Date.valueOf("2000-01-01") /* new date value */,
					java.sql.Date.valueOf("2000-01-01") /* new date value */,
					0, /* new filtre value*/
					first,
					ps);
			ps = ((org.inria.jdbc.Connection)db).prepareStatement(epupd);
			Tools_dmsp.Test_EVENT_UPDATE(
					0 /* new TSSPT value */,
					java.sql.Date.valueOf("2000-01-01") /* new date value */,
					java.sql.Date.valueOf("2000-01-01") /* new date value */,
					0, /* new filtre value*/
					last,
					ps);
		}
		else
		{	// default
			ps = ((org.inria.jdbc.Connection)db).prepareStatement(epupd);
			Tools_dmsp.Test_INFO_UPDATE(
					0, /* new TSSPT value */
					"update", /* new char value */
					0, /* new Num value */
					java.sql.Date.valueOf("2000-01-01") /* new date value */,
					0,
					first,
					ps);
			ps = ((org.inria.jdbc.Connection)db).prepareStatement(epupd);
			Tools_dmsp.Test_INFO_UPDATE(
					0, /* new TSSPT value */
					"update", /* new char value */
					0, /* new Num value */
					java.sql.Date.valueOf("2000-01-01") /* new date value */,
					0,
					last,
					ps);
		}
	}

	// Update the first tuples of the INFO table until the DBMS falls asleep
	public static void updateInfo(java.sql.Connection db, PrintWriter out, int perf) throws Exception
	{
		// The statement for non-parametric select:
		java.sql.Statement st = db.createStatement();
		// The resultset for the select
		java.sql.ResultSet rs;
		java.sql.PreparedStatement ps;
		// The id of the tuple we will update
		int idg;

		// Get the first INFO tuple
		rs = ((org.inria.jdbc.Statement)st).executeQuery(DMSP_QEP_IDs.EP_TEST.EP_INFO_SELECT_STAR);
		rs.next();
		idg = rs.getInt("IdGlobal");
		rs.close();

		// Do updates until the DBMS falls asleep
		while (true)
		{
			/* UDPATE INFO */
			// PRINT: select le tuple qu'on vient d'inserer
			ps = ((org.inria.jdbc.Connection)db).prepareStatement(DMSP_QEP_IDs.EP_UI.INFO_AC_UPDATE_BY_ID);
			Tools_dmsp.Test_INFO_UPDATE(
					0, /* new TSSPT value */
					"update", /* new char value */
					0, /* new Num value */
					java.sql.Date.valueOf("2000-01-01") /* new date value */,
					0,
					idg,
					ps);
		}
	}

	// duplicated from Tools.
	public static void Insert_Failure_Point(Connection db, PrintWriter out, int perf, byte function_id, byte struct_id, byte struct_type) throws Exception
	{
		if(perf==0) { out.println("Inserting Failure Point..."); }
		((org.inria.jdbc.Connection)db).insert_failure(function_id, struct_id, struct_type);
		if(perf==0) { out.println("Done. "); }
	}
/*
	//	Activating the tearing point 31 requires two conditions:
	//	  * write badly in the NAND sector of the last commited tuple,
	//	  * and this NAND sector must not be the first of the block
	public static void activate_31(Connection db, PrintWriter out, int perf) throws Exception
	{
		// bad NAND write, table 3(EVENT)
		TearingTools.Insert_Failure_Point(db, out, perf, Macro.FUNC_NAND_WRITE_FAIL, (byte)3, Macro.STRUCT_TYPE_TABLE);
		// INSERT SEVERAL TUPLES (more than 10 to fill the NOR because it will fail in the NAND write)
		TearingTools.insert(db, out, perf, 2, 0);
		// DB_ERROR0 est lance!!
	}
*/
}
