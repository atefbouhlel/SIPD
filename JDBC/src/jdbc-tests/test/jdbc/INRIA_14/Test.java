package test.jdbc.INRIA_14;

import java.io.PrintWriter;

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
		Tools_schemaIndexInfo t = new Tools_schemaIndexInfo(out);
		init();
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

		INSERT_Big_Comment(400);
		Save_DBMS_on_disk();
		DataGeneratorWithIds dgwi = new DataGeneratorWithIds(out);
		dgwi.SCE_Test_DB_NOAC(db, out);

		Desinstall_DBMS_MetaData();
		Shutdown_DBMS();
	}


	private void INSERT_Big_Comment(int nb) throws Exception {
		out.println("INSERTION of COMMENTS");

		// TABLE  COMMENT
		java.sql.PreparedStatement Pstmt = ((org.inria.jdbc.Connection)db).prepareStatement(DMSP_QEP_IDs.Execution_Plan.COMMENT_NOAC_INSERT, java.sql.Statement.RETURN_GENERATED_KEYS);

		String BigComment = "Ceci est un autre commentaire tres long :" +
		" L objectif du projet PlugDB est la conception et l expérimentation de technologies" +
		" permettant une gestion nomade et securisee de données a caractere personnel.";
		Tools_schemaIndexInfo.Test_COMMENT_NOAC_AC_INSERT(404, 0, 0, BigComment, Pstmt);
		BigComment = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
		"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
		"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
		"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
		"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
		"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
		out.println("\nSIZE COMMENT ="+BigComment.length());
		for(int i=0; i<nb; i++){
			Tools_schemaIndexInfo.Test_COMMENT_NOAC_AC_INSERT(404, 0, 0, BigComment, Pstmt);
		}

		Tools_schemaIndexInfo.Test_COMMENT_NOAC_AC_INSERT(404, 0, 0, "Another Comment", Pstmt);
	}
}

