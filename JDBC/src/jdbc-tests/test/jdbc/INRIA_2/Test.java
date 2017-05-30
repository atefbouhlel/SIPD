package test.jdbc.INRIA_2;

import java.io.PrintWriter;

import org.inria.database.QEPng;

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

    DataGeneratorWithIds dgwi = new DataGeneratorWithIds(out);
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
			1000,// nbInfo
			db,
			1); // MatPatGenerated
	Save_DBMS_on_disk();
	dgwi.SCE_Test_DB_NOAC(db, out);
    
    Desinstall_DBMS_MetaData();
    Shutdown_DBMS();
  }

}

