package test.jdbc.INRIA_13;

import java.io.PrintWriter;
import java.sql.Date;
import java.util.Calendar;
import java.util.Vector;

import org.inria.database.QEPng;
import org.inria.dmsp.tools.DMSP_QEP_IDs;

import test.jdbc.Tools;
import test.jdbc.schemaIndexInfo.DataGeneratorWithIds;
import test.jdbc.schemaIndexInfo.Tools_schemaIndexInfo;
import test.runner.ITest;

public class Test extends Tools implements ITest {

	@Override
	public void run(PrintWriter out, String dbmsHost) throws Exception {
		this.out = out;
		perf = 1;
		Tools_schemaIndexInfo t = new Tools_schemaIndexInfo(out);
		init();
		long duration;
		long start, end;

		//Populates the database
		openConnection(dbmsHost, null);
		String schema = "schemaV4.rsc";
		byte[] schemaDesc = t.loadSchema(schema);
		int usedId = 404;
		Install_DBMS_MetaData(schemaDesc, usedId);
	    schemaDesc = null;
	    
	    // load and install QEPs
	 	Class<?>[] executionPlans = new Class[] { test.jdbc.schemaIndexInfo.Execution_Plan.class };
	 	QEPng.loadExecutionPlans( org.inria.dmsp.tools.DMSP_QEP_IDs.class, executionPlans );
	 	QEPng.installExecutionPlans( db );

    System.gc();
    ((org.inria.jdbc.Connection) db).resetDBMSCallTime(); 
    duration = ((org.inria.jdbc.Connection) db).getDBMSCallTime(null);
	  start = System.currentTimeMillis();
    ((org.inria.jdbc.Connection) db).resetDBMSCallTime(); 
		DataGeneratorWithIds dgwi = new DataGeneratorWithIds(out);
		dgwi.perf=1;
		dgwi.INSERT_Data_Generated(
				5, // nbUser
				3, // nbRole
				7, // nbHab
				20, // nbForm
				0, // nbMatriceDMSP
				1, // nbEpisode
				100, // nbEvent
				309, // nbConcept
				50, // nbComment
				1000, // nbInfo
				db,
				1); 
		Save_DBMS_on_disk();
    end = System.currentTimeMillis();
    duration = ((org.inria.jdbc.Connection) db).getDBMSCallTime("Init");
    System.out.println("Init;" + (end - start) + ";" + duration);
		Shutdown_DBMS();
		
		//====================================================//
		// MEASURE I - Formular insertion with access control //
		//====================================================//
		String[] authent = new String[2];
		// Docteur 1 nommé Pierre Dupont se connecte
		String cur_User = "3"; // Martin Roger
		String cur_Role = "8"; // Referent Social
		authent[0] = cur_User;
		authent[1] = cur_Role;
		openConnection(dbmsHost, authent);
		((org.inria.jdbc.Connection) db).resetDBMSCallTime(); 
    start = System.currentTimeMillis();
		// TABLE  EVENT Add of Formulaire Grille AGGIR Dupont Pierre appartenant à noEpisode
		java.sql.PreparedStatement ps = ((org.inria.jdbc.Connection)db).prepareStatement(DMSP_QEP_IDs.Execution_Plan.EVENT_AC_INSERT, java.sql.Statement.RETURN_GENERATED_KEYS);
		java.sql.ResultSet rs = Tools_schemaIndexInfo.Test_EVENT_NOAC_AC_INSERT(0, 0, 0, (341), (3), (345), java.sql.Date.valueOf("2008-12-18"), ps);
		int IdG = getIdGlobalGetGeneratedKey(rs);
		//int IdG = 26476545;
	
		// insert 10 tuples to table INFO
		ps = ((org.inria.jdbc.Connection)db).prepareStatement(DMSP_QEP_IDs.Execution_Plan.INFO_AC_INSERT);
		Tools_schemaIndexInfo.Test_INFO_NOAC_AC_INSERT(0, 0, 0, IdG, (18), (456), null, 0, 1, ps);
		Tools_schemaIndexInfo.Test_INFO_NOAC_AC_INSERT(0, 0, 0, IdG, (17), (451), null, 0, 14, ps);
		Tools_schemaIndexInfo.Test_INFO_NOAC_AC_INSERT(0, 0, 0, IdG, (21), (451), null, 1, 10, ps);
		Tools_schemaIndexInfo.Test_INFO_NOAC_AC_INSERT(0, 0, 0, IdG, (34), (447), null, 20080312, 5, ps);
		Tools_schemaIndexInfo.Test_INFO_NOAC_AC_INSERT(0, 0, 0, IdG, (31), (456), null, 0, 0, ps);
		Tools_schemaIndexInfo.Test_INFO_NOAC_AC_INSERT(0, 0, 0, IdG, (18), (447), null, 0, 1, ps);
		Tools_schemaIndexInfo.Test_INFO_NOAC_AC_INSERT(0, 0, 0, IdG, (17), (464), null, 0, 14, ps);
		Tools_schemaIndexInfo.Test_INFO_NOAC_AC_INSERT(0, 0, 0, IdG, (21), (464), null, 1, 10, ps);
		Tools_schemaIndexInfo.Test_INFO_NOAC_AC_INSERT(0, 0, 0, IdG, (23), (487), null, 20080312, 5, ps);
		Tools_schemaIndexInfo.Test_INFO_NOAC_AC_INSERT(0, 0, 0, IdG, (28), (492), null, 0, 0, ps);
		Save_DBMS_on_disk();
    end = System.currentTimeMillis();
    duration = ((org.inria.jdbc.Connection) db).getDBMSCallTime("I");
    System.out.println("PERF:I;" + (end - start) + ";" + duration);
		// END MEASURE I
		Shutdown_DBMS();

		//=======================================================//
		// MEASURE II - Chronological view without access control
		//=======================================================//
		openConnection(dbmsHost, null);
		
		// MEASURE II-1
    System.gc();
    ((org.inria.jdbc.Connection) db).resetDBMSCallTime(); 
    start = System.currentTimeMillis();
		java.sql.Statement stmt = db.createStatement();
		// SELECT formulaire.nom, event.dateEvent, userdmsp.nom FROM formulaire, userdmsp, event WHERE event.idUser=userdmsp.IdGlobal and event.idform=formulaire.IdGlobal
		lireResultSet(request(stmt, DMSP_QEP_IDs.Execution_Plan.SELECT_NOAC_FORM_EVENT_USER), out);
    end = System.currentTimeMillis();
    duration = ((org.inria.jdbc.Connection) db).getDBMSCallTime("II-1");
    System.out.println("PERF:II-1;" + (end - start) + ";" + duration);
		
		// MEASURE II-2
    System.gc();
    ((org.inria.jdbc.Connection) db).resetDBMSCallTime(); 
    start = System.currentTimeMillis();
		Vector<Integer> as = new Vector<Integer>();
		// SELECT nom, IdGlobal FROM formulaire
		rs = request(stmt, DMSP_QEP_IDs.Execution_Plan.SELECT_NOAC_FORM);
		// SELECT event.dateEvent, userdmsp.nom FROM userdmsp, event WHERE event.idUser=userdmsp.IdGlobal and event.idform=?
		ps = ((org.inria.jdbc.Connection)db).prepareStatement(DMSP_QEP_IDs.Execution_Plan.SELECT_NOAC_EVENT_USER_BY_FORM );
		while (rs.next()) {
			// IdGlobal
			int b = rs.getInt(2);
			if (!rs.wasNull()) {
				int a = b;
				as.addElement(a);
			}
		}
		for(int i = 0;i<as.size();i++){
			int IdForm = as.elementAt(i);
			lireResultSet(Tools_schemaIndexInfo.Test_SELECT_NOAC_EVENT_USER_BY_FORM(IdForm, ps), out);
		}
		as = null;
    end = System.currentTimeMillis();
    duration = ((org.inria.jdbc.Connection) db).getDBMSCallTime("II-2");
    System.out.println("PERF:II-2;" + (end - start) + ";" + duration);
		
		// MEASURE II-3
    System.gc();
    ((org.inria.jdbc.Connection) db).resetDBMSCallTime(); 
    start = System.currentTimeMillis();
		as = new Vector<Integer>();
		// SELECT nom, IdGlobal FROM formulaire
		rs = request(stmt, DMSP_QEP_IDs.Execution_Plan.SELECT_NOAC_FORM);
		// SELECT event.dateEvent, userdmsp.nom FROM userdmsp, event WHERE event.idUser=userdmsp.IdGlobal and event.idform=?
		ps = ((org.inria.jdbc.Connection)db).prepareStatement(DMSP_QEP_IDs.Execution_Plan.SELECT_NOAC_EVENT_USER_BY_FORM_2 );
		while (rs.next()) {
			// IdGlobal
			int b = rs.getInt(2);
			if (!rs.wasNull()) {
				int a = b;
				as.addElement(a);
			}
		}
		for(int i = 0;i<as.size();i++){
			int IdForm = as.elementAt(i);
			lireResultSet(Tools_schemaIndexInfo.Test_SELECT_NOAC_EVENT_USER_BY_FORM(IdForm, ps), out);
		}
    end = System.currentTimeMillis();
    duration = ((org.inria.jdbc.Connection) db).getDBMSCallTime("II-3");
    System.out.println("PERF:II-3;" + (end - start) + ";" + duration);
		// END MEASURE II
		Shutdown_DBMS();

		//==================================================//
		// MEASURE III - Formular open from the grid with AC
		//==================================================//
		authent = new String[2];
		// Docteur 1 nommé Pierre Dupont se connecte
		cur_User = "3"; // Martin Roger
		cur_Role = "8"; // Referent Social
		authent[0] = cur_User;
		authent[1] = cur_Role;
		openConnection(dbmsHost, authent);
		
		// MEASURE III-1
    System.gc();
    ((org.inria.jdbc.Connection) db).resetDBMSCallTime(); 
    start = System.currentTimeMillis();
		//SELECT userdmsp.nom, event.dateEvent FROM userdmsp, event, formulaire WHERE userdmsp.IdGlobal=event.IdUser and event.idform=formulaire.IdGlobal and event.IdGlobal=?
		ps = ((org.inria.jdbc.Connection)db).prepareStatement(DMSP_QEP_IDs.Execution_Plan.SELECT_AC_EVENT_USER_BY_EVENT );
		int IdEvent = 423;
		lireResultSet(Tools_schemaIndexInfo.Test_SELECT_NOAC_EVENT_USER_BY_FORM(IdEvent, ps), out);
		//SELECT i.IdGlobal, i.IdEvent, i.IdConcept, i.IdComment, i.Valchar, i.Valnum, i.Position, c.IdGlobal, c.ValComment, co.IdGlobal, co.Unit, co.Nom, co.dataType FROM info i, comment c, concept co WHERE i.IdConcept=co.IdGlobal and i.IdComment=c.IdGlobal and i.IdEvent=?
		ps = ((org.inria.jdbc.Connection)db).prepareStatement(DMSP_QEP_IDs.Execution_Plan.SELECT_AC_INFO_COMMENT_CONCEPT_BY_EVENT );
		lireResultSet(Tools_schemaIndexInfo.Test_SELECT_NOAC_EVENT_USER_BY_FORM(IdEvent, ps), out);
    end = System.currentTimeMillis();
    duration = ((org.inria.jdbc.Connection) db).getDBMSCallTime("III-1");
    System.out.println("PERF:III-1;" + (end - start) + ";" + duration);
		
		// MEASURE III-2
    System.gc();
    ((org.inria.jdbc.Connection) db).resetDBMSCallTime(); 
    start = System.currentTimeMillis();
		//SELECT userdmsp.nom, event.dateEvent FROM userdmsp, event, formulaire WHERE userdmsp.IdGlobal=event.IdUser and event.idform=formulaire.IdGlobal and event.IdGlobal=?
		ps = ((org.inria.jdbc.Connection)db).prepareStatement(DMSP_QEP_IDs.Execution_Plan.SELECT_AC_EVENT_USER_BY_EVENT );
		IdEvent = 423;
		lireResultSet(Tools_schemaIndexInfo.Test_SELECT_NOAC_EVENT_USER_BY_FORM(IdEvent, ps), out);
		//SELECT e.IdGlobal, e.IdForm, e.IdUser, e.IdEpisode, e.DateEvent, i.IdGlobal, i.IdEvent, i.IdConcept, i.IdComment, i.Valchar, i.Valnum, i.Position, c.IdGlobal, c.ValComment, co.IdGlobal, co.Unit, co.Nom, co.dataType FROM event e, info i, comment c, concept co WHERE i.Idevent=e.IdGlobal and i.IdConcept=co.IdGlobal and i.IdComment=c.IdGlobal and e.IdGlobal=?
		ps = ((org.inria.jdbc.Connection)db).prepareStatement(DMSP_QEP_IDs.Execution_Plan.SELECT_AC_EVENT_INFO_COMMENT_CONCEPT_BY_EVENT );
		lireResultSet(Tools_schemaIndexInfo.Test_SELECT_NOAC_EVENT_USER_BY_FORM(IdEvent, ps), out);
    end = System.currentTimeMillis();
    duration = ((org.inria.jdbc.Connection) db).getDBMSCallTime("III-2");
    System.out.println("PERF:III-2;" + (end - start) + ";" + duration);
		
		// MEASURE III-3
    System.gc();
    ((org.inria.jdbc.Connection) db).resetDBMSCallTime(); 
    start = System.currentTimeMillis();
		//SELECT u.IdGlobal, u.nom, u.Prenom, u.sexe, u.adresse, u.ville, u.codepost, u.tel1, u.tel2, u.usertype, 
		//  e.IdGlobal, e.IdForm, e.IdUser, e.IdEpisode, e.DateEvent, 
		//  i.IdGlobal, i.IdEvent, i.IdConcept, i.IdComment, i.Valchar, i.Valnum, i.Position, 
		//  c.IdGlobal, c.ValComment, 
		//  co.IdGlobal, co.Unit, co.Nom, co.dataType 
		//  FROM userdmsp u, event e, info i, comment c, concept co 
		//  WHERE u.IdGlobal=e.IdUser and i.Idevent=e.IdGlobal and i.IdConcept=co.IdGlobal and i.IdComment=c.IdGlobal and e.IdGlobal=?
		ps = ((org.inria.jdbc.Connection)db).prepareStatement(DMSP_QEP_IDs.Execution_Plan.SELECT_AC_USER_EVENT_INFO_COMMENT_CONCEPT_BY_EVENT );
		IdEvent = 423;
		lireResultSet(Tools_schemaIndexInfo.Test_SELECT_NOAC_EVENT_USER_BY_FORM(IdEvent, ps), out);
    end = System.currentTimeMillis();
    duration = ((org.inria.jdbc.Connection) db).getDBMSCallTime("III-3");
    System.out.println("PERF:III-3;" + (end - start) + ";" + duration);
		
		// MEASURE III-4
    System.gc();
    ((org.inria.jdbc.Connection) db).resetDBMSCallTime(); 
    start = System.currentTimeMillis();
		//SELECT u.IdGlobal, u.nom, u.Prenom, u.sexe, u.adresse, u.ville, u.codepost, u.tel1, u.tel2, u.usertype, 
		//  e.IdGlobal, e.IdForm, e.IdUser, e.IdEpisode, e.DateEvent, 
		//  i.IdGlobal, i.IdEvent, i.IdConcept, i.IdComment, i.Valchar, i.Valnum, i.Position, 
		//  c.IdGlobal, c.ValComment, 
		//  co.IdGlobal, co.Unit, co.Nom, co.dataType 
		//  FROM userdmsp u, event e, info i, comment c, concept co 
		// WHERE u.idGlobal=e.Iduser and i.Idevent=e.IdGlobal and i.IdConcept=co.IdGlobal and i.IdComment=c.IdGlobal and e.IdGlobal=? AND e.dateEvent=?
		ps = ((org.inria.jdbc.Connection)db).prepareStatement(DMSP_QEP_IDs.Execution_Plan.SELECT_AC_USER_EVENT_INFO_COMMENT_CONCEPT_BY_EVENT_DATE);
		IdEvent = 423;
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 2002);
		cal.set(Calendar.MONTH, 3);
		cal.set(Calendar.DAY_OF_MONTH, 05);
		long sqlDate = cal.getTime().getTime();
		Date d = new java.sql.Date(sqlDate);
		lireResultSet(Tools_schemaIndexInfo.Test_SELECT_AC_USER_EVENT_INFO_COMMENT_CONCEPT_BY_EVENT_DATE(IdEvent, d, ps), out);
    end = System.currentTimeMillis();
    duration = ((org.inria.jdbc.Connection) db).getDBMSCallTime("III-4");
    System.out.println("PERF:III-4;" + (end - start) + ";" + duration);
		
		// MEASURE III-5
    System.gc();
    ((org.inria.jdbc.Connection) db).resetDBMSCallTime(); 
    start = System.currentTimeMillis();
		//SELECT f.IdGlobal, f.nom, 
		//u.IdGlobal, u.nom, u.Prenom, u.sexe, u.adresse, u.ville, u.codepost, u.tel1, u.tel2, u.usertype, 
		//e.IdGlobal, e.IdForm, e.IdUser, e.IdEpisode, e.DateEvent,
		//i.IdGlobal, i.IdEvent, i.IdConcept, i.IdComment, i.Valchar, i.Valnum, i.Position, 
		//c.IdGlobal, c.ValComment, 
		//co.IdGlobal, co.Unit, co.Nom, co.dataType 
		//FROM formulaire f, userdmsp u, event e, info i, comment c, concept co 
		//WHERE f.IdGlobal=e.IdForm and u.idGlobal=e.Iduser and i.Idevent=e.IdGlobal and i.IdConcept=co.IdGlobal and i.IdComment=c.IdGlobal and e.IdGlobal=? AND e.dateEvent=? and f.nom=?
		ps = ((org.inria.jdbc.Connection)db).prepareStatement(DMSP_QEP_IDs.Execution_Plan.SELECT_AC_USER_EVENT_INFO_COMMENT_CONCEPT_BY_EVENT_DATE_FORM);
		IdEvent = 423;
		d = new java.sql.Date(sqlDate);
		String formName = "Hospitalisation";
		lireResultSet(Tools_schemaIndexInfo.Test_SELECT_AC_USER_EVENT_INFO_COMMENT_CONCEPT_BY_EVENT_DATE_FORM(IdEvent, d, formName, ps), out);
    end = System.currentTimeMillis();
    duration = ((org.inria.jdbc.Connection) db).getDBMSCallTime("III-5");
    System.out.println("PERF:III-5;" + (end - start) + ";" + duration);

		// END MEASURE III
		Shutdown_DBMS();
		
		//==========================================================//
		// MEASURE IV - Insert of synchro file : not yet implemented
		//==========================================================//

		openConnection(dbmsHost, null);
		Desinstall_DBMS_MetaData();
		Shutdown_DBMS();
	}

}

